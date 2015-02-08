package attracti.develop.callalign.server.conections

import java.net.InetSocketAddress

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor._
import akka.io.Tcp
import akka.io.Tcp.{ConfirmedClose, ErrorClosed, PeerClosed, ConnectionClosed}
import akka.util.ByteString
import attracti.develop.callalign.server.GlobalContext
import attracti.develop.callalign.server.utill._
import ProtocolMessageDescribe._

import attracti.develop.callalign.server.utill.Utils._
import attracti.develop.callalign.server.utill._
//import org.apache.logging.log4j.{LogManager, Logger}

import scala.collection.mutable.{ArrayBuilder => ARB, Map => MMap, Set => MSet, ArrayBuffer}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Properties.{lineSeparator => newLine}
import scala.util.{Failure, Success}

object TcpConnectionHandlerActor {
  def props(remote: InetSocketAddress, connection: ActorRef,  usermeneger: ActorRef): Props =
    Props(new TcpConnectionHandlerActor(remote, connection, usermeneger))
}

class TcpConnectionHandlerActor(remote: InetSocketAddress, connect: ActorRef, userMenege: ActorRef) extends Actor with ActorLogging {

//  var log:Logger =LogManager.getLogger("InfoLoger")
  val collect = new TransportProtocolCollection(this)
  val buffer=new Buffer(collect)
  val out=new OutgoinFormatTools()

    var userid:String = null
    var useractor:ActorRef = null

    context.watch(connect)



  def send(arr: Array[Byte]): Unit ={
log.debug("TcpConnection send ["+(if(arr(0)==0x2b){"pid-"+buffer.byteToShort(arr,1)+"]["+(new String(arr.slice(7, arr.length)))}else{if(arr(0)==0x30){new String(arr.slice(5, arr.length))}else{"type "+arr(0)}})+"]" +
  " to ["+remote+"]["+(if(useractor!=null){useractor.toString()/*.substring(useractor.toString().length-5)*/}else{"null"})+"]")

    connect ! Tcp.Write(ByteString.fromArray(arr))
  }

