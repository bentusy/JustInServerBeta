package attracti.develop.callalign.server.intents

import java.util.Calendar

import akka.actor.ActorRef
import attracti.develop.callalign.server.utill.Utils

/**
 * Created by Administrator on 1/19/2015.
 */
case class UsersMetaData(user1Id: String, user1Aref: ActorRef, user2Id: String, user2Aref: ActorRef,var u1Tou2CallTime: Int=0,var u1Tou2CallCount: Int=0,
                         var u2Tou1CallTime: Int=0,var u2Tou1CallCount: Int=0){

  var lastChangeDate:Calendar=Calendar.getInstance()
  var id = Utils.makeKeyFromUsersId(user1Id, user2Id)
  val idRevers = Utils.makeKeyFromUsersId(user2Id, user1Id)

  def this(idm:String, user1Id: String, user1Aref: ActorRef, user2Id: String, user2Aref: ActorRef, u1Tou2CallTime: Int, u1Tou2CallCount: Int,
   u2Tou1CallTime: Int, u2Tou1CallCount: Int){
    this(user1Id, user1Aref, user2Id, user2Aref, u1Tou2CallTime, u1Tou2CallCount, u2Tou1CallTime, u2Tou1CallCount)
    id=idm
  }

  def isMy(id1:String, id2:String): Boolean ={
  if((id1==user1Id&&id2==user2Id)||(id1==user2Id&&id2==user1Id)) true else false
  }


  def chng(seter: Int=>Unit,geter: Int,value: Int): Unit ={
    if(geter <= value)seter(value)
  }


   def update(from: String, outCallTime: Int, outCallCount:Int, inCallTime:Int, inCallCount:Int): Unit ={
     this.synchronized{
      from match {
       case user1Id=>
        chng(u1Tou2CallCount_=,u1Tou2CallCount,outCallCount);
        chng(u2Tou1CallCount_=,u2Tou1CallCount,inCallCount);
        chng(u2Tou1CallTime_=,u2Tou1CallTime,inCallTime);
        chng(u1Tou2CallTime_=,u1Tou2CallTime,outCallTime);
       case user2Id=>
        chng(u1Tou2CallCount_=,u1Tou2CallCount,inCallCount);
        chng(u2Tou1CallCount_=,u2Tou1CallCount,outCallCount);
        chng(u2Tou1CallTime_=,u2Tou1CallTime,outCallTime);
        chng(u1Tou2CallTime_=,u1Tou2CallTime,inCallTime);
      }
     }
  }

  def getSecondUserId(id:String):String={
    if(id==user1Id) user2Id
    else
    if(id==user2Id) user1Id
    else
      null
}

  def getSecondUserRef(implicit ref:ActorRef):ActorRef=
  if(ref==user1Aref) user2Aref
  else
  if(ref==user2Aref) user1Aref
  else
  null
}