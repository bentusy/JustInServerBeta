package attracti.develop.callalign.server.bd

import java.util.Calendar

import akka.actor.ActorRef
import attracti.develop.callalign.server.GlobalContext
import attracti.develop.callalign.server.intents.{UsersMetaData, IntentConteiner, Intent}
import attracti.develop.callalign.server.users.{User}
import java.sql.{Connection, DriverManager, ResultSet}

import attracti.develop.callalign.server.utill._

import scala.collection.Map
import scala.collection.mutable.{ListBuffer, ArrayBuffer}
import com.typesafe.config._

import scala.tools.nsc.Global

class BDHandler {


  var conn_str:String = GlobalContext.bdUri



  def addRegContacts(userId: String,regContact: String): Unit ={
    val conn = DriverManager.getConnection(conn_str)
    // do database insert
    try {
      val callableStatement = conn.prepareCall("SELECT add_regcontact(?, ?)");
      callableStatement.setString(1, userId)
      callableStatement.setString(2, regContact)
      callableStatement.execute();
    }
    finally {
      conn.close
    }
  }

  def saveNewUser(user: User): Unit ={
    val conn = DriverManager.getConnection(conn_str)
    // do database insert
    try {
      val callableStatement = conn.prepareCall("SELECT ins_user(?, ?)");
      callableStatement.setString(1, user.id)
      callableStatement.setString(2, user.countryCod)
      callableStatement.execute();
    }
    finally {
      conn.close
    }

  }


  def saveNewMeta(m: UsersMetaData): Unit ={
    val conn = DriverManager.getConnection(conn_str)
    try {
      val callableStatement = conn.prepareCall("SELECT ins_meta(?, ?, ?, ?, ?, ?, ?)");
      callableStatement.setString(1, m.id)
      callableStatement.setString(2, m.user1Id)
      callableStatement.setString(3, m.user2Id)
      callableStatement.setInt(4, m.u1Tou2CallTime)
      callableStatement.setInt(5, m.u1Tou2CallCount)
      callableStatement.setInt(6, m.u2Tou1CallTime)
      callableStatement.setInt(7, m.u2Tou1CallCount)
      callableStatement.execute();
    }
    finally {
      conn.close
    }
  }

  def updMeta(m: UsersMetaData): Unit ={
    val conn = DriverManager.getConnection(conn_str)
    // do database insert
    try {
      val callableStatement = conn.prepareCall("SELECT upd_metas(?, ?, ?, ?)");
      callableStatement.setString(1, m.id)
      callableStatement.setInt(2, m.u1Tou2CallTime)
      callableStatement.setInt(3, m.u1Tou2CallCount)
      callableStatement.setInt(4, m.u2Tou1CallTime)
      callableStatement.setInt(5, m.u2Tou1CallCount)

      callableStatement.execute();
    } finally {
      conn.close
    }
  }

  def updIIndex(ic:IntentConteiner): Unit ={
    val conn = DriverManager.getConnection(conn_str)
    try {
      val callableStatement = conn.prepareCall("SELECT upd_iindex(?, ?, ?)");
      callableStatement.setString(1, ic.iid)
      callableStatement.setInt(2, ic.inedx.indx)
      callableStatement.setBoolean(3, ic.inedx.tf)
      callableStatement.execute();
    }
    finally {
      conn.close
    }
  }


  def saveNewIntent(intent: IntentConteiner): Unit = {
    val conn = DriverManager.getConnection(conn_str)
      try {
        val callableStatement = conn.prepareCall("SELECT ins_intent(?, ?, ?, ?, ?, ?)");
        callableStatement.setString(1, intent.iid)
        callableStatement.setString(2, intent.idCreator)
        callableStatement.setString(3, intent.idDestination)
        callableStatement.setLong(4, intent.dateToDie.getTimeInMillis())
        callableStatement.setInt(5, intent.inedx.indx)
        callableStatement.setBoolean(6, intent.inedx.tf)

        callableStatement.execute();
    }
    finally {
      conn.close
    }
  }

  def markNonactualIntent(intents: IntentConteiner): Unit = {
    println("Помечаю не актуальный интент-1")
    val conn = DriverManager.getConnection(conn_str)
    // do database insert
    try {
      val prep = conn.prepareCall("SELECT nonactual_intent(?)")
        prep.setString(1, intents.iid)
        prep.execute()
    }
    finally {
      conn.close
    }
  }

  def markNonactualIntents(intents: Map[String, IntentConteiner]): Unit = {
    println("Помечаю не актуальныу интенты")
    val conn = DriverManager.getConnection(conn_str)
    // do database insert
    try {
      val callableStatement = conn.prepareCall("SELECT mark_intents(?, ?)");
      callableStatement.setArray(1, conn.createArrayOf("text", intents.keys.toList.toArray.asInstanceOf[Array[AnyRef]]))
      callableStatement.setBoolean(2, false)
      callableStatement.execute();
   }
    finally {
      conn.close
    }
  }