  def receive(): Receive = {

    case UserToTcpSetIIndexRS(erl, pid)=>send(out.createPckgType0x2b(pid, arrayToStringPrt(erl)))

    case UserToTcpUpdMetasRS(err,pid )=>
      send(out.createPckgType0x2b(pid, "113"))


    case UserToTcpCallIntentINF(ic)=>{
      val msg = out.createPckgType0x30("30/"+ic.idCreator+"#"+ic.idDestination)
      send(msg)
    }

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


    case  UserTotcpStatusEventConfRS(pid)=>send(out.createPckgType0x2b(pid,"113"))

    case UserToTcpRemoveIntent(a)=>{
     send( out.createPckgType0x30("32/"+a.idCreator+"#"+a.idDestination))
    }

    case UserToTcpAfterRemoveIntent(a, pid)=>{

      send(out.createPckgType0x2b(pid, if(a==null || a =="") "113" else  a))
    }

    case UserToTcpIntentsForRemove(list, pid)=>{

      send(out.createPckgType0x2b(pid, takeIntentsForRemoving(list)))
  }

    case UserToTcpTakeIncomingIntents(listInt, pid)=>{

      send(out.createPckgType0x2b( pid, takeIncomingIntets(listInt)))
    }


    case UserToTcpAfterAddIntents(errl, pid)=>{

      send(out.createPckgType0x2b(pid, addOutIntnts(errl)))
    }

    case UserToTcpTakeIncomingIntent(i)=>{

      send(out.createPckgType0x30("28/"+i.toString()+"/Incoming intent"))
    }

    case UserToTcpResponsForCallRequest(answer,pid)=>
        send(out.createPckgType0x2b(pid, answer.toString))

    case UserToTcpYourContactSetStatus(rid, status)=>{
      send(out.createPckgType0x30("23/"+rid+":"+status+"/Your Contact set status"))
    }

    case UserToTcpAfterRemoveSeeings(pid)=>
     send(out.createPckgType0x2b(pid,  "113"))

    case UserToTcpAfterAddSeeings(errl, pid)=>
      send(out.createPckgType0x2b(pid,addSeeings(errl)))

    case UserToTcpAfterSetSeeingList(errl, pid)=>
      send(out.createPckgType0x2b(pid, setSeeings(errl)))

    case UserToTcpAfterRemoveFavoritContact(pid)=>
      send(out.createPckgType0x2b(pid, "113"))

    case UserToTcpAfterAddFavorites(erl, pid)=>
      send(out.createPckgType0x2b(pid,addFavorits(erl)))

    case UserToTcpAfterSetFavoritList(errorcontacts, pid) =>
      send(out.createPckgType0x2b(pid, setFavorits(errorcontacts)))

    case UserToTcpAfterRemoveContact(pid)=>
      send(out.createPckgType0x2b(pid, "113"))

    case UserToTcpAfterSetContactsList(map, errorcontact, pid)=>
      send(out.createPckgType0x2b(pid,contctsSetSuccessfuly(map, errorcontact) ))

    case UserToTcpAfterGetAllStatus(pid)=>
        send(out.createPckgType0x2b(pid, "113"))

    case UserToTcpAfterAddContacts(regContacts, pid)=>

      send(out.createPckgType0x2b(pid, nombersAdsdedSuccessfuly(regContacts)))

     case UserManagerToTcpRespForAuthorization(id, aref, pkgid)=>{
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
    }

    case UserManagerToTcpRegNewUser(id, aref, pkgId)=>{
      if(aref!=null){
      send(out.createPckgType0x2b(pkgId,"11/Registered successfull"))
    }else{
      send(out.createPckgType0x2b(pkgId,"0002/This id have already registered"))
      }
  }




    case Tcp.Received(data) => {

      buffer.inComingData(data.toArray)

      while(buffer.hasNext()){

        val incomingData = buffer.getNext()

        log.debug("TcpConnection incoming ["+incomingData.typ+"]["+incomingData.pid+"]["+new String(incomingData.dat.toArray))
        incomingData.typ match {
          case 0 =>{send(out.createPckgType0x30("0002/Wrong data format" ))}

          case 0x10 =>{send(out.createPckgType0X11(incomingData.pid))}

          case 0x11 =>{
            if(collect.isContein(incomingData.pid)){
            println("Пришёл Понг от "+remote)
          collect.respIncoming(incomingData.pid)}else{
              send(out.createPckgType0x30("0002/Wrong data format" ))
            }
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
//            println(s"ид пакета в хендлера - "+incomingData.pid+" Incoming from "+remote+" : "+text)

            cmd(0) match {
              case "60" => {
                if(cmd.length>1 && matchIt(cmd(1), patern60)&&cmd(1).length==14) {
                  val tempAr = cmd(1).split(":")
                  val countryCod = tempAr(0)
                  val phoneNumber= tempAr(1)
                  userMenege ! TcpToUserManagerRegisterUser(countryCod, phoneNumber, incomingData.pid)
                  log.info("TcpHandlerHelp send request for new user-" + countryCod+phoneNumber)
                }else{
                  send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))
                }
              }


              case "61" => {
                if(cmd.length>1 && matchIt(cmd(1), patern61)&& cmd(1).length==15) {
                  val idm= cmd(1).split(":")
                  userMenege ! TcpToUserManagerIsItReg(idm(0), idm(1).toInt, pkgId)
                  log.info("TCPhandler send autorisation for "+idm(0))
                }else{
                  send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))
                }
              }

              case "62" => {

                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern62_70)) {
                    val a = cmd(1).split(",")
                    useractor ! TcpToUserSetAllContactList(a, pkgId)
                    log.info("TCP send to UserActor set size =" + a.length)
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else{ send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }}

              case "63" => {
                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern62_70)) {
                    val a= cmd(1).split(",")
                    useractor ! TcpToUserAddNewContatcs(a, pkgId)
                    log.info("TCP send new contacts size"+a.length)
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }


