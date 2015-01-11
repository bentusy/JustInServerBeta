package attracti.develop.callalign.server.conections

/**
 * Created by Administrator on 24.12.2014.
 */

import attracti.develop.callalign.server.GlobalContext
import attracti.develop.callalign.server.utill.{UserToUserResponsForCall, CallConteoner}

import scala.collection.mutable.{Map, Queue}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
class TransportProtocolCollection {

  val mapa=Map[Int,  (doSomething)]()
  var mapCorrectSize=0
  val mapMaxSize=50
//  val queue=Queue[Int]()

  def +=(pid:Int, func:doSomething): Unit ={
//   if(mapCorrectSize+1>mapMaxSize){
//     mapa.remove(queue.dequeue())
//     mapCorrectSize-=1
//   }


    val dellF = Future {
      Thread.sleep(GlobalContext.timeToPing)
      pid
    }
    dellF.onSuccess{
      case pid if(mapa.contains(pid))=>{
       val f = mapa.getOrElse(pid, null)
        mapa -= pid
        if (f!=null)  f.run(0)
      }
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
//  def getFuncFromPid(pid: Int):doSomething={
//    mapa.getOrElse(pid,null)
//  }

}

abstract class doSomething(){
  def run(typ: Int)
}