  def markPreparetoremove(intent: IntentConteiner, i: Int){
    println("Помечаю подготовку на удаление")
    val conn = DriverManager.getConnection(conn_str)
        try {
      val pst = conn.prepareCall("SELECT preparetoremove_intent(?, ?)")
      pst.setString(1, intent.iid)
      pst.setInt(2, i)
      pst.execute()
    }
    finally {
      conn.close
    }

  }

  def marckSynchronize(i:IntentConteiner){
    val conn = DriverManager.getConnection(conn_str)
    // do database insert
    try {
    val callableStatement = conn.prepareStatement("UPDATE intents SET synchronize = 't' WHERE id = ?")
    callableStatement.setString(1, i.iid)
    //  callableStatement.setBoolean(2, false)
    callableStatement.execute();
    } finally {
          conn.close
    }
  }



  def removeFromSeeings(id: String, seei: Array[String]): Unit = {
    val conn = DriverManager.getConnection(conn_str)
    // do database insert
    try {
      val callableStatement = conn.prepareCall("SELECT remove_seeings(?, ?)");
      callableStatement.setArray(2, conn.createArrayOf("text", seei.asInstanceOf[Array[AnyRef]]))
      callableStatement.setString(1, id)
      callableStatement.execute();
    }
    finally {
      conn.close
    }
  }

  def addForSeeings(id: String, seei: Array[String]): Unit = {
    val conn = DriverManager.getConnection(conn_str)
    // do database insert
    try {
      val callableStatement = conn.prepareCall("SELECT add_seeings(?, ?)");
      callableStatement.setArray(2, conn.createArrayOf("text", seei.asInstanceOf[Array[AnyRef]]))
      callableStatement.setString(1, id)
      callableStatement.execute();
    }
    finally {
      conn.close
    }
  }

  def setSeeings(id: String, seei: Map[String, ActorRef]): Unit = {
    val conn = DriverManager.getConnection(conn_str)
   // do database insert
    try {
      val callableStatement = conn.prepareCall("SELECT set_seeings(?, ?)");
      callableStatement.setArray(2, conn.createArrayOf("text", seei.keys.toList.toArray.asInstanceOf[Array[AnyRef]]))
      callableStatement.setString(1, id)
      callableStatement.execute();
    }
    finally {
      conn.close
    }
  }

  def RemoveFromFavoritList(id: String, favorits: Array[String]): Unit ={
    val conn = DriverManager.getConnection(conn_str)
   // do database insert
    try {
      val callableStatement = conn.prepareCall("SELECT remove_favorits(?, ?)");
      callableStatement.setArray(2, conn.createArrayOf("text", favorits.asInstanceOf[Array[AnyRef]]))
      callableStatement.setString(1, id)
      callableStatement.execute();
    }
    finally {
      conn.close
    }
  }

  def addToFavoritList(id: String, favorits: Array[String]): Unit ={

    val conn = DriverManager.getConnection(conn_str)
    // do database insert
    try {
      val callableStatement = conn.prepareCall("SELECT add_favorits(?, ?)");
      callableStatement.setArray(2, conn.createArrayOf("text", favorits.asInstanceOf[Array[AnyRef]]))
      callableStatement.setString(1, id)
      callableStatement.execute();
    }
    finally {
      conn.close
    }

  }

  def setFavoritList(id: String, favorits: Map[String, ActorRef]): Unit ={

    val conn = DriverManager.getConnection(conn_str)
    // do database insert
    try {
      val callableStatement = conn.prepareCall("SELECT set_favorits(?, ?)");
      callableStatement.setArray(2, conn.createArrayOf("text", favorits.keys.toArray.asInstanceOf[Array[AnyRef]]))

      callableStatement.setString(1, id)
      callableStatement.execute();
    }
    finally {
      conn.close
    }
  }

  def addContacts(id: String, simpleContacts: Array[String], regContacts:Iterable[String]): Unit ={

    val conn = DriverManager.getConnection(conn_str)
    // do database insert
    try {
      val callableStatement = conn.prepareCall("SELECT add_contacts(?, ?, ?)");
      callableStatement.setArray(2, conn.createArrayOf("text", simpleContacts.asInstanceOf[Array[AnyRef]]))
      callableStatement.setArray(3, conn.createArrayOf("text", regContacts.toArray.asInstanceOf[Array[AnyRef]]))
      callableStatement.setString(1, id)
      callableStatement.execute();
    }
    finally {
      conn.close
    }

  }
  def removeContacts(id: String, contacts: Array[String]): Unit ={


    val conn = DriverManager.getConnection(conn_str)
    // do database insert
    try {
      val callableStatement = conn.prepareCall("SELECT remove_contacts(?, ?)");
      callableStatement.setArray(2, conn.createArrayOf("text", contacts.asInstanceOf[Array[AnyRef]]))
      callableStatement.setString(1, id)
      callableStatement.execute();
    }
    finally {
      conn.close
    }


  }

