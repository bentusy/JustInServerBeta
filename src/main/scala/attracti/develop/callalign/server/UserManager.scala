package attracti.develop.callalign.server

import akka.actor.{Actor, ActorRef, Props}
import attracti.develop.callalign.server.users.{ProtoUser, User, UserActor}
import attracti.develop.callalign.server.utill._
import org.apache.logging.log4j.{LogManager, Logger}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.{Map => MHM, ListBuffer, ArrayBuffer}


object UserManager {
  def props(intentM: ActorRef, dao: ActorRef, pu: ArrayBuffer[ProtoUser]): Props =
    Props(new UserManager( intentM, dao, pu))}


class UserManager(intntManager: ActorRef, daoc: ActorRef, pu: ArrayBuffer[ProtoUser]) extends Actor {
  val log:Logger =LogManager.getLogger("InfoLoger")
  val globalUserMap=MHM[String, ActorRef]()
  val dao=daoc
  val intentManager = intntManager
   postconstract()

  def postconstract(): Unit ={
    log.info("Start creating users from BD")
    val usrs = ListBuffer[User]()
    for (a<-pu){
      val user = new User(a.id, globalUserMap, intentManager, a.countrycod, dao)
      val aref = context.actorOf(Props(new UserActor(user)), name = a.id.toString)
      globalUserMap += (a.id -> aref)
      usrs += user
    }

    for(b <- pu){
    val s = globalUserMap(b.id) 
      s ! UserManagerToUserSetAllContactList(b.contacts, b.favorits, b.seeings)
      println("Contacts length == "+ b.contacts.length)
    }
    dao ! UserManagerToBDManagerLoadIntents(globalUserMap, intentManager)
  }


  override def preStart = {
    log.info("Start UserManager")
  }
  override def postStop(): Unit ={
    log.info("STOP user manger - "+self.path)
  }

  override def receive = {

    case TcpToUserManagerIsItReg(id, deviceType, pkgId)=>{
      val aRef = globalUserMap.getOrElse(id, null)
      sender ! UserManagerToTcpRespForAuthorization(id, aRef, pkgId)
      log.info("UserManager have verified "+id+" = "+ aRef)
    }

    case TcpToUserManagerRegisterUser(countryCod, phoneNomber, pkgId) => {
      val id = countryCod+phoneNomber
      if(globalUserMap.contains(id)==false){
      val user = new User(id, globalUserMap, intentManager,  countryCod, dao)
      val aref = context.actorOf(Props(new UserActor(user)), id.toString)
      aref ! UserManagerToUserInformAll()
      globalUserMap += (id -> aref)
      sender ! UserManagerToTcpRegNewUser(id, aref, pkgId)
      dao ! UserManagerToBDSaveNewUser(user)
      log.info("User manager has registered new user = "+id)
      }else{
      sender ! UserManagerToTcpRegNewUser(id, null, pkgId)
      }
    }
  }
}

