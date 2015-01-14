package attracti.develop.callalign.server.utill

import akka.actor.ActorRef
import attracti.develop.callalign.server.users.Intent
import scala.collection.Map
import scala.collection.mutable.ArrayBuffer

/**
 * Created by Artem on 1988,20,10.

 */
object ProtocolMessageDescribe {

  def error(block1: String, block2: String, block0:String = "01"): String ={
  val a =  block0+"/"+block1+"/"+block2+"\n"
    a
  }

  def regOk(id: String): String={
    "11/"+id+"/\n"
  }

  def autSuccessful (id: String): String={
    "12/"+id+"/\n"
  }



  def okIDellContact(id: Int): String={
    "17/"+id+"/\n"
  }

  def lisCountWithHash(count: Int, hash: Int): String ={
    "18/"+count+"/"+hash+"\n"
  }

  def  statusSetSuccessful(status: Int): String={
    "22/"+status+"/\n"
  }

  def allOk(cmnd: String): String ={
    cmnd+"/"+"113"+"/\n"
  }

  def removeIntent(a: Intent): String={
   "32/"+a.idCreator+"#"+a.idDestination+"/\n"
  }

  def canOrNoToCall(id: String, canOrNo: Int): String={
    canOrNo match {
      case 1=>  "24/"+id+"/\n"

      case 0 => "25/"+id+"#5/\n"
      case 2 =>  "25/"+id+"#6/\n"
    }
  }

  def takeIncomingIntent(i: Intent): String ={
   "28/"+i.toString()+"/\n"
  }

  def youCanCallForThisIntent(i: Intent): String={
    "30/"+i.idCreator+"#"+i.idDestination+"/\n"
  }

  def newUserHaveHaveRegistered(id: String): String ={
    "34/"+id+"/"
  }

  def afterRemoveIntent(bug: String): String ={
    if(bug==null) "31/113/" else  "31/"+bug
  }

  def takeIntentsForRemoving(list: Map[String, Intent])={
    if(list==null){"113"}else{
      var s = new StringBuffer()
      for(a <-list){
        s.append(a.toString())
        s.append(",")       // performers ??????????
      }

      s.toString()}
  }

  def takeIncomingIntets(list: Map[String, Intent]):String={
    println("testPoint")
    if(list.isEmpty){"113"}else{
    var s = new StringBuffer()
    for(a <-list){
      s.append(a._2.toString())
      s.append(",")       // performers ??????????
    }

    s.toString()}
  }

  def yourContactChangeStatus(rid: String, status: Int): String={
    "23/"+rid+":"+status+"/\n"
  }

  def  nombersAdsdedSuccessfuly(users:  Map[String, ActorRef]): String={
    var s = new StringBuffer()
    if(users.isEmpty){s.append("113")
    }else{
    for(a <-users){
      s.append(a._1)
      s.append(",")       // performers ??????????
    }
    }
    s.toString()
  }

  def  contctsSetSuccessfuly(users:  Map[String, ActorRef], errorsContacs: ArrayBuffer[String]): String={
    val s = new StringBuffer()
    if(users==null|| users.isEmpty){
      s.append("113")
    }else{
    for(a <-users){
      s.append(a._1)
      s.append(",")     // performers ??????????
    }
    }
    s.append("#")
    if(errorsContacs==null||errorsContacs.isEmpty){
      s.append("113")
    }else{
    for(b<- errorsContacs){
      s.append(b)
      s.append(",")
    }
    }

    s.toString()
  }


  def  setFavorits(errorsContacs: ArrayBuffer[String]): String={
    val s = new StringBuffer()

    if(errorsContacs==null||errorsContacs.isEmpty){
      s.append("113")
    }else{
      for(b<- errorsContacs){
        s.append(b)
        s.append(",")
      }
    }

    s.toString()
  }

  def  addFavorits(errorsContacs: ArrayBuffer[String]): String={
    val s = new StringBuffer()

    if(errorsContacs==null||errorsContacs.isEmpty){
      s.append("113")
    }else{
      for(b<- errorsContacs){
        s.append(b)
        s.append(",")
      }
    }

  s.toString()
  }


  def  setSeeings(errorsContacs: ArrayBuffer[String]): String={
    val s = new StringBuffer()

    if(errorsContacs==null||errorsContacs.isEmpty){
      s.append("113")
    }else{
      for(b<- errorsContacs){
        s.append(b)
        s.append(",")
      }
    }

    s.toString()
  }

  def  addSeeings(errorsContacs: ArrayBuffer[String]): String={
    val s = new StringBuffer()

    if(errorsContacs==null||errorsContacs.isEmpty){
      s.append("113")
    }else{
      for(b<- errorsContacs){
        s.append(b)
        s.append(",")
      }
    }
//    s.append(",&/\n")
    s.toString()
  }

  def  regUsers(regUsers: Map[String, ActorRef]): String={
    val s = new StringBuffer()

    if(regUsers==null||regUsers.isEmpty){
      s.append("113")
    }else{
      for(b<-regUsers){
        s.append(b._1)
        s.append(",")
      }
    }

    s.toString()
  }

  def addOutIntnts(erlist: ArrayBuffer[String]):String={
    val s = new StringBuffer()

    if(erlist==null||erlist.isEmpty){
      s.append("113")
    }else{
      for(b<-erlist){
        s.append(b)
        s.append(",")
      }
    }
   s.toString()
  }

}
