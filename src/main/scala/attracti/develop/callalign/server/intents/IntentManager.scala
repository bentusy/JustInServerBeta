package attracti.develop.callalign.server.intents

import java.util.Calendar
import java.util.concurrent.TimeUnit

import akka.actor.{Props, ActorLogging, Actor, ActorRef}
import attracti.develop.callalign.server.utill._
//import org.apache.logging.log4j.{LogManager, Logger}

import scala.collection.mutable._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
/**
 * Created by Darcklight on 12/1/2014.
 */
class IntentManager(DAO: ActorRef) extends Actor with ActorLogging{

//  val log: Logger = LogManager.getLogger("InfoLoger")
  val fullIntentsList= TreeSet.empty[IntentConteiner]
  val dao=DAO
  val a =Duration.create(24, TimeUnit.HOURS)
  val b = Duration.create(12, TimeUnit.HOURS)
  var deamonForCleaOld = context.system.scheduler.schedule(a , b , self, DeamonToIntentManagerClean)

  val intentCalulator= context.system.actorOf(Props(new IntentCalculator(fullIntentsList)), name = "intentCalculator")



  override def preStart(){
   log.debug("IntentManager start success")
  }

  override def postStop(): Unit ={
    log.debug("IntentManager stopped")
  }

  override def postRestart(reason: Throwable): Unit = {
    log.error(reason, "IntentManager was restart")
  }

  override def  preRestart(reason: Throwable, msg: Option[Any]): Unit = {
    log.error(reason, "IntentManager will restart bczoff - "+msg)
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
      log.debug("IntentMeneger cleaning intents...")
clearOldIntents
    }

    case UserToIntentManagerAddIntent(a)=>{
      log.debug("IntentManager add new intent "+a.toString())
    fullIntentsList+=a
    dao ! IntenteManagerToBDSaveNewIntent(a)
  }
    case UserToIntentManagerRemoveIntent(a)=>{
      log.info("IntentMeneger removing intent "+a)
      fullIntentsList-=a
      dao ! IntentManagerToBDMarkNonactualIntent(a)
    }

    case UserManagerToIntentManagerLoad(arr)=>fullIntentsList ++=arr;
      sender ! "load complete"
      log.debug("UserManager load intents complete")

    case UserToIntentManagerRemoveIntents(m)=> {
      log.info("IntentMeneger removing intent "+m)
      fullIntentsList --= m.values
      dao ! IntentManagerToBDMarkNonactualIntents(m)
    }
  }



}
