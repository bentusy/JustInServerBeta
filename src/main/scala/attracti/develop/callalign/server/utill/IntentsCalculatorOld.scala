/*
package attracti.develop.callalign.server.utill

import java.util.concurrent.TimeoutException

import akka.actor.{ActorRef, Actor}
import akka.actor.Actor.Receive
import akka.util.Timeout
import attracti.develop.callalign.server.GlobalContext
import attracti.develop.callalign.server.intents.Intent
import attracti.develop.callalign.server.users._
import scala.collection.{mutable, Map}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Promise, Future, Await}
import akka.pattern.{ ask, pipe }
import scala.concurrent.duration._
import akka.actor.Cancellable

class IntentsCalculator extends Actor{
  implicit val ex= context.system.dispatcher
    implicit val sys = context.system
  val intentsQueue=mutable.Queue[Intent]()
  var currentIntent:Intent=null
  var isWork=false
  var masterUser:ActorRef=null
  var currentFuture:Future[(UserToIntentCalcultorRS, UserToIntentCalcultorRS)]=null


  def start(): Unit ={
next
  }
  def next(): Unit ={
println("очередь в интенте до выимки"+intentsQueue.size)
    if(intentsQueue.isEmpty){currentFuture=null;currentIntent=null;isWork=false;
    masterUser ! IntentsCalculatorToUserFree(self)
    return;}

    currentIntent=intentsQueue.dequeue()
    masterUser=currentIntent.aRefCreator
    println("очередь в интенте поссле выимки"+intentsQueue.size)
    val p1:Promise[UserToIntentCalcultorRS]=Promise()
    val p2:Promise[UserToIntentCalcultorRS]=Promise()

    val c1 = TimeoutScheduler.startTimout(p1,GlobalContext.timeIntIntentsCalculator)
    val c2 = TimeoutScheduler.startTimout(p2,GlobalContext.timeIntIntentsCalculator)

    val f1=p1.future
    val f2=p2.future

    currentIntent.aRefCreator ! IntentsCalculatorToUserRQ(currentIntent, self, p1)
    currentIntent.aRefDestination ! IntentsCalculatorToUserRQ(currentIntent, self, p2)

    val b = for {
      r1 <- f1
      r2 <- f2
    }yield(r1.asInstanceOf[UserToIntentCalcultorRS],r2.asInstanceOf[UserToIntentCalcultorRS]);


    currentFuture=b
    implicit val timeout2 = GlobalContext.timeToWeightCallIntentAnswer
    val p3 = Promise[UserToIntentCalculatorCallRS]()
    val f3 = p3.future
    var c3:Cancellable=null
    b.onSuccess{
      case x:(UserToIntentCalcultorRS, UserToIntentCalcultorRS) if(b==currentFuture)=>
        println("Вернулся в калькулятор запрос на согласие звонить инициатор ответил - "+x._1.readyOrNo+" адресат - "+x._2.readyOrNo)
        c1.cancel();c2.cancel()
        if(x._1.readyOrNo!=0) currentFuture=null;currentIntent=null;isWork=false; masterUser ! IntentsCalculatorToUserFree(self)
        if(x._2.readyOrNo!=0) next
        if(x._2.readyOrNo!=0&&x._1.readyOrNo!=0) currentFuture=null;currentIntent=null; isWork=false; masterUser ! IntentsCalculatorToUserFree(self)
        if(x._1.readyOrNo==0&&x._2.readyOrNo==0){
          x._1.intn.aRefCreator ! IntentsCalculatorToUserCallRQ(x._1.intn, p3);
         c3 = TimeoutScheduler.startTimout(p3,GlobalContext.timeToWeightCallIntentAnswer)
      }
     }
    b.onFailure {
      case a: Throwable if(b==currentFuture)=>next;println("таймаут в калькуляторе")
    }

    f3.onSuccess{
      case UserToIntentCalculatorCallRS(i, okNo)=>
        println("В КАЛЬКУЛЯТОР ПРИШЁЛ ОТВЕТ ЗАПРОСА НА ЗВОНОК")
        c3.cancel()
        okNo match {
        case 0=>sleepAndWeightForEndCall
        case 1=>currentFuture=null;currentIntent=null;isWork=false;
          masterUser ! IntentsCalculatorToUserFree(self)
      }  }

    f3.onFailure{
      case x:Throwable=>println("СРАБОТАЛ ТАЙМЕР ИНТЕНТА НА ожидание ответа по звонку")
        currentFuture=null;currentIntent=null;isWork=false;
        masterUser ! IntentsCalculatorToUserFree(self)
    }

  }

  def sleepAndWeightForEndCall(): Unit ={
    implicit val timeout = GlobalContext.timeIntIntentsCalculator
    val f = Future{}
    f.onFailure{
      case x:Throwable=>println("Калькулятор проснулся после ожидания ответа");next;
    }

  }

  override def receive: Receive = {

    case UserToIntensCalculateManagerStart(in, out)=>{
    println("Старт калькулятор от - "+out.head._2.idCreator+" количество исходящих интентов - "+out.size)
      if(isWork) {
    intentsQueue ++= out.values.filter(intentsQueue.contains(_)==false)
      currentIntent = null
    }else{
    val tl = out.values.filter(intentsQueue.contains(_)==false)
    intentsQueue ++= tl
    isWork=true
    masterUser = tl.head.aRefCreator
    start
    }
  }

   case UserToIntensCalculateManagerStop(aref:ActorRef)=>{
     println("Прилетел stop в калькулятор")
      if(aref==masterUser&&currentIntent!=null){
        currentIntent.aRefCreator ! IntentsCalculatorToUserFree(self)
        currentIntent.aRefDestination ! IntentsCalculatorToUserFree(self)
        currentIntent=null;isWork=false;masterUser=null
      }
//      if(currentIntent.aRefDestination==aref || currentIntent.aRefCreator==aref) next
      }

case UserToIntentCalcultorRemoveIntent(i)=>{
  println("Прилетел Remove в калькулятор")
    if(currentIntent==i){ currentIntent==null; currentFuture=null;
    next()
    }else{
    intentsQueue.dequeueFirst(x => x == i)
  }
}

    case UserToIntentCalcultorIamFree(aRef, i)=>{
      println("Прилетел я свободен от юзера")
    if(intentsQueue.contains(i)==false) intentsQueue+=i;if(isWork==false)next
    }

}
}

*/
