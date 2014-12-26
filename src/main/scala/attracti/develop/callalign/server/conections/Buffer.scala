package attracti.develop.callalign.server.conections

/**
 * Created by Administrator on 19.12.2014.
 */

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, Queue}
import attracti.develop.callalign.server.utill.IncomingData

/**
 * Created by Darcklight on 12/5/2014.
 */




class Buffer(collect:TransportProtocolCollection) {

  var buffer = ArrayBuffer[Byte]()
  var newOrTail = true
  var tailType = 0
  var expectDataLength = 0
  var queue = Queue[IncomingData]()
  var pid: Int = 0


  def hasNext(): Boolean = {
    !queue.isEmpty
       }

  def getNext(): IncomingData ={
    queue.dequeue()
  }

  def getQueueSize(): Int = {
    queue.size
  }

  def inComingData(pkg: Seq[Byte]) {


    if (newOrTail == true) {
      pkg(0) match {

        case 0x11=>{
        inComingTyp0x11(pkg)
        }

        case 0x12 => {
        inComingTyp0x12(pkg)
        }

        case 0x2a => {
          inComingTyp0x2A(pkg)
        }

        case 0x2b => {
          inComingTyp0x2B(pkg)
        }

        case 0x30 => {
          inComingType0x30(pkg)
        }
        case _=>queue +=IncomingData(0,null,0)
      }

    } else {
      tailType match {
        case 0x11=>{
          inComingTyp0x11(pkg)
        }

        case 0x12 => {
          inComingTyp0x12(pkg)
        }

        case 0x2a => {
          inComingTyp0x2A(pkg)
        }

        case 0x2b => {
          inComingTyp0x2B(pkg)
        }

        case 0x30 => {
          inComingType0x30(pkg)
        }
        case _=>queue +=IncomingData(0,null,0)
      }
      }
    }



  def inComingType0x30(pkg: Seq[Byte]) {

    if (newOrTail) {
      if(pkg.length<5){
        buffer++=pkg
        newOrTail = false
        tailType = 0x30
        return
      }
      expectDataLength = byteToInt(pkg, 1)
//      pid = byteToShort(pkg, 1)

      if (expectDataLength == pkg.length - 5) {
        //      val s = new String(pkg.slice(6, pkg.length))
        queue += IncomingData(0x30, pkg.slice(5, pkg.length), 0)
        expectDataLength = 0
        pid = 0
        return
      }

      if (expectDataLength > pkg.length - 5) {
        buffer ++= pkg//.slice(7, pkg.length)
        newOrTail = false
        tailType = 0x30
        return
      }

      if (expectDataLength < pkg.length - 5) {
        //      val dat = pkg.slice(6, pkg.length)
        val tempArr = pkg.slice(5, 5 + expectDataLength)
        queue += IncomingData(0x30, tempArr, 0)
        inComingData(pkg.slice(5 + expectDataLength, pkg.length))
        return
      }
    } else {
      buffer++=pkg
      if(buffer.length<5){return }else{ expectDataLength = byteToInt(buffer, 1)}

      if (buffer.length < expectDataLength+5) {
        //        buffer ++= pkg
        return
      }

      if (buffer.length == expectDataLength+5) {
        //        val t = Array[Byte]() ++ buffer ++ pkg
        queue += IncomingData(0x30, buffer.slice(5, buffer.length), 0)
        buffer.clear()
        newOrTail = true
        expectDataLength = 0

        return
      }

      if (buffer.length > expectDataLength+7) {
        //        val trim = expectDataLength - buffer.length
        //        buffer ++= pkg.slice(0, trim)
        queue += IncomingData(0x30, buffer.slice(5, expectDataLength+5), 0)
        val tar= buffer.slice(expectDataLength+5, buffer.length)
        buffer.clear()
        newOrTail = true
        inComingData(tar)
        return
      }
    }
  }

  def inComingTyp0x2A(pkg: Seq[Byte]) {

       if (newOrTail) {
         if(pkg.length<7){
           buffer++=pkg
           newOrTail = false
           tailType = 0x2A
           return
         }
       expectDataLength = byteToInt(pkg, 3)
       pid = byteToShort(pkg, 1)

      if (expectDataLength == pkg.length - 7) {
        //      val s = new String(pkg.slice(6, pkg.length))
        queue += IncomingData(0x2a, pkg.slice(7, pkg.length), pid)
        expectDataLength = 0
        pid = 0
        return
      }

      if (expectDataLength > pkg.length - 7) {
        buffer ++= pkg//.slice(7, pkg.length)
        newOrTail = false
        tailType = 0x2A
        return
      }

      if (expectDataLength < pkg.length - 7) {
        //      val dat = pkg.slice(6, pkg.length)
        val tempArr = pkg.slice(7, 7 + expectDataLength)
        queue += IncomingData(0x2a, tempArr, pid)
        inComingData(pkg.slice(7 + expectDataLength, pkg.length))
        return
      }
    } else {
         buffer++=pkg
      if(buffer.length<7){return }else{ expectDataLength = byteToInt(buffer, 3);pid = byteToShort(pkg, 1)}

      if (buffer.length < expectDataLength+7) {
//        buffer ++= pkg
        return
      }

      if (buffer.length == expectDataLength+7) {
//        val t = Array[Byte]() ++ buffer ++ pkg
        queue += IncomingData(0x2a, buffer.slice(7, buffer.length), pid)
        buffer.clear()
        newOrTail = true
        expectDataLength = 0
        pid = 0
        return
      }

      if (buffer.length > expectDataLength+7) {
//        val trim = expectDataLength - buffer.length
//        buffer ++= pkg.slice(0, trim)
        queue += IncomingData(0x2a, buffer.slice(7, expectDataLength+7), pid)
        val tar= buffer.slice(expectDataLength+7, buffer.length)
        buffer.clear()
        newOrTail = true
        inComingData(tar)
        return
      }
    }
  }

