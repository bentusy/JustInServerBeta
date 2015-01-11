package attracti.develop.callalign.server.conections

import java.net.InetSocketAddress

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor._
import akka.io.Tcp
import akka.util.ByteString
import attracti.develop.callalign.server.GlobalContext
import attracti.develop.callalign.server.utill._
import ProtocolMessageDescribe._

import attracti.develop.callalign.server.utill.Utils._
import attracti.develop.callalign.server.utill._
import org.apache.logging.log4j.{LogManager, Logger}

import scala.collection.mutable.{ArrayBuilder => ARB, Map => MMap, Set => MSet, ArrayBuffer}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Properties.{lineSeparator => newLine}
import scala.util.{Failure, Success}

object TcpConnectionHandlerActor {
  def props(remote: InetSocketAddress, connection: ActorRef,  usermeneger: ActorRef): Props =
    Props(new TcpConnectionHandlerActor(remote, connection, usermeneger))
}

class TcpConnectionHandlerActor(remote: InetSocketAddress, connect: ActorRef, userMenege: ActorRef) extends Actor {

  var log:Logger =LogManager.getLogger("InfoLoger")
  val collect = new TransportProtocolCollection()
  val buffer=new Buffer(collect)
  val out=new OutgoinFormatTools()
  val pidReg = ArrayBuffer[Int]()

    var userid:String = null
    var useractor:ActorRef = null

    context.watch(connect)



  def send(arr: Array[Byte]): Unit ={
println()
    connect ! Tcp.Write(ByteString.fromArray(arr))
  }

