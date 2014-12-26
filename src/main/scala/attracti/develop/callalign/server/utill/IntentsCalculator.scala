package attracti.develop.callalign.server.utill

import java.util.concurrent.TimeoutException

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.util.Timeout
import attracti.develop.callalign.server.users._
import scala.collection.Map
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Future, Await}
import akka.pattern.{ ask, pipe }
import scala.concurrent.duration._
/**
 * Created by Darcklight on 12/5/2014.
 */
class IntentsCalculator extends Actor{

  implicit val timeout = Timeout(5 seconds)

def calcIntentsQueue(incomnig: Map[String, Intent], outgoing: Map[String, Intent] ): Unit={

for(a<-incomnig){
  a._2.aRefCreator ! UserToUserYouCanCallMe(a._2)
}

  for(a<-outgoing){
    a._2.aRefDestination ! UserToUserCanICallYouFromIntentsCreater(a._2)
  }



}

  override def receive: Receive = {

    case UserToIntensCalculateManager(in, out)=>{
      var flag = true
      for(a<-out; flag){

      val future = Future[(UserToCalculatorManagerRespons, UserToCalculatorManagerRespons)]{

       val c = (a._2.aRefCreator ? CalculatorManagerToUserRequestForReadyToCall).asInstanceOf[UserToCalculatorManagerRespons]
       val d = (a._2.aRefCreator ? CalculatorManagerToUserRequestForReadyToCall).asInstanceOf[UserToCalculatorManagerRespons]
       (c , d)}
    try{
      val res = Await.result(future, timeout.duration)
      if(res._1.okOrNo==1){flag=false}
      if(res._2.okOrNo==0){
        res._1.a.aRefCreator ! CalculatorToUserWillYouCall(a._2)
        val fut = Future[(UserToCalculatorIamAndToCall)]{
        val n = (a._2.aRefCreator ? CalculatorManagerToUserRequestForReadyToCall).asInstanceOf[UserToCalculatorIamAndToCall]
          n
      }
      }
    }catch{
      case e:TimeoutException =>
      case e:InterruptedException =>
      case e:_=>
        }

    }

  }


  }

}

