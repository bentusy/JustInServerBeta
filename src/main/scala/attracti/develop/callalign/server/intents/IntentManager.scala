package attracti.develop.callalign.server.intents

import java.util.Calendar
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef}
import attracti.develop.callalign.server.utill._
import org.apache.logging.log4j.{LogManager, Logger}

import scala.collection.mutable._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
/**
 * Created by Darcklight on 12/1/2014.
 */
class IntentManager(DAO: ActorRef) extends Actor {

  val log: Logger = LogManager.getLogger("InfoLoger")
  val fullIntentsList= TreeSet.empty[IntentConteiner]
  val dao=DAO
  val  a =Duration.create(24, TimeUnit.HOURS)
  val b = Duration.create(12, TimeUnit.HOURS)
  var deamonForCleaOld = context.system.scheduler.schedule(a , b , self, DeamonToIntentManagerClean)



 override def preStart(){
    log.info("Start Intent Manager")
  }

def addIntent(intnt: IntentConteiner): Unit ={

  fullIntentsList +=(intnt)
}

  def clearOldIntents: Unit = {
    var toDay = Calendar.getInstance()
    var oldIntents= Map[String, IntentConteiner]()
  for(a <-fullIntentsList){
    if(toDay.after(a.dateToDie)){
    a.aRefCreator ! IntentManagerToUserRemoveOld(a, 1)
    a.aRefDestination ! IntentManagerToUserRemoveOld(a, 0)
    a.iRef ! IntentManagerToIntentTerminate
    fullIntentsList.remove(a)
      oldIntents += (a.iid -> a)
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

    case UserManagerToIntentManagerLoad(arr)=>fullIntentsList ++=arr;sender ! "load complete"

    case UserToIntentManagerRemoveIntents(m)=> {
      fullIntentsList --= m.values
      dao ! IntentManagerToBDMarkNonactualIntents(m)
    }
  }



}
