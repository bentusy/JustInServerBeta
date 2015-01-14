package attracti.develop.callalign.server.users

import akka.actor._
import attracti.develop.callalign.server.IntentManager
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
  user.calculator = context.actorOf(Props(new IntentsCalculator()), name = "IntentsCalculator")

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

    case IntentManagerToUserAddIntentToRecycle(int, inOut)=>{
      inOut match {
        case 0 => { user.recycleIncomingIntents += (int.id -> int)
        }
        case 1 => {user.recycleOutgoingIntents += (int.id -> int) }
      }
    }

    case IntentsCalculatorToUserCallRQ(i , p)=>{
      user.callToIntent(i, p)
    }

    case IntentsCalculatorToUserRQ(i, aref, p)=>{
      user.doYouReadyForTallck(i, aref, p)
    }

//    case CalculatorManagerToUserRequestForReadyToCall()=>{
//      user.doYouReadyForTallck(sender)
//    }

    case TcpToUserGetAllRegistredUsers(pid: Int)=>{
      connection ! UserToTcpTakeAllRegistredUsers(user.regContatcs, pid)
    }

    case UserManagerToUserInformAll()=>{
      informAllUserThatIhaveRegistr()
    }

    case UserManagerToUserSetAllContactList(contacts, favorits, seeings)=>{
      user.loadContacts(contacts, favorits, seeings)
    }

    case IntentManagerToUserAddIntent(int: Intent, inOut: Int)=>{
      inOut match {

        case 0 => { user.incomingIntent += (int.id -> int)
        }
        case 1 =>{user.outgoingIntent += (int.id -> int) }
      }

    }

    case UserToUserIHaveRegistred(id, aRef)=>{
    user.newUserHaveRegisteredInSystem(id, aRef)
    }

    case IntentManagerToUserRemoveOld(a, inOut) => {
    user.removeOldIntent(a, inOut)
    }

    case UserToUserRemoveOutIntent(a) => {
      user.outgoingIntent-=a.id
    }


    case IntentsCalculatorToUserFree(aref:ActorRef)=>{
    user.calculatorFree(aref)
  }


case UserToUserMyStatusIsChange(rid,status)=>{


  if (user.connection !=null&&user.regContatcs.contains(rid))  user.connection ! UserToTcpYourContactSetStatus(rid, status)
}


    case UserToUserGetMeYourStatus(rid, aRef)=>{

      if(user.seeingList.contains(rid)) aRef ! UserToUserMyStatusIsChange(user.id,user.status)
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
    log.info(user.id+ "Have drop ConnectionHendle")
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


    // AAAAAAAAAAAAAAAAAA~!!!!!!!!!!!!!!!!
    case TcpToUserAddNewContatcs(contacts, pid)=>{
//      log.info("AddNewContatc("+id+" ) in UserActor"+self.path)
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

    case UserToUserResponsForCall(callConteiner, answer)=>{
    user.connection ! UserToTcpResponsForCallRequest(callConteiner, answer); user.callReg -= callConteiner
    }

    case UserToUserRequestSolutionForCall(conteinar)=>{
      user.requestPermissionForCallFrom(conteinar)
    }

    case TcpToUserCanICallToUser(rUser, pid)=>{
    user.requestForCallFromTcp(rUser, pid)

    }

//    case UserToUserYouCanCallMe(i)=>{
//    if(user.connection != null){
//      user.connection ! UserToTcpYouCanCallForThisIntets(i)
//    }
//    }

    case UserToUserCanICallYouFromIntentsCreater(intent)=>{
      user.requestPermissionForCallWithFomIntentsCreater(intent)
    }

    case UserToUserTakeIncomingIntent(i)=>{
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