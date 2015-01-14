package attracti.develop.callalign.server.conections

/**
 * Created by Administrator on 24.12.2014.
 */

import akka.util.Timeout
import attracti.develop.callalign.server.GlobalContext
import attracti.develop.callalign.server.utill.{UserToUserResponsForCall, CallConteoner}

import scala.collection.mutable.{Map, Queue}
import scala.concurrent.Future


class TransportProtocolCollection(val aRefMaster: TcpConnectionHandlerActor ) {

  val mapa=Map[Int,  (doSomething)]()
  var mapCorrectSize=0
  val mapMaxSize=50
//  val queue=Queue[Int]()

  def +=(pid:Int, func:doSomething, tempFlag:Int =0): Unit ={
    import scala.concurrent.duration._
    implicit val ec = aRefMaster.context.system.dispatcher
    implicit var timeout = GlobalContext.defoultPkgTimeToLive
      tempFlag match {
        case 1 => timeout = GlobalContext.timeToWeightCallIntentAnswerTCP
        case _=>
      }
    val dellF = Future {
      Thread.sleep(GlobalContext.timeToPing.duration.toNanos)
      pid
    }
    dellF.onSuccess{
      case pid:Int if(mapa.contains(pid))=>{
       val f = mapa.getOrElse(pid, null)
        mapa -= pid
        if (f!=null)  f.run(0)
      }
    }
    dellF.onFailure{
      case ex:Throwable=>mapa -= pid
    }

    mapa +=(pid -> func)
  }

  def clean(): Unit ={
    mapa.clear()

    mapCorrectSize=0
  }



  def respIncoming(pid: Int): Unit ={

    val f = mapa.getOrElse(pid,null)
    if (f!=null){
      mapa -= pid
      f.run(1)
    }
  }

  def getAndRemove(pid:Int):doSomething={
  val v=mapa(pid)
    mapa-=pid
    v
  }

  def isContein(pid: Int):Boolean={
mapa.contains(pid)
  }

//  def getFuncFromPid(pid: Int):doSomething={
//    mapa.getOrElse(pid,null)
//  }
}



abstract class doSomething(){
  def run(typ: Int)
}
