package attracti.develop.callalign.server.intents

import akka.actor.{ActorLogging, Actor, Props}
import akka.io.{Tcp, IO}
import attracti.develop.callalign.server.GlobalContext
import attracti.develop.callalign.server.utill.{IntentCalculatorToIntentWork, IntentWeightOrder, ShedulerToIntentCalculatorStart}

import scala.collection.mutable.{TreeSet, ListBuffer,HashSet}
import scala.collection.immutable.{List}


object IntentCalculator {
  def props(fullIntentsList: TreeSet[IntentConteiner]): Props =
    Props(new IntentCalculator(fullIntentsList))
}


class IntentCalculator(val fullIntentsList: TreeSet[IntentConteiner]) extends Actor with ActorLogging{
  implicit val ex = context.system.dispatcher
  val predicate:IntentConteiner=>Boolean= ic=>{println("ic.istatus.value="+ic.istatus.value+"ic.statusCreator.value="+ic.statusCreator.value+"ic.statusDestisnation.value="
    +ic.statusDestisnation.value+"ic.synchronize="+ic.synchronize)
    if(ic.istatus.value==1&&ic.statusCreator.value==1&&ic.statusDestisnation.value==1&&ic.synchronize){;true;} else{ false}}
  val canc= context.system.scheduler.schedule(GlobalContext.delayIntentCalculatorStart, GlobalContext.periodicityIntentCalculatorStart,self, ShedulerToIntentCalculatorStart)


def start(): Unit = {
  implicit val ord = IntentWeightOrder
  val step2 = HashSet[String]()

  val f1: IntentConteiner => Unit = x =>{println("Начинаю"); if (step2.contains(x.idCreator) == false) {
    if (step2.contains(x.idDestination) == false) {
      step2 += x.idCreator;
      step2 += x.idDestination;
      println("Отправляю!! В интент")
      x.iRef ! IntentCalculatorToIntentWork()
    }
  }}

  fullIntentsList.filter(predicate).toList.sorted.foreach(f1(_))

}



  override def receive: Receive = {

    case ShedulerToIntentCalculatorStart=>start

    log.debug("IntentCalculator start... в дереве есть "+fullIntentsList.size)
  }


  override def preStart() {
    log.debug("IntentCalculator load success")
  }

  override def postStop(): Unit ={
    log.debug("IntentCalculator stopped")
  }

  override def postRestart(reason: Throwable): Unit = {
    log.error(reason, "IntentCalculator was restart")

  }


  override def  preRestart(reason: Throwable, msg: Option[Any]): Unit = {
    log.error(reason, "IntentCalculator will restart bczof - "+msg)
  }

}
