package attracti.develop.callalign.server.utill

import java.util.Calendar
import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import attracti.develop.callalign.server.intents.{IntentConteiner, UsersMetaData, Intent}
import attracti.develop.callalign.server.users.{User}

import scala.collection.Map
import scala.collection.IndexedSeq
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Promise

/**
 * Created by User on 19.11.2014.
 */

//Container for incoming buffered data
case class IncomingData(typ: Int, dat: Seq[Byte], pid:Int=0)
//Container for Call Reguest operation
case class CallConteoner(idFrom: String, arefFrom: ActorRef, idFor: String, arefFor: ActorRef, pid:Int)
//IntentsIndex
case class IIndex(var indx:Int, var tf: Boolean)
//StatusObject
case class Status(private var valueStat:Int=0){
  private var caChe=0
  def value = valueStat
  def value_=(a:Int){caChe=valueStat;valueStat=a}
  def rollback(){valueStat=caChe}
}
case class ProtoIntent(idc: String,
                       idCreatorc: String,
                       idDestinationc: String,
                       timeToDiec: Long,
                       prepareToRemovc: Int,
                       synchronizec: Boolean,
                       indexManualChng: Boolean,
                       indexOrder: Int
                        )

case class ProtoUser (idc: String,
                      countrycodc: String,
                      contactsc: Array[String],
                      regcontactsc: Array[String],
                      favoritsc:Array[String],
                      seeingsc: Array[String] )

case class ProtoMetaData( idm:String,
                          user1Id: String,
                          user2Id: String,
                          u1Tou2CallTime: Int=0,
                          u1Tou2CallCount: Int=0,
                          u2Tou1CallTime: Int=0,
                          u2Tou1CallCount: Int=0
                          )

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

case class UserToTcpAfterAddContacts(rc:Iterable[String], pid: Int)

//case class UserToTcpSetStatusSuccessful(status: Int, pid: Int)

case class UserToUserYouHaveCorrectCall(id: Int, inOut: Int) 

case class TcpToUserRemoveContacts(contacts: Array[String], pid: Int)

case class TcpToUserGetAllStatus(pid: Int)

case class UserToTcpAfterSetContactsList(regContatcs: Iterable[String], errorcontacts: ArrayBuffer[String], pid:Int)
case class UserToUserGetMeYourStatus(id: String, aRef: ActorRef)
case class UserToIntentManagerAddIntent(ic: IntentConteiner)
case class DeamonToIntentManagerClean()
case class UserToIntentManagerRemoveIntent(intnt: IntentConteiner)
case class IntentManagerToUserRemoveOld(intnt: IntentConteiner, inOut: Int)
case class UserToUserRemoveOutIntent(a: Intent)
case class UserToTcpIntentsForRemove(intents: Map[String,IntentConteiner], pid: Int)
case class UserToIntentManagerRemoveIntents(intents: Map[String,IntentConteiner])
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
case class UserToUserRequestSolutionForCall(pr:Promise[Int],uid:String)//(objec: ReguestForCall)//(id: String, selfRef: ActorRef, pid: Int)
//case class UserToUserResponsForCall(callSolution:CallConteoner, answer: Int)//(id: String, canOrNo: Int, pid: Int)
case class UserToTcpResponsForCallRequest(rsp:Int,pid:Int)//(id: String, canOrNo: Int, pid: Int)
case class UserToUserAddIncomingIntent(ic: IntentConteiner)
case class UserToTcpTakeIncomingIntent(i: IntentConteiner)
case class UserToTcpTakeIncomingIntents(i: Map[String, IntentConteiner], pid: Int)
case class UserToTcpAfterAddIntents(errors: ArrayBuffer[String], pid:Int)
case class TcpToUserAddIntents(list: Array[String], pid: Int)
case class UserToUserYouCanCallMe(i: Intent)
case class TcpToUserGetAllIncomingIntents(pid: Int)
case class TcpToUserGetAllIntentsForRemove(pid: Int)
case class TcpToUserRemoveIntet(i: String, pid: Int)
case class UserToUserRemoveIntent(i: IntentConteiner, inOut: Int)
case class UserToTcpAfterRemoveIntent(bug: String, pid: Int)
case class UserToTcpRemoveIntent(a: IntentConteiner)
case class UserToUserCanICallYouFromIntentsCreater(a: Intent)
case class UserManagerToBDSaveNewUser(user: User)
case class ToBDMarkOldIntent(i: Intent)
case class UserToBdSetContacts(id: String, contacts: Array[String], regContacts:Iterable[String])
case class UserToBDAddContacts(id: String, newContacts:IndexedSeq[String], newRegContacts:Iterable[String])
case class UserToBDSetFavoritList(id: String, flist:Map[String,ActorRef])
case class UserToBDSetSeeings(id: String, slist:Map[String,ActorRef])
case class IntenteManagerToBDSaveNewIntent(i: IntentConteiner)
case class IntentManagerToBDMarkNonactualIntents(intents: Map[String,IntentConteiner])
case class IntentManagerToBDMarkNonactualIntent(i: IntentConteiner)
case class UserToUserIHaveRegistred(id:String, a: ActorRef)
case class UserToTcpYourContactHaveRegistr(id: String)
case class UserToBdRemoveContacts(id: String, list: Array[String])
case class UserToBDRemoveFromFavoritList(id: String, list:Array[String])
case class UserToBDAddToFavoritList(id: String, list:Array[String])
case class UserToBDRemoveFromSeeings(id:String, list:Array[String])
case class UserToBDAddForSeeings(id:String, list:Array[String])
//case class UserManagerToBDManagerLoadIntents(globalUsersMap: Map[String, ActorRef], intmanager: ActorRef)
case class BDManagerToIntentManagerCreatIntents(globalUsersMap: Map[String, ActorRef], prti: ArrayBuffer[ProtoIntent])
case class IntentManagerToUserAddIntent(intn: IntentConteiner, inOut: Int)
case class IntentManagerToTcpServerStart()
case class UserManagerToUserSetAllContactList(contacts:Array[String], favorits: Array[String], seeings: Array[String] )
case class UserToIntentCalcultorRemoveIntent(i: Intent)
case class UserToBdManagerAddRegContacs(id: String, regId: String)
case class UserToTcpCloseConnection()
case class  UserManagerToUserInformAll()
case class TcpToUserGetAllRegistredUsers(pid: Int)
case class UserToTcpTakeAllRegistredUsers(regs: Map[String, UsersMetaData], pid:Int)
case class IntentManagerToUserAddIntentToRecycle(i: IntentConteiner, inOut: Int)
case class UserToDbManagerMarkIntentsPreparetoremove(i: IntentConteiner, inOut:Int)
case class UserToDbManagerMarkIntentsSynchronize(i: IntentConteiner)
case class UserToTcpPing(reguestForCall: CallConteoner)
case class UserToIntensCalculateManagerStart(in: Map[String, Intent], out: Map[String, Intent])
case class UserToIntensCalculateManagerStop(aref:ActorRef)