  def receive(): Receive = {


//    case UserToTcpWillYouCallForThisIntent(intn, calc)=>{
//      val pkg = out.createPckgType0X2A(intn.toString())
//      send(pkg._1)
//      collect +=(pkg._2, new doSomething {
//        override def run(typ: Int): Unit = {
//          typ match {
//            case 0 =>
//            case 1 => calc ! UserToCalculatorAnswerForIntetnsRequest(0)
//            case 2 => calc ! UserToCalculatorAnswerForIntetnsRequest(1)
//          }
//        }
//        })
//    }


    case UserToTcpTakeAllRegistredUsers(map, pid)=>{
      send(out.createPckgType0x2b(pid, regUsers(map)))
    }

    case UserToTcpCloseConnection()=>{
      connect ! Tcp.Close
//      context.stop(connect)
//      context.stop(self)
    }

    case UserToTcpYourContactHaveRegistr(id)=>{
    send(out.createPckgType0x30("34/"+id+"/One of your contacts hase registred"))
    }

    case UserToTcpRemoveIntent(a)=>{
     send( out.createPckgType0x30("32/"+a.idCreator+"#"+a.idDestination))
    }

    case UserToTcpAfterRemoveIntent(a, pid)=>{
      if(pidReg.contains(pid)){
      send(out.createPckgType0x2b(pid, if(a==null || a =="") "113" else  a))}
    }

    case UserToTcpIntentsForRemove(list, pid)=>{
      if(pidReg.contains(pid)){
      send(out.createPckgType0x2b(pid, takeIntentsForRemoving(list)))
  }}

    case UserToTcpTakeIncomingIntents(listInt, pid)=>{
      if(pidReg.contains(pid)){
      send(out.createPckgType0x2b( pid, takeIncomingIntets(listInt)))
    }}

    case UserToTcpYouCanCallForThisIntets(i)=>{

//      val p = out.createPckgType4(youCanCallForThisIntent(i))
//      collect.+=(p._2, new doSomething(){
//        def run(typ: Int): Unit ={
//        typ match {
//          case 0=>
//        }
//        }
//      })
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    }

    case UserToTcpAfterAddIntents(errl, pid)=>{
      if(pidReg.contains(pid)){
      send(out.createPckgType0x2b(pid, addOutIntnts(errl)))
    }}

    case UserToTcpTakeIncomingIntent(i)=>{

      send(out.createPckgType0x30("28/"+i.toString()+"/Incoming intent"))
    }

    case UserToTcpResponsForCallRequest(callConteiner:CallConteoner, answer)=>{

      if(pidReg.contains(callConteiner.pid)){
      send(out.createPckgType0x2b(callConteiner.pid, ""+answer))
    }}

    case UserToTcpPing(callConteiner: CallConteoner)=>{
      import scala.concurrent.ExecutionContext.Implicits.global

     val para = out.createPckgType0X10()
      val ds = new doSomething {
        def run(typ: Int): Unit = {
        typ match {
          case 0=> context.stop(self);
          case 1=> callConteiner.arefFrom ! UserToUserResponsForCall(callConteiner, 5);
        }
        }
      }
      collect += (para._2, ds)


     send(para._1)

    }

    case UserToTcpYourContactSetStatus(rid, status)=>{

      send(out.createPckgType0x30("23/"+rid+":"+status+"/Your Contact set status"))
    }

    case UserToTcpAfterRemoveSeeings(pid)=>{
      if(pidReg.contains(pid)){
     send(out.createPckgType0x2b(pid,  "113"))}
    }

    case UserToTcpAfterAddSeeings(errl, pid)=>{
      if(pidReg.contains(pid)){
      send(out.createPckgType0x2b(pid,addSeeings(errl)))
    }}

    case UserToTcpAfterSetSeeingList(errl, pid)=>{
      if(pidReg.contains(pid)){
      send(out.createPckgType0x2b(pid, setSeeings(errl)))
    }}

    case UserToTcpAfterRemoveFavoritContact(pid)=>{
        if(pidReg.contains(pid)){

      send(out.createPckgType0x2b(pid, "113"))
    }}

    case UserToTcpAfterAddFavorites(erl, pid)=>{
      if(pidReg.contains(pid)){
      send(out.createPckgType0x2b(pid,addFavorits(erl)))
    }}

    case UserToTcpAfterSetFavoritList(errorcontacts, pid) =>{
      if(pidReg.contains(pid)){
      send(out.createPckgType0x2b(pid, setFavorits(errorcontacts)))
    }}

    case UserToTcpAfterRemoveContact(pid)=>{
      if(pidReg.contains(pid)){
      send(out.createPckgType0x2b(pid, "113"))
    }}

    case UserToTcpAfterSetContactsList(map, errorcontact, pid)=>{
      if(pidReg.contains(pid)){
      send(out.createPckgType0x2b(pid,contctsSetSuccessfuly(map, errorcontact) ))
    }}

    case UserToTcpAfterGetAllStatus(pid)=> {
      if (pidReg.contains(pid)) {
        send(out.createPckgType0x2b(pid, "113"))
      }
    }

     case UserManagerToTcpRespForAuthorization(id, aref, pkgid)=>{
       if(pidReg.contains(pkgid)){
      if (aref!=null){
        if(userid!=id){
      userid=id;
      if(useractor !=null)useractor ! TcpToUserNewConnection(null)
      useractor=aref;
      aref ! TcpToUserNewConnection(self)}

      send(out.createPckgType0x2b(pkgid, "12/Authorization successfull"))
      }else{
      send(out.createPckgType0x2b(pkgid, "0004/You must register first" ))
      }
    }}

    case UserManagerToTcpRegNewUser(id, aref, pkgId)=>{
      if(pidReg.contains(pkgId)){
      if(aref!=null){
      send(out.createPckgType0x2b(pkgId,"11/Registered successfull"))
    }else{
      send(out.createPckgType0x2b(pkgId,"0002/This id have already registered"))
      }
  }}



    case UserToTcpAfterAddContacts(regContacts, pid)=>{
      if(pidReg.contains(pid)){
      send(out.createPckgType0x2b(pid, nombersAdsdedSuccessfuly(regContacts)))

  }}

    case Tcp.Received(data) => {


      buffer.inComingData(data.toArray)

      while(buffer.hasNext()){

        val incomingData = buffer.getNext()

        incomingData.typ match {
          case 0 =>{send(out.createPckgType0x30("0002/Wrong data format" ))}

          case 0x11 =>{

            collect.respIncoming(incomingData.pid)
          }
          case 0x12 =>{

            if(useractor!=null) {

              val status = incomingData.dat(0).toInt
              if (status == 0 || status == 1) {
                useractor ! TcpToUserSetStatus(status)
              }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
             }else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
          }
          case 0x2a =>{
            val text = new String(incomingData.dat.toArray)
            val pkgId = incomingData.pid
            val cmd = text.split("/")
            log.info("Incoming from "+remote+" : "+text)

            cmd(0) match {

              case "60" => {
                if(cmd.length>1 && matchIt(cmd(1), patern60)&&cmd(1).length==14) {
                  val tempAr = cmd(1).split(":")
                  val countryCod = tempAr(0)
                  val phoneNumber= tempAr(1)
                  pidReg+=incomingData.pid
                  userMenege ! TcpToUserManagerRegisterUser(countryCod, phoneNumber, incomingData.pid)
                  log.info("TcpHandlerHelp send request for new user-" + countryCod+phoneNumber)
                }else{
                  send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))
                }
              }


              case "61" => {
                if(cmd.length>1 && matchIt(cmd(1), patern61)&& cmd(1).length==15) {
                  val idm= cmd(1).split(":")
                  pidReg+=incomingData.pid
                  userMenege ! TcpToUserManagerIsItReg(idm(0), idm(1).toInt, pkgId)
                  log.info("TCPhandler send autorisation for "+idm(0))
                }else{
                  send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))
                }
              }

              case "62" => {

                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern62_70)) {
                    val a = sliceIncomingListsDate(cmd(1))
                    pidReg+=incomingData.pid
                    useractor ! TcpToUserSetAllContactList(a, pkgId)
                    log.info("TCP send to UserActor set size =" + a.length)
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else{ send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }}

              case "63" => {
                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern62_70)) {
                    val a= sliceIncomingListsDate(cmd(1))
                    pidReg+=incomingData.pid
                    useractor ! TcpToUserAddNewContatcs(a, pkgId)
                    log.info("TCP send new contacts size"+a.length)
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }


