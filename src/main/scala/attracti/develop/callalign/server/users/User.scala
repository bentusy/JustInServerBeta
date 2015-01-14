package attracti.develop.callalign.server.users

import akka.actor.ActorRef
import attracti.develop.callalign.server.{GlobalContext, IntentManager}
import attracti.develop.callalign.server.utill._
import org.apache.logging.log4j.{LogManager, Logger}
import akka.pattern.{ ask, pipe }

import scala.collection.{mutable, Map}
import scala.collection.mutable.{Map => MMap, TreeSet, ArrayBuffer}
import scala.concurrent.{Promise, Future}
import scala.util.{Failure, Success}


class User(val id: String, val globalMap:Map[String, ActorRef], val intentManager: ActorRef, val countryCod:String, val dao: ActorRef) {

  val log: Logger = LogManager.getLogger("InfoLoger")

  var deviceType:Int=0
  var userActor:UserActor=null


  var connection:ActorRef=null
  var status = 0;
  var contacts: ArrayBuffer[String] = ArrayBuffer()
  val regContatcs: MMap[String, ActorRef] = MMap()
  val favoritList: MMap[String, ActorRef] = MMap()
  val seeingList: MMap[String, ActorRef] = MMap()
  val incomingIntent=  MMap[String, Intent]()
  val outgoingIntent= MMap[String, Intent]()
  val recycleIncomingIntents= MMap[String, Intent]()
  val recycleOutgoingIntents= MMap[String, Intent]()
  var calculator:ActorRef = null

  val callReg=ArrayBuffer[CallConteoner]()
  val calculatorCaptureQueue = mutable.Queue[ActorRef]()
  var calculatorCapture:ActorRef=null
  var futureCapture:Future[TcpToUserReadyToCallRS]=null
  var isCall= false


  def calculatorFree(aref:ActorRef): Unit ={
    if(calculatorCapture==aref){
      calculatorCapture==null;
      futureCapture==null;

    }
  }


  def callToIntent(i: Intent, p1: Promise[UserToIntentCalculatorCallRS]): Unit = {
    implicit val ec = userActor.context.system.dispatcher
    implicit val sys = userActor.context.system
    println("ВЫЗОВ ФУНКЦИИ callToInten")

    if(connection==null){
      p1.success(UserToIntentCalculatorCallRS(i, 1))
    }
    val p = Promise[TcpToUserCallIntentRS]()
    TimeoutScheduler.startTimout(p1, GlobalContext.timeToWeightCallIntentAnswer)
    val f = p.future
    f.onSuccess{
      case TcpToUserCallIntentRS(i, okNo)=>
        println("В ЮЗЗЕРА ПРИШЁЛ ОТВЕТ ОТ ТСП ЗАПРОСА НА ЗВОНОК")
        p1.success(UserToIntentCalculatorCallRS(i, okNo))
    }

    f.onFailure{
      case ex:Throwable=>println("ЗАКОНЧИЛОСЬ ПО ТАЙМАУТУ ОЖИДАНИЕ ОТВЕТА ЗАПРОСА НА ЗВОНОК В ЮЗЕРЕ");
        p1.success(UserToIntentCalculatorCallRS(i, 1))
      case _=>println("Пришла некая ХРень")
    }

    connection ! UserToTcpCallIntentRQ(i, p)
  }


  def doYouReadyForTallck(i: Intent, ref: ActorRef, p:Promise[UserToIntentCalcultorRS]): Unit = {
    println("Пришол запрос от калькулятора в Юзера "+id+" первая проверка "+(connection==null)+" 2- "+(calculatorCapture!=ref)+""+(status!=1))
    implicit val ec = userActor.context.system.dispatcher
    implicit val sys = userActor.context.system
    if(connection==null){ ref ! UserToIntentCalcultorRS(1, i); return;}

    if(calculatorCapture!=null&&calculatorCapture!=ref){ calculatorCaptureQueue += ref; return }

    if(status!=1){ref ! UserToIntentCalcultorRS(1, i)}else {

      calculatorCapture = ref
      val p1: Promise[TcpToUserReadyToCallRS] = Promise()
      TimeoutScheduler.startTimout(p1, GlobalContext.timeIntentRQToTCP)
      val f = p1.future
      futureCapture = f

      f.onSuccess { case TcpToUserReadyToCallRS(okNo, i, aref) if (f == futureCapture) =>
        okNo match {
          case 0 => p.success(UserToIntentCalcultorRS(okNo, i));println("Пришёл ответ от ТСР в юзера "+id)
          case 1 => p.success(UserToIntentCalcultorRS(okNo, i));println("Пришёл ответ от ТСР в юзера "+id)
            calculatorCapture = null
            setStatus(0)
        }
        futureCapture = null
      }

      f.onFailure{
        case ex:Throwable if (f==futureCapture)=>{
          calculatorCapture ! UserToIntentCalcultorRS(0, i)
        }
      }

      connection ! UserToTcpReadyToCallRQ(i, p1, ref)
    }
  }


