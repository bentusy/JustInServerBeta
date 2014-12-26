package attracti.develop.callalign.server.utill

import akka.actor.ActorRef
import attracti.develop.callalign.server.users.{ProtoIntent, User, Intent}

import scala.collection.Map
import scala.collection.IndexedSeq
import scala.collection.mutable.ArrayBuffer

/**
 * Created by User on 19.11.2014.
 */

//Container for incoming buffered data
case class IncomingData(typ: Int, dat: Seq[Byte], pid:Int=0)
//Container for Call Reguest operation
case class CallConteoner(idFrom: String, arefFrom: ActorRef, idFor: String, arefFor: ActorRef, pid:Int)

case class TcpToUserManagerRegisterUser(coutryCod: String, phone:String, pkgId:Int)


case class TcpToUserManagerIsItReg(id: String, devType: Int, pkgId: Int)

case class UserManagerToTcpRespForAuthorization(id: String, aref: ActorRef, pkgId:Int)

case class UserManagerToTcpRegNewUser(id: String, aref: ActorRef, pkgId: Int)

case class TcpToUserNewConnection(aref: ActorRef) 

case class TcpToUserSetStatus(status: Int)


case class MyContactsSizeResp(size: Int, hash: Int) 

case class TcpToUserAddNewContatcs(contacts: Array[String], pid: Int)

case class TcpToUserSetAllContactList(contatcs: Array[String], pid: Int)

case class UserToUserMyStatusIsChange(id: String, status: Int) 

case class UserToTcpAfterAddContacts(rc:Map[String, ActorRef], pid: Int)

//case class UserToTcpSetStatusSuccessful(status: Int, pid: Int)

case class UserToUserYouHaveCorrectCall(id: Int, inOut: Int) 

case class TcpToUserRemoveContacts(contacts: Array[String], pid: Int)

case class TcpToUserGetAllStatus(pid: Int)

