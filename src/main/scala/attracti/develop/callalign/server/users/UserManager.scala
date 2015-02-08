package attracti.develop.callalign.server.users

import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import akka.event.LoggingReceive
import attracti.develop.callalign.server.intents.{IntentConteiner, UsersMetaData}
import attracti.develop.callalign.server.utill._
//import org.apache.logging.log4j.{LogManager, Logger}

import scala.collection.mutable.{ArrayBuffer, ListBuffer, Map => MHM}


object UserManager {
  def props(intentM: ActorRef, dao: ActorRef): Props =
    Props(new UserManager( intentM, dao))}


class UserManager(val intentManager: ActorRef, val dao: ActorRef) extends Actor with ActorLogging{
//  val log:Logger =LogManager.getLogger("InfoLoger")
  val globalUserMap=MHM[String, ActorRef]()
  var loadInc=0
  var tcpServer:ActorRef=null
  val metaDatas=MHM[String, UsersMetaData]()


  def load(pusers:ArrayBuffer[ProtoUser], pi:ArrayBuffer[ProtoIntent], pm:ArrayBuffer[ProtoMetaData]): Unit ={
    log.debug("UserManager (Users, Intents,Meta's) loading...")
    if(pusers.isEmpty){tcpServer ! UserMangerToServerStart();return;}
    val users=MHM[String,User]()
    for(a<-pusers){
      val user = new User(a.idc, a.countrycodc, globalUserMap, intentManager, dao, metaDatas, self)
      val aref = context.actorOf(Props(new UserActor(user)), a.idc)
      globalUserMap += (a.idc -> aref)
      users+=(a.idc ->user)
    }
    for(a<-pm){
if(globalUserMap.contains(a.user1Id)&&globalUserMap.contains(a.user2Id)){
      metaDatas +=(a.idm -> new UsersMetaData(a.idm,
                                              a.user1Id,
                                              globalUserMap(a.user1Id),
                                              a.user2Id,
                                              globalUserMap(a.user2Id),
                                              a.u1Tou2CallTime,
                                              a.u1Tou2CallCount,
                                              a.u2Tou1CallTime,
                                              a.u2Tou1CallCount ))}
    }

    val icArr=ArrayBuffer[IntentConteiner]()
    for(a<-pi){
    icArr+= new IntentConteiner(a.idc, a.idCreatorc, users(a.idCreatorc).status, globalUserMap(a.idCreatorc), a.idDestinationc,users(a.idDestinationc).status,
     globalUserMap(a.idDestinationc), IIndex(a.indexOrder, a.indexManualChng), a.timeToDiec.toString, a.prepareToRemovc, a.synchronizec)
    }


    import scala.concurrent.Await
    import akka.pattern.ask
    import akka.util.Timeout
    import scala.concurrent.duration._
    implicit val timeout = Timeout(5 seconds)
    val future = intentManager ? UserManagerToIntentManagerLoad(icArr) // enabled by the “ask” import
    val result = Await.result(future, timeout.duration).asInstanceOf[String]

    pusers.foreach(x=>globalUserMap(x.idc) ! UserManagerToUserLoad(icArr, x.regcontactsc, x.favoritsc, x.seeingsc))

    log.debug("UserManager load all Users, Intents, Metas")
  }

  override def preStart() {
    log.debug("UserManager load success")
  }

  override def postStop(): Unit ={
    log.debug("UserManager stopped")
  }

  override def postRestart(reason: Throwable): Unit = {
    log.error(reason, "UserManager was restart")
  }

  override def  preRestart(reason: Throwable, msg: Option[Any]): Unit = {
    log.error(reason, "UserManager will restart bczoff - "+msg)
  }




  override def receive = {

    case SystemTouserManagerPutServer(serv) => tcpServer = serv

    case BdManagerToUserManagerLoad(pu, pi, pm) => load(pu, pi, pm)

    case UserToUserManagerLoadComplete() => loadInc += 1;
      if (loadInc >= globalUserMap.size) {
        tcpServer ! UserMangerToServerStart();
        log.debug("UserManager -> all Users load success")
      }

    case UserToUserManagerAddMetadates(from, md) =>
      if (metaDatas.contains(md.id) == false) {
        if (metaDatas.contains(md.idRevers) == false) {
          metaDatas += md.id -> md
          dao ! UserManagerToBdManagerSaveNewMeta(md)
          log.debug(s"UserManager add new metaData-> $md  from user->$from")
        } else {
          from ! UserManagerToUserSetMetaInRegs(metaDatas(md.idRevers))
        }
      } else {
        from ! UserManagerToUserSetMetaInRegs(metaDatas(md.id))
      }

    case TcpToUserManagerIsItReg(id, deviceType, pkgId) => {
      val aRef = globalUserMap.getOrElse(id, null)
      sender ! UserManagerToTcpRespForAuthorization(id, aRef, pkgId)
      log.debug(s"UserManager authorised new User->id:$id")
    }

    case TcpToUserManagerRegisterUser(countryCod, phoneNomber, pkgId) => {
      val id = countryCod + phoneNomber
      if (globalUserMap.contains(id) == false) {
        val user = new User(id, countryCod, globalUserMap, intentManager, dao, metaDatas, self)
        val aref = context.actorOf(Props(new UserActor(user)), id.toString)
        aref ! UserManagerToUserInformAll()
        globalUserMap += (id -> aref)
        sender ! UserManagerToTcpRegNewUser(id, aref, pkgId)
        dao ! UserManagerToBDSaveNewUser(user)
        log.debug(s"UserManager registered new User->id:$id")
      } else {
        sender ! UserManagerToTcpRegNewUser(id, null, pkgId)
        log.debug(s"UserManager deny registration User->id:$id")
      }
    }

  }
}