              case "64" =>{
                if(cmd.length>1&&useractor!=null) {
                  if (matchIt(cmd(1), patern62_70)) {
                    val a=sliceIncomingListsDate(cmd(1))
                    pidReg+=incomingData.pid
                    useractor ! TcpToUserRemoveContacts(a, pkgId)
                    log.info("Tcp Send Contacts for removing="+a.length)
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }


              //          Get me allstatus of my contacts
              case "65" => {
                if(cmd.length>1&&useractor!=null) {
                  if (matchIt(cmd(1), patern62_70)) {
                    pidReg+=incomingData.pid
                    useractor ! TcpToUserSetFavoritContacts(sliceIncomingListsDate(cmd(1)), pkgId)
                    log.info("TCP set favoritList")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "66" => {
                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern62_70)) {
                    pidReg+=incomingData.pid
                    useractor ! TcpToUserAddFavoritContacts(sliceIncomingListsDate(cmd(1)), pkgId)
                    log.info(" ! TcpToUserGetCountAndHashContacts()")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "67" => {
                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern62_70)) {
                    val a = sliceIncomingListsDate(cmd(1))
                    pidReg+=incomingData.pid
                    useractor ! TcpToUserRemoveFavorits(a, pkgId)
                    log.info("Tcp send dell contacts size"+a.length)
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "68" => {
                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern62_70)) {
                    pidReg+=incomingData.pid
                    useractor ! TcpToUserSetSeeings(sliceIncomingListsDate(cmd(1)), pkgId)
                    log.info("Tcp send set Seeing ")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "69" => {
                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern62_70)) {
                    val a =sliceIncomingListsDate(cmd(1))
                    pidReg+=incomingData.pid
                    useractor ! TcpToUserAddSeeings(a, pkgId)
                    log.info("Tcp send add Seeings size"+a.length)
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "70" => {
                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern62_70)) {
                    val a = sliceIncomingListsDate(cmd(1))
                    pidReg+=incomingData.pid
                    useractor ! TcpToUserRemoveSeeings(a, pkgId)
                    log.info("Tcp send Remove Seeings size = "+a.length)
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "71" => {
                if(useractor!=null) {
                  if (cmd.length==1) {
                    pidReg+=incomingData.pid
                    useractor ! TcpToUserGetAllStatus(pkgId)
                    log.info("Tcp send get status all my contacts")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }


              case "74" => {
                if(useractor!=null) {
                  if (cmd.length==2&&matchIt(cmd(1), patern74)) {
                    pidReg+=incomingData.pid
                    useractor ! TcpToUserCanICallToUser(cmd(1), pkgId)
                    log.info("Tcp send Can I call to-"+cmd(1))
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "75" => {
                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern75)) {
                    val a = sliceIncomingListsDate(cmd(1))
                    pidReg+=incomingData.pid
                    useractor ! TcpToUserAddIntents(a, pkgId)
                    log.info("Tcp send outgoing Intents-")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "76" => {
                if(useractor!=null) {
                  if (cmd.length==1) {
                    pidReg+=incomingData.pid
                    useractor ! TcpToUserGetAllIncomingIntents(pkgId)
                    log.info("Tcp send get all incoming intets")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "78" => {

                if(useractor!=null) {
                  if (cmd.length==1) {
                    pidReg+=incomingData.pid
                    useractor ! TcpToUserGetAllIntentsForRemove(pkgId)
                    log.info("Tcp send get intents for remove")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "80" => {

                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern80)) {
                    pidReg+=incomingData.pid
                    useractor ! TcpToUserRemoveIntet(cmd(1), pkgId)
                    log.info("Tcp send RemoveIntent")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "86" => {

                if(useractor!=null) {
                  if (cmd.length==1) {
                    pidReg+=incomingData.pid
                    useractor ! TcpToUserGetAllRegistredUsers(pkgId)
                    log.info("Tcp send RemoveIntent")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case _ => {
                send(out.createPckgType0x30("0002/Wrong data format" ))

          }}}
          case 0x2b =>{

          }

          case 0x30 =>{

          }
        }
      }

    }

//    case _: Tcp.ConnectionClosed =>{
//      log.info("Stopping, conn for remote address {} closed" + remote)
//      context.stop(self)}

    case Terminated(connection) => {
      log.info("Stopping,conn from " + remote)
      context.stop(self)
    }

    case _ => {
      log.error("TCP DEFAULT")
  }
  }

  override def preStart() {
    val actorName = self.path
    log.info("Start new TCPHendler  with connection-"+remote)

  }
  override def postStop() {
    log.info("Stoped TCPHandlerAActor "+self.path)
//    useractor ! ConectionDrop
  }
  override def postRestart(reason: Throwable): Unit = {
    log.info("postRestart TCPHendler-"+self.path, reason) //preStart()
  }

///Позже разобраться с этим методом !!!!!!!!!!!!!
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
    case _: ArithmeticException      => Resume
    case _: NullPointerException     => Restart
    case _: IllegalArgumentException => Stop
    case _: Exception                => Escalate
  }
}