case class UserToTcpAfterSetContactsList(regContatcs: Map[String, ActorRef], errorcontacts: ArrayBuffer[String], pid:Int)
case class UserToUserGetMeYourStatus(id: String, aRef: ActorRef)
case class UserToIntentManagerAddIntent(intent: Intent)
case class DeamonToIntentManagerClean()
case class UserToIntentManagerRemoveIntent(intnt: Intent)
case class IntentManagerToUserRemoveOld(intnt: Intent, inOut: Int)
case class UserToUserRemoveOutIntent(a: Intent)
case class UserToTcpIntentsForRemove(intents: Map[String,Intent], pid: Int)
case class UserToIntentManagerRemoveIntents(intents: Map[String,Intent])
case class UserToTcpAfterGetAllStatus(pid: Int)
case class UserToTcpAfterRemoveContact(pid: Int)
case class TcpToUserSetFavoritContacts(fContacts: Array[String], pid: Int)
case class UserToTcpAfterSetFavoritList(errorList: ArrayBuffer[String], pid: Int)
case class TcpToUserAddFavoritContacts(fcontacts: Array[String], pid: Int)
case class UserToTcpAfterAddFavorites(errorList: ArrayBuffer[String], pid:Int)
case class TcpToUserRemoveFavorits(fcontacts: Array[String], pid: Int)
case class UserToTcpAfterRemoveFavoritContact(pid: Int)
case class TcpToUserSetSeeings(scontacts: Array[String], pid: Int)
case class UserToTcpAfterSetSeeingList(errorList: ArrayBuffer[String], pid: Int)
case class TcpToUserAddSeeings(sc: Array[String], pid:Int)
case class UserToTcpAfterAddSeeings(errorList: ArrayBuffer[String], pid: Int)
case class TcpToUserRemoveSeeings(sc: Array[String], pid: Int)
case class UserToTcpAfterRemoveSeeings(pid: Int)
case class UserToTcpYourContactSetStatus(rid: String, status: Int)
case class TcpToUserCanICallToUser(rUser: String, pid:Int)
case class UserToUserRequestSolutionForCall(callSolution:CallConteoner)//(objec: ReguestForCall)//(id: String, selfRef: ActorRef, pid: Int)
case class UserToUserResponsForCall(callSolution:CallConteoner, answer: Int)//(id: String, canOrNo: Int, pid: Int)
case class UserToTcpResponsForCallRequest(callSolution:CallConteoner, answe: Int)//(id: String, canOrNo: Int, pid: Int)
case class UserToUserTakeIncomingIntent(i: Intent)
case class UserToTcpTakeIncomingIntent(i: Intent)
case class UserToTcpTakeIncomingIntents(i: Map[String, Intent], pid: Int)
case class UserToTcpAfterAddIntents(errors: ArrayBuffer[String], pid:Int)
case class TcpToUserAddIntents(list: Array[String], pid: Int)
case class UserToUserYouCanCallMe(i: Intent)
case class UserToTcpYouCanCallForThisIntets(i: Intent)
case class TcpToUserGetAllIncomingIntents(pid: Int)
case class TcpToUserGetAllIntentsForRemove(pid: Int)
case class TcpToUserRemoveIntet(i: String, pid: Int)
case class UserToUserRemoveIntent(i: Intent, inOut: Int)
case class UserToTcpAfterRemoveIntent(bug: String, pid: Int)
case class UserToTcpRemoveIntent(a: Intent)
case class UserToUserCanICallYouFromIntentsCreater(a: Intent)
case class UserManagerToBDSaveNewUser(user: User)
case class ToBDMarkOldIntent(i: Intent)
case class UserToBdSetContacts(id: String, contacts: Array[String], regContacts:Map[String, ActorRef])
case class UserToBDAddContacts(id: String, newContacts:IndexedSeq[String], newRegContacts:Map[String, ActorRef])
case class UserToBDSetFavoritList(id: String, flist:Map[String,ActorRef])
case class UserToBDSetSeeings(id: String, slist:Map[String,ActorRef])
case class IntenteManagerToBDSaveNewIntent(i: Intent)
case class IntentManagerToBDMarkNonactualIntents(intents: Map[String,Intent])
case class IntentManagerToBDMarkNonactualIntent(i: Intent)
case class UserToUserIHaveRegistred(id:String, a: ActorRef)
case class UserToTcpYourContactHaveRegistr(id: String)
case class UserToBdRemoveContacts(id: String, list: Array[String])
case class UserToBDRemoveFromFavoritList(id: String, list:Array[String])
case class UserToBDAddToFavoritList(id: String, list:Array[String])
case class UserToBDRemoveFromSeeings(id:String, list:Array[String])
case class UserToBDAddForSeeings(id:String, list:Array[String])
case class UserManagerToBDManagerLoadIntents(globalUsersMap: Map[String, ActorRef], intmanager: ActorRef)
case class BDManagerToIntentManagerCreatIntents(globalUsersMap: Map[String, ActorRef], prti: ArrayBuffer[ProtoIntent])
case class IntentManagerToUserAddIntent(intn: Intent, inOut: Int)
case class IntentManagerToTcpServerStart()
case class UserManagerToUserSetAllContactList(contacts:Array[String], favorits: Array[String], seeings: Array[String] )

case class UserToBdManagerAddRegContacs(id: String, regId: String)
case class UserToTcpCloseConnection()
case class  UserManagerToUserInformAll()
case class TcpToUserGetAllRegistredUsers(pid: Int)
case class UserToTcpTakeAllRegistredUsers(regs: Map[String, ActorRef], pid:Int)
case class IntentManagerToUserAddIntentToRecycle(i: Intent, inOut: Int)
case class UserToDbManagerMarkIntentsPreparetoremove(i: Intent, inOut:Int)
case class UserToDbManagerMarkIntentsSynchronize(i: Intent)
case class UserToTcpPing(reguestForCall: CallConteoner)
case class UserToIntensCalculateManager(in: Map[String, Intent], out: Map[String, Intent])
case class CalculatorManagerToUserRequestForReadyToCall(a: Intent)
case class UserToCalculatorManagerRespons(a: Intent, okOrNo: Int)
case class CalculatorToUserWillYouCall(in: Intent)
case class UserToCalculatorIamAndToCall(i: Intent)