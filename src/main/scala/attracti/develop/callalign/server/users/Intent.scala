package attracti.develop.callalign.server.users

import java.util.Calendar
import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import attracti.develop.callalign.server.utill.Utils

/**
 * Created by Darcklight on 12/1/2014.
 */
class Intent( ) extends Ordered[Intent]  {
  var id:String=null
  var idCreator:String=null
  var aRefCreator:ActorRef =null
  var idDestination:String=null
  var aRefDestination:ActorRef =null
  var dateToDie:Calendar =  Calendar.getInstance()
  var synchronize:Boolean=false



 def this(idc: String, idCreatorc: String, aRefCreatorc: ActorRef, idDestinationc: String,
          aRefDestinationc: ActorRef, timeToDiec: Long, synchronizec:Boolean){
   this()
    id = idc
    idCreator = idCreatorc
    aRefCreator = aRefCreatorc
    idDestination = idDestinationc
    aRefDestination = aRefDestinationc
    dateToDie.setTimeInMillis(timeToDiec)
    synchronize=synchronizec

 }
  def this(idCreatorc: String, aRefCreatorc: ActorRef, idDestinationc: String, aRefDestinationc: ActorRef,
  timeToDiec: String){
      this()
     id = Utils.makeKeyFromIntetsUsers(idCreatorc, idDestinationc)
     idCreator = idCreatorc
     aRefCreator = aRefCreatorc
     idDestination = idDestinationc
     aRefDestination = aRefDestinationc
     dateToDie.add(Calendar.MINUTE, timeToDiec.toInt)
  }

override def toString():String={
  val s="#"
  val minToDie= TimeUnit.MILLISECONDS.toMinutes(dateToDie.getTimeInMillis).asInstanceOf[Int]-TimeUnit.MILLISECONDS.toMinutes(Calendar.getInstance().getTimeInMillis).asInstanceOf[Int];
  idCreator+s+idDestination+s+minToDie.toString //  [id Инициатора]#[id адресата]#[mn to die]
}

  override def compare(that: Intent): Int ={
  this.dateToDie.compareTo(that.dateToDie)
  }

}