//case class UserToCalculatorManagerIAmReadyToTallc(okOrNo: Int)
case class IntentsCalculatorToUserRQ(intn: Intent, self: ActorRef, p: Promise[UserToIntentCalcultorRS])
case class UserToIntentCalcultorRS(readyOrNo: Int, intn: Intent)
case class IntentsCalculatorToUserCallRQ(intn:Intent, p: Promise[UserToIntentCalculatorCallRS])
case class IntentsCalculatorToUserFree(aref:ActorRef)
case class UserToTcpWillYouCalForThisIntent(i: Intent, Calculator: ActorRef)
case class UserToTcpReadyToCallRQ(i:Intent, p: Promise[TcpToUserReadyToCallRS], aref: ActorRef)
case class TcpToUserReadyToCallRS(okOrNo: Int, i:Intent, aref: ActorRef)
case class UserToTcpCallIntentINF(i:IntentConteiner)
case class TcpToUserCallIntentRS(i: Intent, okNo:Int)
case class UserToIntentCalculatorCallRS(i: Intent, okNo: Int)
case class UserToIntentCalcultorIamFree(a:ActorRef, i: Intent)

case class ShedulerToIntentCalculatorStart()
case class UserToUserManagerAddMetadates(from: ActorRef, usersMetaData: UsersMetaData)
case class UserManagerToUserSetMetaInRegs(usersMetaData: UsersMetaData)
case class UserToBdManagerUpdateMetas(md: UsersMetaData)
case class UserToTcpUpdMetasRS(md: ArrayBuffer[String], pid:Int)
case class IntentManagerToIntentTerminate()
case class IntentCalculatorToIntentWork()
case class UserToIntentMetaRefresh()
case class IntentToUserCall(ic: IntentConteiner)
case class IntentToUserGetMeYourStatusPR(p:Promise[Int])
case class IntentToUserFree()
case class SystemToBdManagerLoad(um:ActorRef)
case class BdManagerToUserManagerLoad(pusers:ArrayBuffer[ProtoUser], pi:ArrayBuffer[ProtoIntent], pm:ArrayBuffer[ProtoMetaData])
case class UserManagerToIntentManagerLoad(arr:ArrayBuffer[IntentConteiner])
case class UserManagerToUserLoad(arr:ArrayBuffer[IntentConteiner], regcs:Array[String], favoritcs:Array[String], seeingscs:Array[String])
case class UserToUserManagerLoadComplete()
case class UserMangerToServerStart()
case class SystemTouserManagerPutServer(serv:ActorRef)
case class TcpToUserConfigStatusEvent(v:Int,pid:Int)
case class UserTotcpStatusEventConfRS(pid:Int)
case class TcpToUserSetMetas(str:Array[String], pid:Int)
case class TcpToUserSetIIndex(str:Array[String], pid:Int)
case class UserManagerToBdManagerSaveNewMeta(meta: UsersMetaData)
case class UserToBdManagerUpdIIndex(ic: IntentConteiner)
case class UserToTcpSetIIndexRS(erl: ArrayBuffer[String], pid:Int)