  def inComingTyp0x2B(pkg: Seq[Byte]) {

    if (newOrTail) {
      if(pkg.length<7){
        buffer++=pkg
        newOrTail = false
        tailType = 0x2B
        return
      }
      expectDataLength = byteToInt(pkg, 3)
      pid = byteToShort(pkg, 1)

      if (expectDataLength == pkg.length - 7) {
        //      val s = new String(pkg.slice(6, pkg.length))
        queue += IncomingData(0x2B, pkg.slice(7, pkg.length), pid)
        expectDataLength = 0
        pid = 0
        return
      }

      if (expectDataLength > pkg.length - 7) {
        buffer ++= pkg//.slice(7, pkg.length)
        newOrTail = false
        tailType = 0x2B
        return
      }

      if (expectDataLength < pkg.length - 7) {
        //      val dat = pkg.slice(6, pkg.length)
        val tempArr = pkg.slice(7, 7 + expectDataLength)
        queue += IncomingData(0x2B, tempArr, pid)
        inComingData(pkg.slice(7 + expectDataLength, pkg.length))
        return
      }
    } else {
      buffer++=pkg
      if(buffer.length<7){return }else{ expectDataLength = byteToInt(buffer, 3);pid = byteToShort(pkg, 1) }

      if (buffer.length < expectDataLength+7) {
        //        buffer ++= pkg
        return
      }

      if (buffer.length == expectDataLength+7) {
        //        val t = Array[Byte]() ++ buffer ++ pkg
        queue += IncomingData(0x2B, buffer.slice(7, buffer.length), pid)
        buffer.clear()
        newOrTail = true
        expectDataLength = 0
        pid = 0
        return
      }

      if (buffer.length > expectDataLength+7) {
        //        val trim = expectDataLength - buffer.length
        //        buffer ++= pkg.slice(0, trim)
        queue += IncomingData(0x2B, buffer.slice(7, expectDataLength+7), pid)
        val tar = buffer.slice(expectDataLength+7, buffer.length)
        buffer.clear()
        newOrTail = true
        inComingData(tar)
        return
      }
    }
  }
  def inComingTyp0x12(pkg: Seq[Byte]){
    if(newOrTail){
      if(pkg.length<2){
        //        buffer+=pkg(0)
        newOrTail = false
        tailType = 0x12
        return
      }
      if(pkg.length==2){
        queue += IncomingData(0x12, pkg.slice(1,2),0)
        return
      }
      if (pkg.length>2){
        queue += IncomingData(0x12, pkg.slice(1,2),0)
        inComingData(pkg.slice(2, pkg.length))
        return
      }
    }else{

      queue += IncomingData(0x12, pkg.slice(0,1),0)
      newOrTail = true
      if(pkg.length>1){
        inComingData(pkg.slice(1, pkg.length))
      }

    }
  }

  def inComingTyp0x11(pkg: Seq[Byte]): Unit = {
    if(newOrTail){
      if(pkg.length<3){
        buffer++=pkg
        newOrTail = false
        tailType = 0x11
        return
      }
      if(pkg.length==3){
        queue += IncomingData(0x11, null,byteToShort(pkg, 1))
        return
        }
      if (pkg.length>3){
        queue += IncomingData(0x11, null,byteToShort(pkg, 1))
        inComingData(pkg.slice(2, pkg.length))
        return
        }
    }else{
        buffer ++= pkg
        if(buffer.length==3){
        queue += IncomingData(0x11,null,byteToShort(buffer, 1))
        newOrTail = true
          tailType=0
          buffer.clear()
        }
      if(buffer.length>3){
        queue += IncomingData(0x11,null,byteToShort(buffer, 1))
        newOrTail = true
        tailType=0
        val t = buffer.slice(3, buffer.length)
        buffer.clear()
        inComingData(t)
      }
    }

  }





  def byteToInt(arr: Seq[Byte], startIndx: Int): Int = {
    val c = 4
    var int: Int = 0
    var i = 0
    while (i < c) {
      int = (int << 8) + arr(startIndx + i).asInstanceOf[Int]
      i += 1
    }
    int
  }


  def byteToShort(arr: Seq[Byte], startIndx: Int): Short = {
    val c = 2
    var sh: Short = 0
    var i = 0
    while (i < c) {
      sh = ((sh << 8) + arr(startIndx + i).asInstanceOf[Short]).asInstanceOf[Short]
      i += 1
    }
    sh
  }

}