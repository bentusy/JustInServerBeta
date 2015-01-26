package attracti.develop.callalign.server.intents

import java.util.Calendar
import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import attracti.develop.callalign.server.exeptions.PutExceptions
import attracti.develop.callalign.server.utill.{Status, IIndex}

class IntentConteiner(val iid: String,val idCreator:String,val statusCreator: Status, val aRefCreator:ActorRef,
                           val idDestination:String,val aRefDestination:ActorRef, val inedx:IIndex, timeToDiec: String) extends Ordered[IntentConteiner] {
  val dateToDie=Calendar.getInstance()
  dateToDie.add(Calendar.MINUTE, timeToDiec.toInt)
  var synchronize= false
private[this] var iRefV:ActorRef=null
private[this] var putFlag=true
private[this] var putSFlag=true
private[this] var putDFlag=true
 var istatus:Status=null
 var statusDestisnation: Status=null
 var isInRecycle=false
 var weight=0


  def this(iid: String, idCreator:String, statusCreator: Status, aRefCreator:ActorRef,
          idDestination:String, statusDestinationc: Status, aRefDestination:ActorRef, inedx:IIndex, timeToDiec: String, prepToRemove:Int, synchr: Boolean){
    this(iid, idCreator, statusCreator, aRefCreator, idDestination, aRefDestination, inedx, timeToDiec)
    statusDestisnation=statusDestinationc
    isInRecycle=if(prepToRemove==3)false else true
    synchronize=synchr
  }

  def isICreator(id:String):Boolean={
    if(id==idCreator)true else false
  }

  def isIDestination(id:String):Boolean={
    if(id==idDestination)true else false
  }


  def iRef=iRefV

  def iRef_=(ir: ActorRef): Unit ={
    if(putFlag){iRef=ir; putFlag=false;} else throw PutExceptions(s"cannot put $ir, this container is already comprise Intent")
  }

  def putIStatus(st: Status): Unit ={
    if(putSFlag){istatus=st; putSFlag=false;} else throw PutExceptions(s"cannot put $st, this container is already comprise Status")
  }
  def putDStatus(st: Status): Unit ={
    if(putDFlag){statusDestisnation=st; putDFlag=false;} else throw PutExceptions(s"cannot put $st, this container is already comprise Dest-Status")
  }






  override def toString():String={
    val s="#"
    val minToDie= TimeUnit.MILLISECONDS.toMinutes(dateToDie.getTimeInMillis).asInstanceOf[Int]-TimeUnit.MILLISECONDS.toMinutes(Calendar.getInstance().getTimeInMillis).asInstanceOf[Int];
    idCreator+s+idDestination+s+minToDie.toString //  [id Инициатора]#[id адресата]#[mn to die]
  }

  override def compare(that: IntentConteiner): Int ={
    this.dateToDie.compareTo(that.dateToDie)
  }



}