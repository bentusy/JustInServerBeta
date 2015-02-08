package attracti.develop.callalign.server.users

import java.util.Calendar

import akka.actor.{ActorRef,Cancellable}
import akka.event.{LoggingAdapter, Logging}
import attracti.develop.callalign.server.intents.{IntentConteiner, UsersMetaData, Intent}
import attracti.develop.callalign.server.GlobalContext
import attracti.develop.callalign.server.utill._
//import org.apache.logging.log4j.{LogManager, Logger}
import akka.pattern.{ ask, pipe }

import scala.collection.{mutable, Map}
import scala.collection.mutable.{Map => MMap, TreeSet, ArrayBuffer}
import scala.concurrent.{ExecutionContextExecutor, Promise, Future}
import scala.util.{Failure, Success}


class User(val id: String, val countryCod:String, val globalMap:Map[String, ActorRef], val intentManager: ActorRef,
            val dao: ActorRef, val metaMap:Map[String, UsersMetaData], val userManager: ActorRef) {

  var log:LoggingAdapter = null

//  val log: Logger = LogManager.getLogger("InfoLoger")

  var deviceType:Int=0
  var userActor:UserActor=null
  var connection:ActorRef=null
  implicit var slf:ActorRef=null
  implicit var ex: ExecutionContextExecutor=null
  var status = Status(0);
  var statusTo0Cancell:Cancellable=null
  var cancellableStatusChngIntrequestForCall:Cancellable=null
  var contacts: ArrayBuffer[String] = ArrayBuffer()
  val regContatcs: MMap[String, UsersMetaData] = MMap()
  val favoritList: MMap[String, ActorRef] = MMap()
  val seeingList: MMap[String, ActorRef] = MMap()
  val incomingIntent=  MMap[String, IntentConteiner]()
  val outgoingIntent= MMap[String, IntentConteiner]()
  val recycleIncomingIntents= MMap[String, IntentConteiner]()
  val recycleOutgoingIntents= MMap[String, IntentConteiner]()
  var calculator:ActorRef = null
  var flagIntentWorkWithMe = false
  var lastConnectDate=Calendar.getInstance()
  var flagConfigStatusEvent:Int=0
  var currentIntentsPromis:Promise[Int]=null


  def requestForCallFromTcp(rUser: String, pid: Int): Unit = {

    val rUserM =regContatcs.getOrElse(rUser, null)
    if(rUserM!=null){
      if(flagIntentWorkWithMe==false){
        flagIntentWorkWithMe==true
        val pc=Promise[Int]()
        cancellableStatusChngIntrequestForCall = userActor.context.system.scheduler.scheduleOnce(GlobalContext.delayUserToUserCallRQ){pc.trySuccess(1)}
        pc.future.onSuccess{
          case v:Int if v==5 =>connection ! UserToTcpResponsForCallRequest(v,pid);cancellableStatusChngIntrequestForCall.cancel();
          case v:Int if v!=5 =>connection ! UserToTcpResponsForCallRequest(v,pid);status.rollback;flagIntentWorkWithMe==false;if(cancellableStatusChngIntrequestForCall!=null)cancellableStatusChngIntrequestForCall.cancel();
        }
        setStatus(0)
        rUserM.getSecondUserRef ! UserToUserRequestSolutionForCall(pc,id)
      }else{connection ! UserToTcpResponsForCallRequest(3,pid)}
    }else {
      connection ! UserToTcpResponsForCallRequest(2,pid)
    }
  }

  def requestForCallFromUser(pr:Promise[Int],rid:String): Unit ={

    if(regContatcs.contains(rid)==false){pr.trySuccess(0); return}
    if(status.value==0||flagIntentWorkWithMe){
      pr.trySuccess(1);
      return
    }
    if(status.value==1&&flagIntentWorkWithMe==false){
      flagIntentWorkWithMe==true
//      status.value= 0
      pr.trySuccess(5);
      cancellableStatusChngIntrequestForCall= userActor.context.system.scheduler.scheduleOnce(GlobalContext.delayUserRollBackFree){/*status.rollback;*/this.flagIntentWorkWithMe==false}
      return
    }

  }
  def setConnection(connect: ActorRef): Unit ={
    connection=connect
    statusTo0Cancell.cancel()
//  if(toTcpMasageBuffer.isEmpty==false) {
//    for(a<-toTcpMasageBuffer){
//     connection ! a
//   }
//   }
   }



  def dropConection: Unit ={
    connection = null
    lastConnectDate.setTimeInMillis(System.currentTimeMillis())
    println("DROPCONNECTION IN USER")
    deviceType match {

    case 0=>
    statusTo0Cancell = userActor.context.system.scheduler.scheduleOnce(GlobalContext.delayUserStatusTo0){status.value=0;

    setStatus(0)}

    case 1=>
        setStatus(0);
        }
    }


  def statusRQfromInent(pr: Promise[Int]): Unit ={
    println("Запрос статуса из интента в при этом статус ="+status.value+" flag -"+flagIntentWorkWithMe+" в актёре "+id)
    if(flagIntentWorkWithMe/*&&status.value==1*/) pr.trySuccess(0)
      else {
        if (status.value==1) {
          pr.trySuccess(1)
          flagIntentWorkWithMe = true
          userActor.context.system.scheduler.scheduleOnce(GlobalContext.delayUserRollBackFree){this.flagIntentWorkWithMe = false}
            }else{
              if(connection==null&&System.currentTimeMillis()-lastConnectDate.getTimeInMillis<900000) {
                Utils.makePush
                currentIntentsPromis=pr
                }else pr.trySuccess(0)
        }
  }
 }

  def startWorkIntent(ic:IntentConteiner): Unit ={
    if(connection!=null)connection ! UserToTcpCallIntentINF(ic)
    setStatus(0)
    flagIntentWorkWithMe=false
  }

  def setStatus(statusm: Int): Unit = {
    println("изменяется статус в"+id+" "+status.value+"->"+statusm)
    def inform():Unit={
    flagConfigStatusEvent match{
      case 0=> seeingList.foreach(_._2 ! UserToUserMyStatusIsChange(id, status.value))
      case 1=> seeingList.foreach(_._2 ! UserToUserMyStatusIsChange(id, status.value));favoritList.foreach(_._2 ! UserToUserMyStatusIsChange(id, status.value))
      case 2=> regContatcs.foreach(_._2.getSecondUserRef ! UserToUserMyStatusIsChange(id, status.value));
    }
  }

   statusm match {
     case 0 /*if(status.value==1)*/=> status.value = statusm;inform;if(cancellableStatusChngIntrequestForCall!=null){cancellableStatusChngIntrequestForCall.cancel()}; cancellableStatusChngIntrequestForCall=null

     case 1 if(status.value==0)=> status.value = statusm;inform;flagIntentWorkWithMe=false;if(cancellableStatusChngIntrequestForCall!=null)cancellableStatusChngIntrequestForCall.cancel();cancellableStatusChngIntrequestForCall=null
     case 1/* if(status.value==0)*/=> status.value = statusm;inform;

     case _=>println(s" изменить статус из $status to $statusm")
   }
  }

  def setAllContats(list: Array[String], pid:Int): Unit = {
    log.info(id + "start Set All Contacts")
    contacts.clear()
    regContatcs.clear()

    val errorcontacts=ArrayBuffer[String]()
      for(a<-list if a.length==13&&a!=id ){
        contacts +=a
        val d = globalMap.get(a)
        if(d.isDefined){
          val key1=Utils.makeKeyFromUsersId(id,a)
          val key2=Utils.makeKeyFromUsersId(a,id)
          val md = metaMap get key1 orElse  metaMap.get(key2) orElse {val umd = new UsersMetaData(id, slf, a, d.get);;println(s"Создал мета in  setAllContats  из slf [$slf] and ["+d.get+"]");
            userManager ! UserToUserManagerAddMetadates(slf, umd);Some(umd)}
          regContatcs += a->md.get
        }
    }
    dao ! UserToBdSetContacts(id, contacts.toArray[String], regContatcs.keys)
    connection ! UserToTcpAfterSetContactsList(regContatcs.keys, errorcontacts, pid)
  }

  def removeContacts(list: Array[String], pid: Int): Unit = {
    contacts --= list
    regContatcs --= list
    favoritList --= list
    seeingList --= list
    dao ! UserToBdRemoveContacts(id, list)
    connection ! UserToTcpAfterRemoveContact(pid)
  }

  def addContacts(list: Array[String], pid: Int): Unit = {
//    log.debug("User ["+id + "] adding ["+list.length+"] contacts pid["+pid+"]")
    val rc: MMap[String, UsersMetaData] = MMap()
    val c = ArrayBuffer[String]()

    for(a<-list if a.length==13&&a!=id ) {
      c += a
      val d = globalMap.get(a)
      if (d.isDefined) {
        val key1 = Utils.makeKeyFromUsersId(id, a)
        val key2 = Utils.makeKeyFromUsersId(a, id)
        val md = metaMap get key1 orElse metaMap.get(key2) orElse {
          val umd = new UsersMetaData(id, slf, a, d.get);println(s"Создал мета in addContacts  из slf [$slf] and ["+d.get+"]");
          userManager ! UserToUserManagerAddMetadates(slf, umd);
          Some(umd)
        }
        rc += a -> md.get
      }
    }
    println("regContatcs size befor add contacts - "+regContatcs.size )
    regContatcs ++= rc
    println("regContatcs size after add contacts - "+regContatcs.size )
    contacts ++=c
    dao ! UserToBDAddContacts(id, c, rc.keys)
    connection ! UserToTcpAfterAddContacts(rc.keys, pid)
  }


  def newUserHaveRegisteredInSystem(idm:String, aRef:ActorRef): Unit ={
    if(contacts.contains(idm)){
      val key1=Utils.makeKeyFromUsersId(id,idm)
      val key2=Utils.makeKeyFromUsersId(idm,id)
      val rz=metaMap get key1 orElse  metaMap.get(key2) orElse {val umd = new UsersMetaData(id, slf, idm, aRef)
        userManager ! UserToUserManagerAddMetadates(slf, umd);println(s"Создал мета in newUserHaveRegisteredInSystem  из slf [$slf] and [$aRef]");Some(umd)}
      println("regContatcs size befor add new user - "+regContatcs.size)
      regContatcs.foreach(x=>println(x+" regContatcs befor add new user"))
      regContatcs +=(idm -> rz.get);
      println("regContatcs size after add new user - "+regContatcs.size)
      regContatcs.foreach(x=>println(x+" regContatcs after add new user"))
     if(connection!=null){
     connection ! UserToTcpYourContactHaveRegistr(idm)
     }
      dao ! UserToBdManagerAddRegContacs(id, idm)
    }
  }

  def setMetasFromUM(um: UsersMetaData) {
    println("regContatcs setMetas from UM size befor - "+regContatcs.size)
    if(regContatcs.contains(um.getSecondUserId(id)))regContatcs += um.getSecondUserId(id)->um
    println("regContatcs size after set metas - "+regContatcs.size)
  }


  def setFavoritList(list: Array[String], pid:Int): Unit = {
    log.info(id + " Seting FavoritList")
    favoritList.clear()
    val errorList = ArrayBuffer[String]()

    for (a <- list) {
      var d = regContatcs.getOrElse(a, null)
      if (d != null) {
      favoritList += (a -> d.getSecondUserRef)
      }else{
        errorList+=a
      }
    }
    dao ! UserToBDSetFavoritList(id, favoritList)
    connection ! UserToTcpAfterSetFavoritList(errorList, pid)
  }

  def removeFavorits(list: Array[String], pid: Int): Unit = {
    log.info(id + " remove from FavoritList")
    favoritList.--=(list)
    dao ! UserToBDRemoveFromFavoritList(id, list)
    connection ! UserToTcpAfterRemoveFavoritContact(pid)
  }

  def addFavorits(list: Array[String], pid: Int): Unit = {
    log.info(id + " adding Favorits")

    val errorList = ArrayBuffer[String]()
    val fl = MMap[String, ActorRef]()
    for (a <- list) {
      val d = regContatcs.getOrElse(a, null)
      if (d != null&&favoritList.contains(a)==false) {
        fl += (a -> d.getSecondUserRef)
      }else{errorList+=a}
      favoritList ++=fl

      dao ! UserToBDAddToFavoritList(id, fl.keys.toArray)
      connection ! UserToTcpAfterAddFavorites(errorList, pid)
    }

  }

    def setSeeings(list: Array[String], pid: Int): Unit = {
      log.info(id + " Seting SeeingList")
      seeingList.clear()
      val errorList = ArrayBuffer[String]()
      for (a <- list) {
        val d = regContatcs.getOrElse(a, null)
        if (d != null) {
          val f = (a -> d.getSecondUserRef)
          seeingList += f
          f._2 ! UserToUserMyStatusIsChange(id, status.value)
        }else{errorList+=a}
      }
      dao ! UserToBDSetSeeings(id, seeingList)
      connection ! UserToTcpAfterSetSeeingList(errorList, pid)
    }

    def removeSeeing(list: Array[String], pid: Int): Unit = {
      log.info(id + " remove from SeeingList")
      val sd=seeingList.filter(x=>list.contains(x._1))
      sd.foreach(x=>x._2 ! UserToUserMyStatusIsChange(id,0))
      seeingList --= list
      dao ! UserToBDRemoveFromSeeings(id, list)
      connection ! UserToTcpAfterRemoveSeeings(pid)
    }

  def configStatusEvent(conf:Int, pid:Int){flagConfigStatusEvent=conf;connection ! UserTotcpStatusEventConfRS(pid)}

    def addSeeings(list: Array[String], pid:  Int): Unit = {

      val errorList = ArrayBuffer[String]()
      val sl = MMap[String, ActorRef]()
      for (a <- list) {
        var d = regContatcs.getOrElse(a, null)
        if (d != null) {
          if (seeingList.contains(a) == false) {
            val f = (a -> d.getSecondUserRef)
            sl += f
            f._2 ! UserToUserMyStatusIsChange(id, status.value)
          } else {
            errorList += a
          }
        }
        if(sl.isEmpty==false){
        seeingList ++= sl
        dao ! UserToBDAddForSeeings(id, sl.keys.toArray)
        connection ! UserToTcpAfterAddSeeings(errorList, pid)
      }
      }
    }


    def hailStatusOfMyContact(pid: Int): Unit ={
      println("Slf "+slf+" id- "+id)
      println("regContatcs size befor hailStatus - "+regContatcs.size)
      regContatcs.foreach(x=>println("["+x._1+"]["+x._2+"] regContatcs in heail my ststus"))
      for (a<-regContatcs){
      println("["+a._1+"]["+a._2+"]regContatcs in loop hailStstus")
      a._2.getSecondUserRef ! UserToUserGetMeYourStatus(id, userActor.getMyActorRef())
    }
      connection ! UserToTcpAfterGetAllStatus(pid)
    }

  def load(intents:ArrayBuffer[IntentConteiner], regcs:Array[String], favoritcs:Array[String], seeingscs:Array[String]): Unit ={
    println("LOAD USER ")
  regcs.foreach(x=>
    regContatcs+=(x -> (metaMap.find(b=>b._2.isMy(id, x)) orElse {val umd = new UsersMetaData(id, slf, x, globalMap(x));println(s"Создал мета in load  из slf [$slf] and ["+globalMap(x)+"]");
      userManager ! UserToUserManagerAddMetadates(slf, umd);Some((x,umd))} ).get._2))
  favoritcs.foreach(x=>favoritList +=x->regContatcs(x).getSecondUserRef)
  seeingscs.foreach(x=>seeingList +=x->regContatcs(x).getSecondUserRef)


  val outics = intents.filter(_.isICreator(id))
  val inics = intents.filter(_.isIDestination(id))

    outics.foreach(x=>{val iRefs=userActor.context.system.actorOf(
      Intent.propsFromUser(x, metaMap.find(b=>b._2.isMy(x.idCreator, x.idDestination)).get._2, x.inedx ));x.iRef =iRefs })

    val a=outics.partition(_.isInRecycle)
    val b = inics.partition(_.isInRecycle)
    outgoingIntent ++= a._2.map(x=>x.iid->x)
    recycleOutgoingIntents ++= a._1.map(x=>x.iid->x)
    incomingIntent ++= b._2.map(x=>x.iid->x)
    recycleIncomingIntents ++= b._1.map(x=>x.iid->x)
    userManager ! UserToUserManagerLoadComplete()

  }




  def updMetas(l:Array[String],pid:Int): Unit ={
  val errorList=ArrayBuffer[String]()
    for(a<-l) {
      val ar=a.split(":")
      val idm=ar(0)
      val inCount=ar(1).toInt
      val inTime=ar(2).toInt
      val outCount=ar(3).toInt
      val outTime=ar(4).toInt
      val md = regContatcs.get(idm)
      if (md.isDefined){ md.get.update(id, outTime, outCount, inTime, inCount);
                         dao ! UserToBdManagerUpdateMetas(md.get);}
      else errorList +=idm
    }
    connection ! UserToTcpUpdMetasRS(errorList,pid)

  }

  def updIIndex(str: Array[String], pid: Int): Unit ={
    val errl=ArrayBuffer[String]()
    for(a<-str){
      val b = a.split(":")

      val key = Utils.makeKeyFromUsersId(id, b(0))
      if(incomingIntent.contains(key)){
      val intnt = incomingIntent(key)
        intnt.inedx.indx = b(1).toInt
        intnt.inedx.tf = b(2).toBoolean
        dao ! UserToBdManagerUpdIIndex(intnt)
      }else{
       errl += b(0)
      }

    }

    connection ! UserToTcpSetIIndexRS(errl, pid)
  }


  def addOutgoingIntents(str: Array[String], pid: Int): Unit ={
      val errorList = ArrayBuffer[String]()
      for(a<-str){
      val b = a.split("#")
        val rId= b(1)
        val timeToDie=b(2)
        val indx=b(3)
        val tf=b(4)
        val iid= Utils.makeKeyFromUsersId(b(0), b(1))
        if(b(0)==id && regContatcs.contains(b(1))&&outgoingIntent.contains(iid)==false&&incomingIntent.contains(Utils.makeKeyFromUsersId(b(1), b(0)))==false){
          val index =IIndex(indx.toInt, tf.toBoolean)
          val meta = regContatcs(rId)
          val ic = new IntentConteiner(iid, id, status, slf, rId, meta.getSecondUserRef, index, timeToDie)
          val iRefs = userActor.context.system.actorOf(Intent.propsFromUser(ic, meta, index ))
          ic.iRef=iRefs
          outgoingIntent += (iid -> ic)
          intentManager ! UserToIntentManagerAddIntent(ic)
          ic.aRefDestination ! UserToUserAddIncomingIntent(ic)

         }else{
          errorList+=a
        }
      }
      connection ! UserToTcpAfterAddIntents(errorList, pid)
    }

    def addIncomingIntent(a: IntentConteiner): Unit ={
      a.putDStatus(status)
      incomingIntent +=(a.iid -> a)
      if (connection != null){
      connection ! UserToTcpTakeIncomingIntent(a)
        a.synchronize=true
        dao ! UserToDbManagerMarkIntentsSynchronize(a)
      }
    }

  def getIncomingIntents(pid: Int): Unit = {
    connection ! UserToTcpTakeIncomingIntents(incomingIntent, pid)
    incomingIntent.values.filter(_.synchronize == false).foreach({ x => x.synchronize = true; dao ! UserToDbManagerMarkIntentsSynchronize(x)})
  }


def removeIntent(str: String, pid: Int): Unit ={
  val a = str.split("#")
  var bag:String = null


  if(id==a(0)){
    val i =outgoingIntent.getOrElse(Utils.makeKeyFromUsersId(a(0), a(1)), null)
    if(i!=null){
    outgoingIntent -=i.iid
    i.aRefDestination ! UserToUserRemoveIntent(i, 0)
  }else{bag=str}
}

  if(id==a(1)){
    val i =incomingIntent.getOrElse(Utils.makeKeyFromUsersId(a(0), a(1)), null)
    if(i!=null){
    incomingIntent -= i.iid
    i.aRefCreator ! UserToUserRemoveIntent(i, 1)
  }else{bag=str}
  }
  if(bag==null)bag="Всё ок"


  connection ! UserToTcpAfterRemoveIntent(bag, pid)
}

  def removeIntentUtU(a: IntentConteiner, inOut: Int): Unit ={

  inOut match {

    case 0 =>{
      incomingIntent -= a.iid
      if(connection != null){
      connection ! UserToTcpRemoveIntent(a)
      intentManager ! UserToIntentManagerRemoveIntent(a)
      }else{
      if(a.synchronize){
      dao ! UserToDbManagerMarkIntentsPreparetoremove(a, 0)
      recycleIncomingIntents += (a.iid->a)
      }else{
        intentManager ! UserToIntentManagerRemoveIntent(a)
      }
      }
      }
    case 1 =>{
         outgoingIntent -= a.iid
      if(connection != null){
         connection ! UserToTcpRemoveIntent(a)
         intentManager ! UserToIntentManagerRemoveIntent(a)
      }else{
        dao ! UserToDbManagerMarkIntentsPreparetoremove(a, 1)
        recycleOutgoingIntents+= (a.iid->a)
      }
      }
  }
  }



  def getIntentsFoRemovig(pid: Int): Unit ={
    if(recycleIncomingIntents.isEmpty==false || recycleOutgoingIntents.isEmpty==false){
      val mapForRemove = recycleIncomingIntents++=recycleOutgoingIntents
      connection ! UserToTcpIntentsForRemove(mapForRemove, pid)
      intentManager ! UserToIntentManagerRemoveIntents(mapForRemove)
    }else{
      connection ! UserToTcpIntentsForRemove(null, pid)
    }
  }

  def removeOldIntent(a: IntentConteiner, inOut: Int): Unit ={
    inOut match {

      case 0 =>{
        incomingIntent -= a.iid
        if(connection != null){
          connection ! UserToTcpRemoveIntent(a)
          }else{
          if(a.synchronize){
            recycleIncomingIntents += (a.iid->a)
          }
        }
      }
      case 1 =>{
        outgoingIntent -= a.iid
        if(connection != null){
          connection ! UserToTcpRemoveIntent(a)
        }else{
          recycleOutgoingIntents+= (a.iid->a)
        }
      }
    }
  }




}
