package attracti.develop.callalign.server

import java.util.Calendar

import akka.actor.Actor.Receive
import akka.actor.{ActorRef, Props, Actor}
import attracti.develop.callalign.server.users.{Intent, UserActor}
import attracti.develop.callalign.server.utill._
import org.apache.logging.log4j.{LogManager, Logger}


import scala.collection.mutable._
import scala.concurrent.duration.Duration;
import java.util.concurrent.TimeUnit;
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created by Darcklight on 12/1/2014.
 */
class IntentManager(DAO: ActorRef) extends Actor {

  val log: Logger = LogManager.getLogger("InfoLoger")
  val fullIntentsList= TreeSet.empty[Intent]
  val dao=DAO
  import context.system
  val  a =Duration.create(24, TimeUnit.HOURS)
  val b = Duration.create(12, TimeUnit.HOURS)
  var deamonForCleaOld = context.system.scheduler.schedule(a , b , self, DeamonToIntentManagerClean)

 override def preStart(){
    log.info("Start Intent Manager")
  }

def addIntent(intnt: Intent): Unit ={

  fullIntentsList +=(intnt)
}

  def clearOldIntents: Unit = {
    var toDay = Calendar.getInstance()
    var oldIntents= Map[String, Intent]()
  for(a <-fullIntentsList){
    if(toDay.after(a.dateToDie)){
    a.aRefCreator ! IntentManagerToUserRemoveOld(a, 1)
    a.aRefDestination ! IntentManagerToUserRemoveOld(a, 0)
    fullIntentsList.remove(a)
      oldIntents += (a.id -> a)
    }
   dao ! IntentManagerToBDMarkNonactualIntents(oldIntents)
  }

  }

  override def receive: Receive = {

    case DeamonToIntentManagerClean =>{
      log.info("IntentMeneger cleaning intents")
clearOldIntents
    }

    case UserToIntentManagerAddIntent(a)=>{
      log.info("IntentMeneger adding new intent ")
    fullIntentsList+=a
    dao ! IntenteManagerToBDSaveNewIntent(a)
  }
    case UserToIntentManagerRemoveIntent(a)=>{
      log.info("IntentMeneger removing intent ")
      fullIntentsList-=a
      dao ! IntentManagerToBDMarkNonactualIntent(a)
    }

    case UserToIntentManagerRemoveIntents(m)=> {
      fullIntentsList --=m.values}
      dao ! IntentManagerToBDMarkNonactualIntents(m)

    case BDManagerToIntentManagerCreatIntents(globalUsersMap, protointents)=>{
      log.info("Start creating intents from BD")
      for(a<-protointents) {

      val i = new Intent(a.id, a.idCreator, globalUsersMap(a.idCreator), a.idDestination,
        globalUsersMap(a.idDestination), a.timeToDie, a.synchronize)
        fullIntentsList += i
        a.prepareToRemove match {
          case 0=> i.aRefDestination ! IntentManagerToUserAddIntentToRecycle(i, 0)
          case 1=> i.aRefCreator ! IntentManagerToUserAddIntentToRecycle(i, 1)
          case 3=> {
            i.aRefCreator ! IntentManagerToUserAddIntent(i, 1)
            i.aRefDestination ! IntentManagerToUserAddIntent(i, 0)
          }
                  }
      }

      context.actorSelection("/user/tcpServer") ! IntentManagerToTcpServerStart

  }
  }



}
