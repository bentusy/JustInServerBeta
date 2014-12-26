package attracti.develop.callalign.server.bd

import java.util.Calendar

import akka.actor.ActorRef
import attracti.develop.callalign.server.users.{ProtoIntent, ProtoUser, Intent, User}
import java.sql.{Connection, DriverManager, ResultSet}

import scala.collection.Map
import scala.collection.mutable.ArrayBuffer


class BDHandler {

  // Change to Your Database Config
//  val conn_str = "jdbc:mysql://localhost:3306/DBNAME?user=DBUSER&password=DBPWD"
  // Load the driver
//  classOf[org.postgresql.Driver]
//
//
//  val conn = DriverManager.getConnection(conn_str)
//  try {
//
//    // Configure to be Read Only
//    val statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
//
//    // Execute Query
//    val rs = statement.executeQuery("SELECT quote FROM quotes LIMIT 5")
//
//    // Iterate Over ResultSet
//    while (rs.next) {
//      println(rs.getString("quote"))
//    }
//  }
//  finally {
//    conn.close
//  }
//
//  val statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)
//
//  // do database insert
//  try {
//    val prep = conn.prepareStatement("INSERT INTO quotes (quote, author) VALUES (?, ?) ")
//    prep.setString(1, "Nothing great was ever achieved without enthusiasm.")
//    prep.setString(2, "Ralph Waldo Emerson")
//    prep.executeUpdate
//  }
//  finally {
//    conn.close
//  }


  val conn_str = "jdbc:postgresql://localhost:5432/callalign?user=postgres&password=artemcom"

//  classOf[org.postgresql.Driver]


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


  def saveNewIntent(intent: Intent): Unit = {
    val conn = DriverManager.getConnection(conn_str)
      try {
        val callableStatement = conn.prepareCall("SELECT ins_intent(?, ?, ?, ?)");
        callableStatement.setString(1, intent.id)
        callableStatement.setString(2, intent.idCreator)
        callableStatement.setString(3, intent.idDestination)
        callableStatement.setLong(4, intent.dateToDie.getTimeInMillis())
        callableStatement.execute();
    }
    finally {
      conn.close
    }
  }

  def markNonactualIntent(intents: Intent): Unit = {
    val conn = DriverManager.getConnection(conn_str)
    // do database insert
    try {
      val prep = conn.prepareStatement("UPDATE intents SET actualmark = ? WHERE id = ?")

        prep.setBoolean(1, false)
        prep.setString(2, intents.id)
        prep.executeUpdate
    }
    finally {
      conn.close
    }
  }

  def markNonactualIntents(intents: Map[String, Intent]): Unit = {

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

  def markPreparetoremove(intent: Intent, i: Int){

    val conn = DriverManager.getConnection(conn_str)
    try {
      val pst = conn.prepareStatement("UPDATE intents SET preparetoremove = ? WHERE id = ?;")
      pst.setString(2, intent.id)
      pst.setInt(1, i)
      pst.execute()
    }
    finally {
      conn.close
    }

  }

  def marckSynchronizeIntent(intent: Intent){

    val conn = DriverManager.getConnection(conn_str)
    try {
      val pst = conn.prepareStatement("UPDATE intents SET synchronize = TRUE WHERE id = ?;")
      pst.setString(1, intent.id)
      pst.execute()
    }
    finally {
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
      callableStatement.setArray(2, conn.createArrayOf("text", favorits.keys.toList.toArray.asInstanceOf[Array[AnyRef]]))

      callableStatement.setString(1, id)
      callableStatement.execute();
    }
    finally {
      conn.close
    }
  }

  def addContacts(id: String, simpleContacts: Array[String], regContacts: Map[String, ActorRef]): Unit ={

    val conn = DriverManager.getConnection(conn_str)
    // do database insert
    try {
      val callableStatement = conn.prepareCall("SELECT add_contacts(?, ?, ?)");
      callableStatement.setArray(2, conn.createArrayOf("text", simpleContacts.asInstanceOf[Array[AnyRef]]))
      callableStatement.setArray(3, conn.createArrayOf("text", regContacts.keys.toList.toArray.asInstanceOf[Array[AnyRef]]))
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

  def setContacts(id: String, simpleContacts: Array[String], regContacts: Map[String, ActorRef]): Unit = {

    val conn = DriverManager.getConnection(conn_str)
    // do database insert
    try {
      val callableStatement = conn.prepareCall("SELECT set_contacts(?, ?, ?)");
      callableStatement.setArray(2, conn.createArrayOf("text", simpleContacts.asInstanceOf[Array[AnyRef]]))
      callableStatement.setArray(3, conn.createArrayOf("text", regContacts.keys.toList.toArray.asInstanceOf[Array[AnyRef]]))
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

  def getProtoUsers(): ArrayBuffer[ProtoUser] ={

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
  }



}