  def requestForCallFromTcp(rUser: String, pid: Int): Unit = {

    implicit val ec = userActor.context.system.dispatcher
    implicit val timeout = GlobalContext.timeIntentRQToTCP
    val rUserRef =regContatcs.getOrElse(rUser, null)
    if(rUserRef!=null){
      val cl = CallConteoner( id, userActor.self, rUser, rUserRef, pid)
      rUserRef ! UserToUserRequestSolutionForCall(cl)
      callReg +=cl

      val dellF = Future {
        Thread.sleep(GlobalContext.timeToCallRequest)
        cl}

      dellF.onSuccess{
      case a=>if(callReg.contains(a)){connection ! UserToTcpResponsForCallRequest(a, 1);callReg-=a;}
      }


    }else {
      connection ! UserToTcpResponsForCallRequest(CallConteoner( id, userActor.self, rUser, rUserRef, pid), 2)
    }
  }

  def setConnection(connect: ActorRef): Unit ={
    connection=connect
//  if(toTcpMasageBuffer.isEmpty==false) {
//    for(a<-toTcpMasageBuffer){
//     connection ! a
//   }
//   }
   }



  def dropConection: Unit ={
    connection = null
    deviceType match {
      case 1=> setStatus(0);
      calculatorCapture ! UserToIntensCalculateManagerStop(userActor.self)
      calculatorCapture==null
      futureCapture=null
    case 0=> setStatus(0);
/////Здесь будет куча логики
        }

    }






  def setStatus(statusm: Int): Unit = {
    log.info(id + " setStatus to '" + statusm+"'")

    def inform= for (a <- seeingList) {
    a._2 ! UserToUserMyStatusIsChange(id, status)
    }

   statusm match {
     case 0 if(status==1)=> status = statusm;inform;
     case 1 if(status==0)=> status = statusm;inform;
       if(calculatorCapture==null&&outgoingIntent.isEmpty==false) {calculator ! UserToIntensCalculateManagerStart(incomingIntent, outgoingIntent);
         calculatorCapture=calculator}
     case _=>println(s" изменить статус из $status to $status")
   }
  }


  def loadContacts(list: Array[String], favorits: Array[String], seeings: Array[String]): Unit ={
    for(a<-list){
      var b:String=null
      if(a.length<=9){
        b = a+countryCod }else{
        b = a
      }
      contacts +=b
      val d = findUserInSystem(a)
      if (d != null) {
        regContatcs += (a -> d)
      }
    }

      for (a <- favorits) {
        var d = regContatcs.getOrElse(a, null)
        if (d != null) {
          favoritList += (a -> d)
        }
      }

      for (a <- seeings) {
        var d = regContatcs.getOrElse(a, null)
        if (d != null) {
          seeingList += (a -> d)
        }
      }

  }

  def setAllContats(list: Array[String], pid:Int): Unit = {
    log.info(id + "start Set All Contacts")
    contacts.clear()
    regContatcs.clear()

    val errorcontacts=ArrayBuffer[String]()
      for(a<-list){

        var b:String=null
        if(a.length==13){
          b = if(a!=id){a}else{null}
          }else{
          if(a.length+countryCod.length==13){
            val temp = a+countryCod
        b = if(temp!=id){temp}else{null}
          }else{
            errorcontacts+=a
          }
        }

        if(b!=null){
        contacts +=b
        val d = findUserInSystem(a)
        if (d != null) {
          regContatcs += (a -> d)
        }
        }

    }
    dao ! UserToBdSetContacts(id, contacts.toArray[String], regContatcs)
    connection ! UserToTcpAfterSetContactsList(regContatcs, errorcontacts, pid)
    log.info(id+"End seting contact Length = "+ contacts.length + "  RC.length = "+ regContatcs.size+"\n")
  }

