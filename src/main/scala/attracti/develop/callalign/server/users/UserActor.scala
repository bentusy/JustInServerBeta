package attracti.develop.callalign.server.users

import akka.actor._
import attracti.develop.callalign.server.intents.{IntentManager, Intent}
import attracti.develop.callalign.server.utill._
import org.apache.logging.log4j.{LogManager, Logger}

import scala.collection.Map
import scala.collection.mutable.{ArrayBuilder => ARB, Map => MMap, Set => MSet}
import scala.util.Properties.{lineSeparator => newLine}

//class UserActor(idc: String, aRef: ActorRef,gmap: Map[String, ActorRef], deviceType: Int, intentManager: ActorRef, countryCod: String, dao: ActorRef) extends Actor {
class UserActor(usr: User) extends Actor {


  val log:Logger =LogManager.getLogger("InfoLoger")
//  val gmap=gma
//  val usermeneger = aRef
  var connection:ActorRef = null
//  var status = 0; // 1-всё ок
//  var tcpconn:ActorRef = null
//  var contacts:mutable.ArrayBuffer[Int]= mutable.ArrayBuffer()
  val user:User= usr
  user.userActor = this
  user.slf=self
  usr.ex=context.system.dispatcher

  def informAllUserThatIhaveRegistr(): Unit ={
   val g = user.globalMap
    for(a<-g){
      a._2 ! UserToUserIHaveRegistred(user.id, self)
    }
  }





  def getMyActorRef():ActorRef={
    self
  }

  override def preStart() {
    val actorName = self.path
    log.info("Start new UserActor "+actorName)
  }

  override def receive = {


    case TcpToUserSetIIndex(str, pid)=>user.setIIndex(str, pid)

    case TcpToUserSetMetas(str,pid)=>{
  user.updMetas(str,pid)
  }

    case TcpToUserConfigStatusEvent(conf, pid)=> user.configStatusEvent(conf, pid)

    case UserManagerToUserLoad(intents, regs, favor, seeing)=>user.load(intents, regs, favor, seeing)

    case IntentManagerToUserAddIntentToRecycle(int, inOut)=>{
      inOut match {
        case 0 => { user.recycleIncomingIntents += (int.iid -> int)
        }
        case 1 => {user.recycleOutgoingIntents += (int.iid -> int) }
      }
    }

    case TcpToUserGetAllRegistredUsers(pid: Int)=>{
      connection ! UserToTcpTakeAllRegistredUsers(user.regContatcs, pid)
    }


    case UserManagerToUserSetMetaInRegs(meta)=>{
      user.setMetasFromUM(meta)
  }

    case UserManagerToUserInformAll()=>{
      informAllUserThatIhaveRegistr()
    }

    case UserManagerToUserSetAllContactList(contacts, favorits, seeings)=>{
//      user.loadContacts(contacts, favorits, seeings)
    }

    case IntentManagerToUserAddIntent(int, inOut)=>{
      inOut match {
        case 0 => { user.incomingIntent += (int.iid -> int)
        }
        case 1 =>{user.outgoingIntent += (int.iid -> int) }
      }
    }

    case UserToUserIHaveRegistred(id, aRef)=>{
    user.newUserHaveRegisteredInSystem(id, aRef)
    }

    case IntentManagerToUserRemoveOld(a, inOut) => {
    user.removeOldIntent(a, inOut)
    }


    case UserToUserMyStatusIsChange(rid,status)=>{
      if (user.connection !=null&&user.regContatcs.contains(rid))  user.connection ! UserToTcpYourContactSetStatus(rid, status)
    }


    case UserToUserGetMeYourStatus(rid, aRef)=>{

      if(user.seeingList.contains(rid)) aRef ! UserToUserMyStatusIsChange(user.id,user.status.value)
      if(user.flagConfigStatusEvent==1&&user.favoritList.contains(rid))  aRef ! UserToUserMyStatusIsChange(user.id,user.status.value)
      if(user.flagConfigStatusEvent==2&&user.regContatcs.contains(rid))  aRef ! UserToUserMyStatusIsChange(user.id,user.status.value)

    }

    case TcpToUserNewConnection(aRef)=>{
      log.info(user.id+ " Get New Connection" )
      if(connection!=null){context.unwatch(connection);
      if(aRef!=null){connection ! UserToTcpCloseConnection()}
      }
      user.connection=aRef
      connection=aRef
      context.watch(connection)
      }

    case Terminated(connection) => {
    log.info(user.id+ "Have drop Connection")
      this.connection=null
    user.dropConection
        }


    case TcpToUserGetAllStatus(pid)=>{
//      log.info("TcpToUserGetAllStatus")
      user.hailStatusOfMyContact(pid)
    }


    case TcpToUserSetAllContactList(list, pid) =>{
//      log.info("TcpToUserSetAllContactList")
      user.setAllContats(list, pid)
    }

    case TcpToUserSetStatus(stat)=>{
//      log.info("Set Status in userActor-"+self.path+" to-"+stat)
    user.setStatus(stat)
    }


//chek
    case TcpToUserAddNewContatcs(contacts, pid)=>{
      user.addContacts(contacts, pid)
    }



    case UserToUserRemoveIntent(a, inOut)=>{
    user.removeIntentUtU(a,inOut)
    }

    case TcpToUserRemoveIntet(str, pid)=>{
    user.removeIntent(str, pid)
    }

    case TcpToUserGetAllIntentsForRemove(pid)=>{
      user.getIntentsFoRemovig(pid)
    }

    case TcpToUserGetAllIncomingIntents(pid)=>{
            user.getIncomingIntents(pid)
    }

    case TcpToUserAddIntents(list, pid)=>{
      user.addOutgoingIntents(list, pid)
    }


    case UserToUserRequestSolutionForCall(pr,rid)=>{
      user.requestForCallFromUser(pr, rid)
    }

    case TcpToUserCanICallToUser(rUser, pid)=>{
    user.requestForCallFromTcp(rUser, pid)

    }

    case IntentToUserGetMeYourStatusPR(prm)=>{
      user.statusRQfromInent(prm)
    }

    case IntentToUserCall(ic)=>{
    user.startWorkIntent(ic)
  }
    case IntentToUserFree()=>user.flagIntentWorkWithMe=false

//    case UserToUserYouCanCallMe(i)=>{
//    if(user.connection != null){
//      user.connection ! UserToTcpYouCanCallForThisIntets(i)
//    }
//    }

//    case UserToUserCanICallYouFromIntentsCreater(intent)=>{
//      user.requestPermissionForCallWithFomIntentsCreater(intent)
//    }

    case UserToUserAddIncomingIntent(i)=>{
    user.addIncomingIntent(i)
    }

    case TcpToUserRemoveSeeings(sc, pid)=>{
      user.removeSeeing(sc, pid)
    }

    case TcpToUserAddSeeings(sc, pid)=>{
      user.addSeeings(sc, pid)
    }

    case TcpToUserSetSeeings(sc, pid)=>{
      user.setSeeings(sc, pid)
    }

    case TcpToUserRemoveFavorits(fc, pid)=>{
      user.removeFavorits(fc, pid)
    }

    case TcpToUserSetFavoritContacts(fcontacts, pid) =>{
      user.setFavoritList(fcontacts, pid)

    }
    case TcpToUserAddFavoritContacts(fcontacts, pid)=>{
      user.addFavorits(fcontacts, pid)
    }

    case TcpToUserRemoveContacts(contacts, pid) => {
//      log.info("TcpToUserDellContact("+id+")")
      user.removeContacts(contacts, pid)
//      tcpconn ! UserToTcpConfirmDellContact(id)
    }
  }

}