package attracti.develop.callalign.server.users

import java.util.Calendar

import akka.actor.ActorRef

/**
 * Created by Darcklight on 12/11/2014.
 */

class ProtoIntent(idc: String, idCreatorc: String, idDestinationc: String, timeToDiec: Long, prepareToRemovc: Int, synchronizec: Boolean)  {

    val id=idc
  val idCreator =idCreatorc
  val idDestination = idDestinationc
  val timeToDie=timeToDiec
  val prepareToRemove=prepareToRemovc
  val synchronize=synchronizec
  
}