  def setContacts(id: String, simpleContacts: Array[String], regContacts: Iterable[String]): Unit = {

    val conn = DriverManager.getConnection(conn_str)
    // do database insert
    try {
      val callableStatement = conn.prepareCall("SELECT set_contacts(?, ?, ?)");
      callableStatement.setArray(2, conn.createArrayOf("text", simpleContacts.asInstanceOf[Array[AnyRef]]))
      callableStatement.setArray(3, conn.createArrayOf("text", regContacts.toArray.asInstanceOf[Array[AnyRef]]))
      callableStatement.setString(1, id)
      callableStatement.execute();
    }
    finally {
      conn.close
    }
    
  }
  def checkArrayForNull(a: java.sql.Array): Array[String] = {
    if (a != null) {
      return a.getArray().asInstanceOf[Array[String]]
    } else {
      return Array[String]()
    }
  }

  def loadAll(um: ActorRef): Unit ={
    val conn = DriverManager.getConnection(conn_str)

    try {
      val pusers = ArrayBuffer[ProtoUser]()
      val pi = ArrayBuffer[ProtoIntent]()
      val pm = ArrayBuffer[ProtoMetaData]()

      val st = conn.createStatement()
      val rs = st.executeQuery("SELECT * from users");
      while (rs.next()) {
        pusers += new ProtoUser(rs.getString("id"),
          rs.getString("countrycod"),
          checkArrayForNull(rs.getArray("contacts")),
          checkArrayForNull(rs.getArray("regcontacts")),
          checkArrayForNull(rs.getArray("favorits")),
          checkArrayForNull(rs.getArray("seeings"))
        )
      }

      val rs2 = st.executeQuery("SELECT * from intents WHERE actualmark = true");
      while (rs2.next()) {
        pi += new ProtoIntent(rs2.getString("id"),
          rs2.getString("idcreator"),
          rs2.getString("iddestination"),
          rs2.getLong("datetodie"),
          rs2.getInt("preparetoremove"),
          rs2.getBoolean("synchronize"),
          rs2.getBoolean("index_manual_chng"),
          rs2.getInt("index_order")
        )
      }
      val rs3 = st.executeQuery("SELECT * from metas");
      while (rs3.next()) {
        pm += new ProtoMetaData(
        rs3.getString("id_meta"),
        rs3.getString("id_user1"),
        rs3.getString("id_user2"),
        rs3.getInt("users1_to_user2_calltime"),
        rs3.getInt("users1_to_user2_callcount"),
        rs3.getInt("users2_to_user1_calltime"),
        rs3.getInt("users2_to_user1_callcount")
        )
      }
      println("LoadFromBdComplete")
      um ! BdManagerToUserManagerLoad(pusers, pi, pm)
    }finally {
      conn.close()
    }
  }

 /* def getProtoUsers(): ArrayBuffer[ProtoUser] ={

    val arr = ArrayBuffer[ProtoUser]()
    val conn = DriverManager.getConnection(conn_str)
    try {
      val st = conn.createStatement()
      val rs = st.executeQuery("SELECT * from users");




      while(rs.next()){
        val b = rs.getArray("contacts")

      arr.append(new ProtoUser(rs.getString("id"), rs.getString("countrycod"),
        checkArrayForNull(rs.getArray("contacts")),
        checkArrayForNull(rs.getArray("regcontacts")),
        checkArrayForNull(rs.getArray("favorits")),
        checkArrayForNull(rs.getArray("seeings"))
      ))
      }
  }
    finally {
    conn.close
  }
    arr
  }

  def getProtoIntent(): ArrayBuffer[ProtoIntent] ={
    val arr = ArrayBuffer[ProtoIntent]()
    val conn = DriverManager.getConnection(conn_str)
    try {
      val st = conn.createStatement()
      val rs = st.executeQuery("SELECT * from intents WHERE actualmark = true");
      while(rs.next()){
        arr.append(new ProtoIntent(rs.getString("id"), rs.getString("idcreator"),
          rs.getString("iddestination"), rs.getLong("datetodie"), rs.getInt("preparetoremove"), rs.getBoolean("synchronize"))
        )
      }
    }
    finally {
      conn.close
    }
    arr
  }*/



}
