package attracti.develop.callalign.server.utill

import java.util.concurrent.TimeoutException

import akka.actor.{ActorRef, Actor}
import akka.actor.Actor.Receive
import akka.util.Timeout
import attracti.develop.callalign.server.GlobalContext
import attracti.develop.callalign.server.users._
import scala.collection.{mutable, Map}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Promise, Future, Await}
import akka.pattern.{ ask, pipe }
import scala.concurrent.duration._
//import scala.concurrent.ExecutionContext.Implicits.global


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

    if(intentsQueue.isEmpty){currentFuture=null;currentIntent=null;isWork=false;
      masterUser ! IntentsCalculatorToUserFree(self)
      return;}

    currentIntent=intentsQueue.dequeue()


    val p1:Promise[UserToIntentCalcultorRS]=Promise()
    val p2:Promise[UserToIntentCalcultorRS]=Promise()
    TimeoutScheduler.startTimout(p1,GlobalContext.timeIntIntentsCalculator)
    TimeoutScheduler.startTimout(p2,GlobalContext.timeIntIntentsCalculator)
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

    b.onSuccess{
      case x:(UserToIntentCalcultorRS, UserToIntentCalcultorRS) if(b==currentFuture)=>println("Вернулся длинный запрос в калькулятор")
        if(x._1.readyOrNo!=0)currentFuture=null;currentIntent=null;isWork=false; masterUser ! IntentsCalculatorToUserFree(self)
        if(x._2.readyOrNo!=0)currentIntent.aRefDestination ! IntentsCalculatorToUserFree(self)
        if(x._1.readyOrNo==0&&x._2.readyOrNo==0){
          x._1.intn.aRefCreator ! IntentsCalculatorToUserCallRQ(x._1.intn, p3);
          TimeoutScheduler.startTimout(p3,GlobalContext.timeToWeightCallIntentAnswer)
      }
     }
    b.onFailure {
      case a: Throwable if(b==currentFuture)=>next;println("таймаут в калькуляторе")
    }

    f3.onSuccess{
      case UserToIntentCalculatorCallRS(i, okNo)=> println("В КАЛЬКУЛЯТОР ПРИШЁЛ ОТВЕТ ЗАПРОСА НА ЗВОНОК")
        okNo match {

        case 0=>sleepAndWeightForEndCall
        case 1=>currentFuture=null;currentIntent=null;isWork=false;
          masterUser ! IntentsCalculatorToUserFree(self)
      }
    }
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
      case x:Throwable=>println("Калькулятор проснулся после ожмдания ответа");next;
    }

  }

  override def receive: Receive = {

    case UserToIntensCalculateManagerStart(in, out)=>{
      println("Старт калькулятор")
    if(isWork) intentsQueue.clear;currentIntent=null
    intentsQueue ++=out.values
//    intentsQueue ++=in.values
    masterUser=out.head._2.aRefCreator
    isWork=true
    start
  }

   case UserToIntensCalculateManagerStop(aref:ActorRef)=>{
      if(aref==masterUser){
        currentIntent.aRefCreator ! IntentsCalculatorToUserFree(self)
        currentIntent.aRefDestination ! IntentsCalculatorToUserFree(self)
        currentIntent=null;isWork=false;masterUser=null
      }
      if(currentIntent.aRefDestination==aref || currentIntent.aRefCreator==aref) next
   }

case UserToIntentCalcultorRemoveIntent(i)=>{
    if(currentIntent==intentsQueue){currentIntent==null;currentFuture=null;}
    intentsQueue.dequeueFirst(x=>x==i)
    next()
}


}
}

