package attracti.develop.callalign.server.intents



import akka.actor.{Props, Actor, Cancellable}
import attracti.develop.callalign.server.GlobalContext
import attracti.develop.callalign.server.utill._

import scala.concurrent.Promise

/**
 * Created by Darcklight on 12/1/2014.
 */

object Intent{
  def propsFromUser(mc:IntentConteiner, metaDatac:UsersMetaData,myIndexc:IIndex): Props =
    Props(new Intent(mc,metaDatac,myIndexc))
}

class Intent(val myConteiner:IntentConteiner, val metaDatac:UsersMetaData, val myIndexc:IIndex ) extends Actor {

  implicit val ex = context.system.dispatcher
  var cancellSR:Cancellable=null
  var cancellPromise:(Cancellable, Cancellable)=null

  def weightRefresh(): Unit ={
  myConteiner.weight= 0
  }


  override def receive: Receive = {

    case IntentManagerToIntentTerminate()=>
//      cancellSR.cancel();cancellPromise._1.cancel();cancellPromise._2.cancel();
    context.system.stop(self)

    case IntentCalculatorToIntentWork()=>{
    myConteiner.istatus.value=0
     val sch=context.system.scheduler
    cancellSR=  sch.scheduleOnce(GlobalContext.delayIStatusReturn){ myConteiner.istatus.value=1}

    val pcr=Promise[Int]()
    val pds=Promise[Int]()

    val ccr=  sch.scheduleOnce(GlobalContext.delayIPsStop){ pcr.trySuccess(0)}
    val cds=  sch.scheduleOnce(GlobalContext.delayIPsStop){ pds.trySuccess(0)}
    cancellPromise=(ccr,cds)
   myConteiner.aRefCreator ! IntentToUserGetMeYourStatusPR(pcr)
   myConteiner.aRefDestination ! IntentToUserGetMeYourStatusPR(pds)
    val fcr=pcr.future
    val fds=pds.future
    val rsp =  for{
        rs1<-fcr
        rs2<-fds} yield (rs1, rs2)

    rsp.onSuccess{
      case (1,1)=>myConteiner.aRefCreator ! IntentToUserCall(myConteiner);cancellPromise._1.cancel();cancellPromise._2.cancel();
      case (_,_)=>myConteiner.aRefCreator ! IntentToUserFree(); myConteiner.aRefDestination ! IntentToUserFree()
    }

   }

    case UserToIntentMetaRefresh()=>{
      weightRefresh
    }


}
}
