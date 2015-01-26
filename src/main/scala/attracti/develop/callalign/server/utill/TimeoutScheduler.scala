package attracti.develop.callalign.server.utill

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Actor}
import akka.actor.Cancellable

import scala.concurrent._
import scala.concurrent.duration._
/**
 * Created by Administrator on 1/13/2015.
 */
object TimeoutScheduler{

  def startTimout(p:Promise[_], timeout: FiniteDuration)(implicit sys : ActorSystem): Cancellable={
    implicit val ex = sys.dispatcher
    sys.scheduler.scheduleOnce(timeout)(
     try{
       p.failure(new Throwable())
     }catch {
       case sx:IllegalStateException=>
       case ex:Throwable=>println(ex+" в Стартайме")
     }
    )

  }

}