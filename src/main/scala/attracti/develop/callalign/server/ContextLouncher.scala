package attracti.develop.callalign.server

import java.net.InetSocketAddress

import _root_.org.apache.logging.log4j._
import akka.actor._
import akka.io.{IO, Tcp}
import attracti.develop.callalign.server.bd.{BDManager, BDHandler}
import attracti.develop.callalign.server.conections.TcpConnectionHandlerActor
import attracti.develop.callalign.server.utill.IntentManagerToTcpServerStart

import scala.collection.mutable.{ArrayBuilder => ARB, Map => MMap, Set => MSet}
import scala.util.Properties.{lineSeparator => newLine}

object CallAlign extends App {
//  Configurator.initialize(null, "./resources/log4j2.xml")

  var log:Logger = LogManager.getLogger("InfoLoger")

  val system = ActorSystem("hostsystem")
  val bdHandler= new BDHandler


  val DAO = system.actorOf(Props(new BDManager(bdHandler)), name = "DAO")

  val intentManager = system.actorOf(Props(new IntentManager(DAO)), name = "intentManager")

  val usermeneger = system.actorOf(UserManager.props(intentManager, DAO, bdHandler.getProtoUsers()), name = "userManager")

  val endpoint = new InetSocketAddress("10.0.1.24", 9900) // conf it

  val server = system.actorOf(TCPServerA.props(endpoint, usermeneger), name = "tcpServer")


  Console.readLine()
  system.shutdown()
//  System.exit(0)



}


object TCPServerA {
  def props(endpoint: InetSocketAddress, usermg: ActorRef): Props =
  Props(new TCPServerA(endpoint, usermg))
}



class TCPServerA(endpoint: InetSocketAddress, usermg: ActorRef) extends Actor{

  var log:Logger =LogManager.getLogger("InfoLoger")

  import context.system


  var usermeneger =  usermg

  override def preStart() {

    log.info("Start TCPServerA")
  }

  override def postStop(): Unit ={
    log.info("SOPED TCPServerA")
  }

  override def receive: Receive = {
    case Tcp.Connected(remote, _) => {
      log.info("New TCP connection:" + remote)
      sender ! Tcp.Register(context.actorOf(TcpConnectionHandlerActor.props(remote, sender, usermeneger))) // sender - tcp connection
    }

    case IntentManagerToTcpServerStart => {
      IO(Tcp) ! Tcp.Bind(self, endpoint)
      log.info("All load successfully ♥")
    }

  }

}