  def removeContacts(list: Array[String], pid: Int): Unit = {
    log.info(id + "Start Remove from Contact")
    contacts --= list
    regContatcs --= list
    favoritList --= list
    seeingList --= list
    dao ! UserToBdRemoveContacts(id, list)
    connection ! UserToTcpAfterRemoveContact(pid)
    log.info(id+"End Remove contacts Length = "+ contacts.length + " RC.length= "+ regContatcs.size+"\n")
  }

  def addContacts(list: Array[String], pid: Int): Unit = {
    log.info(id + " Start add contacts")
    val rc: MMap[String, ActorRef] = MMap()
    val c = ArrayBuffer[String]()

    for (a <- list) {
      if(contacts.contains(a)==false){

      var d:String=null;
      if(a.length<=9){ d =(a+countryCod) }else {d=a}
      c += d
      val aref = findUserInSystem(d)
        if (aref != null) {
        rc += (d -> aref);
      }
    }
    }
    regContatcs ++= rc
    contacts ++=c
    dao ! UserToBDAddContacts(id, c, rc)
    connection ! UserToTcpAfterAddContacts(rc, pid)
    log.info(id+"End add contacts Length = "+ contacts.length + "RC.length="+ regContatcs.size)
  }

  def newUserHaveRegisteredInSystem(idm:String, aRef:ActorRef): Unit ={
    if(contacts.contains(idm)){
     regContatcs+=(idm -> aRef)
     if(connection!=null){
     connection ! UserToTcpYourContactHaveRegistr(idm)
     }else{
//     toTcpMasageBuffer += UserToTcpYourContactHaveRegistr(idm)
    }
      dao ! UserToBdManagerAddRegContacs(id, idm)
    }
  }

  def setFavoritList(list: Array[String], pid:Int): Unit = {
    log.info(id + " Seting FavoritList")
    favoritList.clear()
    val errorList = ArrayBuffer[String]()

    for (a <- list) {
      var d = regContatcs.getOrElse(a, null)
      if (d != null) {
      favoritList += (a -> d)
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
        fl += (a -> d)
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
          seeingList += (a -> d)
        }else{errorList+=a}
      }
      dao ! UserToBDSetSeeings(id, seeingList)
      connection ! UserToTcpAfterSetSeeingList(errorList, pid)
    }

    def removeSeeing(list: Array[String], pid: Int): Unit = {
      log.info(id + " remove from SeeingList")
      seeingList --= list
      dao ! UserToBDRemoveFromSeeings(id, list)
      connection ! UserToTcpAfterRemoveSeeings(pid)
    }

    def addSeeings(list: Array[String], pid:  Int): Unit = {
      if(id=="+380995450043"){
      log.info(id + " add Favorits")}
      val errorList = ArrayBuffer[String]()
      val sl = MMap[String, ActorRef]()
      for (a <- list) {
        var d = regContatcs.getOrElse(a, null)
        if (d != null && seeingList.contains(a)==false) {
          sl += (a -> d)
        }else{errorList+=a}
      }
      seeingList ++=sl
      dao ! UserToBDAddForSeeings(id, sl.keys.toArray)
      connection ! UserToTcpAfterAddSeeings(errorList, pid)
      }

    def findUserInSystem(idm: String): ActorRef = {
 //     log.info(" findUserInSystem=" + id)
      globalMap.getOrElse(idm, null)
    }


    def hailStatusOfMyContact(pid: Int): Unit ={
    for (a<-regContatcs){
      a._2 ! UserToUserGetMeYourStatus(id, userActor.getMyActorRef())
    }
      connection ! UserToTcpAfterGetAllStatus(pid)
    }

    def getMeYourStatusIfICanSeeIt(intrestedUserid: String, intrestedUser: ActorRef): Unit ={
      if(seeingList.contains(intrestedUserid)) intrestedUser ! UserToUserMyStatusIsChange(id,status)
    }

    def ifThisUserCanSeeMeSendStatus(idm: String, aRef: ActorRef): Unit ={
     val a= seeingList.getOrElse(idm, null)
     if(a!=null){aRef ! UserToUserMyStatusIsChange( id, status)}
    }

