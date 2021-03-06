package attracti.develop.callalign.server.bd

import akka.actor.{ActorRef, Actor}
import akka.actor.Actor.Receive
import attracti.develop.callalign.server.users.{Intent, User}
import attracti.develop.callalign.server.utill._
import org.apache.logging.log4j.{LogManager, Logger}
import scala.collection.Map
import scala.collection.mutable.ArrayBuffer

/**
 * Created by Darcklight on 12/8/2014.
 */
class BDManager (bdHandlerc: BDHandler) extends Actor{
  val bdHandler:BDHandler=bdHandlerc
  val log: Logger = LogManager.getLogger("InfoLoger")
 override def preStart(): Unit ={
   log.info("Start BD Manager")
 }

  override def receive: Receive = {

    case UserToDbManagerMarkIntentsSynchronize(i)=>{
      bdHandler.marckSynchronizeIntent(i)
    }

    case UserToDbManagerMarkIntentsPreparetoremove(i, inOut)=>{
      bdHandler.markPreparetoremove(i, inOut)
    }

    case UserToBdManagerAddRegContacs(id, regId)=>{
      bdHandler.addRegContacts(id, regId)
    }
    
    case UserManagerToBDManagerLoadIntents(glum, intemanager)=>{
    intemanager !  BDManagerToIntentManagerCreatIntents(glum, bdHandler.getProtoIntent())
    }
    case UserManagerToBDSaveNewUser(usr: User)=>{
     bdHandler.saveNewUser(usr)
    }

    case UserToBdSetContacts(id, contacts, regContacts)=>{
    bdHandler.setContacts(id, contacts, regContacts)
    }


    case UserToBdRemoveContacts(id, contactsForRemove)=>{
      bdHandler.removeContacts(id, contactsForRemove)
    }

    case UserToBDAddContacts(id, newContacts, newRegContacts)=>{
    bdHandler.addContacts(id, newContacts.toList.toArray, newRegContacts)
    }

    case UserToBDSetFavoritList(id, favorits)=>{
    bdHandler.setFavoritList(id,favorits)
    }

    case UserToBDAddToFavoritList(id, listToAdd)=>{
    bdHandler.addToFavoritList(id, listToAdd)
    }

    case UserToBDRemoveFromFavoritList(id, listForRemove)=>{
    bdHandler.RemoveFromFavoritList(id, listForRemove)
    }

    case UserToBDSetSeeings(id, seeingList) =>{
    bdHandler.setSeeings(id, seeingList)
    }

    case UserToBDAddForSeeings(id, list)=>{
    bdHandler.addForSeeings(id, list)
    }

    case UserToBDRemoveFromSeeings(id, listForRemove)=>{
    bdHandler.removeFromSeeings(id, listForRemove)
    }

    case IntenteManagerToBDSaveNewIntent(intnt: Intent)=>{
    bdHandler.saveNewIntent(intnt)
    }

    case IntentManagerToBDMarkNonactualIntents(intents)=>{
    bdHandler.markNonactualIntents(intents)
    }

    case IntentManagerToBDMarkNonactualIntent(intent)=>{
    bdHandler.markNonactualIntent(intent)
    }


  }
}
