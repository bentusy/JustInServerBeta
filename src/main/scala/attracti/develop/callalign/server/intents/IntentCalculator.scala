package attracti.develop.callalign.server.intents

import akka.actor.{Actor, Props}
import attracti.develop.callalign.server.utill.{IntentCalculatorToIntentWork, IntentWeightOrder, ShedulerToIntentCalculatorStart}

import scala.collection.mutable.{TreeSet, ListBuffer,HashSet}
import scala.collection.immutable.{List}


object IntentCalculator {
  def props(fullIntentsList: TreeSet[IntentConteiner]): Props =
    Props(new IntentCalculator(fullIntentsList))
}


class IntentCalculator(val fullIntentsList: TreeSet[IntentConteiner]) extends Actor{

  val predicate:IntentConteiner=>Boolean= ic=>if(ic.istatus==1&&ic.statusCreator==1&&ic.statusDestisnation==1&&ic.synchronize)true else false


def start(): Unit = {
  implicit val ord = IntentWeightOrder
  val step2 = HashSet[String]()

  val f1: IntentConteiner => Unit = x => if (step2.contains(x.idCreator) == false) {
    if (step2.contains(x.idDestination) == false) {
      step2 += x.idCreator;
      step2 += x.idDestination;
      x.iRef ! IntentCalculatorToIntentWork
    }
  }

  fullIntentsList.filter(predicate).toList.sorted.foreach(f1(_))

}



  override def receive: Receive = {

    case ShedulerToIntentCalculatorStart=>start

  }
}
