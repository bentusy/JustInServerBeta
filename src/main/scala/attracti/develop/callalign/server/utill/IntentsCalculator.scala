package attracti.develop.callalign.server.utill

import java.util.concurrent.TimeoutException

import akka.actor.{ActorRef, Actor}
import akka.actor.Actor.Receive
import akka.util.Timeout
import attracti.develop.callalign.server.users._
import scala.collection.{mutable, Map}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Future, Await}
import akka.pattern.{ ask, pipe }
import scala.concurrent.duration._
//import scala.concurrent.ExecutionContext.Implicits.global


class IntentsCalculator extends Actor{
  implicit val ec = context.system.dispatcher
  implicit val timeout = Timeout(5.seconds)
  val intentsQueue=mutable.Queue[Intent]()
  var currentIntent:Intent=null
  var isWork=false
  var masterUser:ActorRef=null

  def isCurrent(i: Intent):Boolean={
    currentIntent == i
  }

  def start(): Unit ={
    currentIntent=intentsQueue.dequeue()
    val f1= currentIntent.aRefCreator ? IntentsCalculatorToUserRQ(currentIntent)
    val f2= currentIntent.aRefDestination ? IntentsCalculatorToUserRQ(currentIntent)
   val b = for {
      r1 <- f1
      r2 <- f2
    }yield (r1.asInstanceOf[UserToIntentCalcultorRS],r2.asInstanceOf[UserToIntentCalcultorRS])

    b.onSuccess{
      case (a, b) if(isCurrent(a.intn))=>if(b!=1||a!=0)next else {
      a.intn.aRefCreator ! IntentsCalculatorToUserCall(a.intn)
    }
    }

  }
  def next(): Unit ={



  }

  override def receive: Receive = {

    case UserToIntensCalculateManagerStart(in, out)=>{
    if(isWork) intentsQueue.clear;currentIntent=null
    intentsQueue ++=out.values
    intentsQueue ++=in.values
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


  }

}