   def requestPermissionForCallFrom(callObject:CallConteoner): Unit ={

     if(regContatcs.contains(callObject.idFrom)==false){callObject.arefFrom ! UserToUserResponsForCall(callObject, 0); return}
  if(status==0|| connection==null){
    callObject.arefFrom ! UserToUserResponsForCall(callObject, 1)
    return
  }
   if(connection!=null){
   connection ! UserToTcpPing(callObject)
   }
  }

  def requestPermissionForCallWithFomIntentsCreater(intnt: Intent): Unit ={
    if(status==0||regContatcs.contains(intnt.idCreator)==false||connection==null){
      intnt.aRefCreator ! UserToUserYouCanCallMe(intnt)
    }else{intnt.aRefCreator ! UserToUserYouCanCallMe(intnt)}
  }

    def addOutgoingIntents(str: Array[String], pid: Int): Unit ={
      val errorList = ArrayBuffer[String]()
      for(a<-str){
        val b = a.split("#")
        if(b(0)==id && regContatcs.contains(b(1))&&outgoingIntent.contains(Utils.makeKeyFromIntetsUsers(b(0), b(1)))==false){
          val i = new Intent(b(0), findUserInSystem(b(0)), b(1), findUserInSystem(b(1)), b(2))
          outgoingIntent += (i.id -> i)
          intentManager ! UserToIntentManagerAddIntent(i)
          i.aRefDestination ! UserToUserTakeIncomingIntent(i)
        }else{
          errorList+=a
        }
      }
      connection ! UserToTcpAfterAddIntents(errorList, pid)
    }


    def getIncomingIntents(pid: Int): Unit ={
      connection ! UserToTcpTakeIncomingIntents(incomingIntent, pid)
      for(a<-incomingIntent){a._2.synchronize=true;dao ! UserToDbManagerMarkIntentsSynchronize(a._2)}
    }

    def addIncomingIntent(a: Intent): Unit ={
      if (connection != null){
      connection ! UserToTcpTakeIncomingIntent(a)
        a.synchronize=true
        println("синхронизировал исходящий интент с ид- "+a.id+" в "+a.synchronize)
        dao ! UserToDbManagerMarkIntentsSynchronize(a)
      }
      incomingIntent +=(a.id -> a)
    }





def removeIntent(str: String, pid: Int): Unit ={
  val a = str.split("#")
  var bag:String = null

  if(id==a(0)){
    val i =outgoingIntent.getOrElse(Utils.makeKeyFromIntetsUsers(a(0), a(1)), null)
    if(i!=null){
    outgoingIntent -=i.id
    i.aRefDestination ! UserToUserRemoveIntent(i, 0)
    if(calculatorCapture!=null) calculatorCapture ! UserToIntentCalcultorRemoveIntent(i)
  }else{bag=str}
}

  if(id==a(1)){
    val i =incomingIntent.getOrElse(Utils.makeKeyFromIntetsUsers(a(0), a(1)), null)
    if(i!=null){
    incomingIntent -= i.id
    i.aRefCreator ! UserToUserRemoveIntent(i, 1)
  }else{bag=str}
  }

  connection ! UserToTcpAfterRemoveIntent(bag, pid)
}

  def removeIntentUtU(a: Intent, inOut: Int): Unit ={

  inOut match {

    case 0 =>{
      incomingIntent -= a.id
      if(connection !=null){
      connection ! UserToTcpRemoveIntent(a)
      intentManager ! UserToIntentManagerRemoveIntent(a)
      }else{
      if(a.synchronize){
      dao ! UserToDbManagerMarkIntentsPreparetoremove(a, 0)
      recycleIncomingIntents += (a.id->a)
      }else{
        intentManager ! UserToIntentManagerRemoveIntent(a)
      }
      }

      }
    case 1 =>{

      outgoingIntent -= a.id
      if(connection !=null){
        connection ! UserToTcpRemoveIntent(a)
        intentManager ! UserToIntentManagerRemoveIntent(a)
      }else{
        dao ! UserToDbManagerMarkIntentsPreparetoremove(a, 1)
        recycleOutgoingIntents+= (a.id->a)
      }
      if(calculatorCapture!=null) calculatorCapture ! UserToIntentCalcultorRemoveIntent(a)

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

  def removeOldIntent(intent: Intent, inOut: Int): Unit ={
  inOut match {
    case 0 => incomingIntent -=intent.id; recycleIncomingIntents -=intent.id
    case 1 => outgoingIntent -=intent.id; recycleOutgoingIntents -=intent.id
  }
  }



}
