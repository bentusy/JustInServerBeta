package attracti.develop.callalign.server.utill

import java.text.{SimpleDateFormat, DateFormat}
import java.util.Calendar
import java.util.regex.{Matcher, Pattern}

import scala.collection.mutable


object Utils {

    val patern60:Pattern =Pattern.compile("^\\+[0-9]{1,5}:[0-9]{5,11}$")
    val patern61:Pattern =Pattern.compile("^\\+[0-9]{12}:[0-1]{1}$")
    val patern62_70:Pattern =Pattern.compile("^(\\+[0-9]{12},)+$")
    val patern72:Pattern =Pattern.compile( "^[0-1]{1}$")
    val patern74:Pattern =Pattern.compile( "^\\+[0-9]{12}$")
    val patern75:Pattern =Pattern.compile("^(\\+[0-9]{12}#\\+[0-9]{12}#[0-9]{1,5},)+$")
    val patern80:Pattern =Pattern.compile("^\\+[0-9]{12}#\\+[0-9]{12}$")
    val paternSetStatusOk:Pattern =Pattern.compile("^\\+[0-9]{12}:[0-1]$")
    val simpleCmnds:Array[String] = Array("60", "61", "63", "64", "68", "75", "76")
    val listCmnds:Array[String] = Array("62", "74")
    val listForIntent:Array[String] = Array("69", "71")
    val listCmndForIntents:Array[String] = Array("70")
    val setStatusCmnd:Array[String] = Array("67")
    val onlyCmnd:Array[String] = Array("65", "66", "70")



  def cutStringFromEnd(s: String, a: Int, b: Int):String={
  s.substring(s.length-a, s.length-b)
  }

  def getHash(maps: mutable.ArrayBuffer[Int]): Int={
    val md = java.security.MessageDigest.getInstance("SHA-1")
//  val hash = new sun.misc.BASE64Encoder().encode(md.digest(maps.))
    3
  }

  def sliceIncomingListsDate(str: String): Array[String]={

   str.split(",")

}

  def makeKeyFromIntetsUsers(idCreator: String, idDestinatio: String): String ={
    idCreator.substring(idCreator.length-7, idCreator.length)+idDestinatio.substring(idDestinatio.length-7, idDestinatio.length)
  }

  def matchIt(str: String, p:Pattern):Boolean={
//    val a=Pattern.compile(p)
    val m:Matcher = p.matcher(str)
    m.matches()
  }

 /* private */def multiEq(ind: String, args:Array[String]): Boolean ={
    for(a <-args){
if (ind==a){return true}
    }
   false
  }

  def verifyIncomingDate(date: String,  p:Pattern): Boolean= {

    val m:Matcher = p.matcher(date);
    m.matches()


  }
}

