package attracti.develop.callalign.server.users

import akka.actor.ActorRef

import scala.collection.Map
import scala.collection.mutable.ArrayBuffer

/**
 * Created by Darcklight on 12/11/2014.
 */
class ProtoUser (idc: String, countrycodc: String, contactsc: Array[String],regcontactsc: Array[String], favoritsc:Array[String],
                 seeingsc: Array[String] ){

    val id = idc
  val countrycod=countrycodc
  val contacts = contactsc
  val regcontacts = regcontactsc
  val favorits = favoritsc
  val seeings = seeingsc



}