              case "64" =>{
                if(cmd.length>1&&useractor!=null) {
                  if (matchIt(cmd(1), patern62_70)) {
                    val a=sliceIncomingListsDate(cmd(1))
                    useractor ! TcpToUserRemoveContacts(a, pkgId)
                    log.info("Tcp Send Contacts for removing="+a.length)
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }


              //          Get me allstatus of my contacts
              case "65" => {
                if(cmd.length==2&&useractor!=null) {
                  if (matchIt(cmd(1), patern62_70)) {
                    useractor ! TcpToUserSetFavoritContacts(sliceIncomingListsDate(cmd(1)), pkgId)
                    log.info("TCP set favoritList")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "66" => {
                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern62_70)) {
                    useractor ! TcpToUserAddFavoritContacts(sliceIncomingListsDate(cmd(1)), pkgId)
                    log.info(" ! TcpToUserGetCountAndHashContacts()")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "67" => {
                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern62_70)) {
                    val a = sliceIncomingListsDate(cmd(1))
                    useractor ! TcpToUserRemoveFavorits(a, pkgId)
                    log.info("Tcp send dell contacts size"+a.length)
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "68" => {
                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern62_70)) {
                    useractor ! TcpToUserSetSeeings(sliceIncomingListsDate(cmd(1)), pkgId)
                    log.info("Tcp send set Seeing ")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "69" => {
                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern62_70)) {
                    val a =sliceIncomingListsDate(cmd(1))
                    useractor ! TcpToUserAddSeeings(a, pkgId)
                    log.info("Tcp send add Seeings size"+a.length)
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "70" => {
                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern62_70)) {
                    val a = sliceIncomingListsDate(cmd(1))
                    useractor ! TcpToUserRemoveSeeings(a, pkgId)
                    log.info("Tcp send Remove Seeings size = "+a.length)
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "71" => {
                if(useractor!=null) {
                  if (cmd.length==1) {
                    useractor ! TcpToUserGetAllStatus(pkgId)
                    log.info("Tcp send get status all my contacts")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }


              case "74" => {
                if(useractor!=null) {
                  if (cmd.length==2&&matchIt(cmd(1), patern74)) {
                    useractor ! TcpToUserCanICallToUser(cmd(1), pkgId)
                    log.info("Tcp send Can I call to-"+cmd(1))
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "75" => {
                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern75)) {
                    val a = sliceIncomingListsDate(cmd(1))
                    useractor ! TcpToUserAddIntents(a, pkgId)
                    log.info("Tcp send outgoing Intents-")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "76" => {
                if(useractor!=null) {
                  if (cmd.length==1) {
                    useractor ! TcpToUserGetAllIncomingIntents(pkgId)
                    log.info("Tcp send get all incoming intets")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "78" => {

                if(useractor!=null) {
                  if (cmd.length==1) {
                    useractor ! TcpToUserGetAllIntentsForRemove(pkgId)
                    log.info("Tcp send get intents for remove")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "80" => {

                if(useractor!=null) {
                  if (cmd.length>1&&matchIt(cmd(1), patern80)) {
                    useractor ! TcpToUserRemoveIntet(cmd(1), pkgId)
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "85" => {

                if(useractor!=null) {
                  if (cmd.length>=1) {
                    useractor ! TcpToUserGetAllRegistredUsers(pkgId)

                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "86" => {
                if(useractor!=null) {
                  if (cmd.length==2&&matchIt(cmd(1), patern86)) {
                    useractor ! TcpToUserConfigStatusEvent(cmd(1).toInt, pkgId)
                    log.info("Tcp send ConfigStatusEvent")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "87" => {
                if(useractor!=null) {
                  if (cmd.length==1&&matchIt(cmd(1), patern87)) {
                    useractor ! TcpToUserSetMetas(cmd(1).split(","), pkgId)
                    log.info("Tcp send SetMetas")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

              case "88" => {
                if(useractor!=null) {
                  if (cmd.length==1&&matchIt(cmd(1), patern88)) {
                    useractor ! TcpToUserSetIIndex(cmd(1).split(","), pkgId)
                    log.info("Tcp send SetIIndex")
                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
              }

//              case "89" => {
//                if(useractor!=null) {
//                  if (cmd.length==1&&matchIt(cmd(1), patern88)) {
//                    useractor ! TcpToUserSetIIndex(cmd(1).split(","), pkgId)
//                    log.info("Tcp send SetIIndex")
//                  }else{send (out.createPckgType0x2b(incomingData.pid, "0002/Wrong data format"))}
//                } else send (out.createPckgType0x2b(incomingData.pid, "0001/You must authorization first"))
//              }


              case _ => {
                send(out.createPckgType0x30("0002/Wrong data format" ))

          }}}
          case 0x2b =>{
            if(collect.isContein(incomingData.pid)){
              println(s"ид пакета в хендлера - "+incomingData.pid+" Incoming from "+remote)
              collect.getAndRemove(incomingData.pid).run(incomingData.dat(0))
            }else{
              send(out.createPckgType0x30("0002/Wrong data format" ))
            }
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
      log.info("Stopping,conn from " + remote+"  "+connection)
      context.stop(self)
    }
//    case  ConfirmedClose=>("ConfirmedClose")
//    case  ErrorClosed=>("Eror closed")
    case  PeerClosed=>println("PeerClosed ")
    case _: ConnectionClosed => println("ConnectionClosed ")

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


  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
    case _: ArithmeticException      => Resume
    case _: NullPointerException     => Restart
    case _: IllegalArgumentException => Stop
    case _: Exception                => Escalate
  }
}



