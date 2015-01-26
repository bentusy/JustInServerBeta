package attracti.develop.callalign.server.conections

/**
 * Created by Administrator on 23.12.2014.
 */
class OutgoinFormatTools{

  var pidGen: Short = 0



  def createPckgType0x2b(pid: Int, str: String): Array[Byte] = {
    val typ = Array[Byte](0x2b)
    val byt = str.getBytes()
    val size = byt.length
    val sizea = intToByte(size)
    val header = typ ++ shortToByte(pid.asInstanceOf[Short]) ++ sizea
    val pakage = header ++ byt
    pakage
  }

  def createPckgType0x30(str: String):Array[Byte]={
    val typ = Array[Byte](0x30)
    val byt = str.getBytes()
    val size = byt.length
    val sizea = intToByte(size)
//    print("Создание 30-го протокола");typ.foreach(x=>print(x+" "));print(" sizea ");sizea.foreach(x=>print(x+" "));print(" byt ");byt.foreach(x=>print(x+" "))
//    println()
    val a = typ ++ sizea ++ byt
//   a.foreach(x=>print(x+"p"))

    a
  }

  def createPckgType0X2a(str: String):(Array[Byte], Int)={
    val typ = Array[Byte](0x2a)
    val byt = str.getBytes()
    val size = byt.length
    pidGen = (pidGen + 1.asInstanceOf[Short]).asInstanceOf[Short]
    val pid=pidGen
    val sizea = intToByte(size)
    val header = typ ++ shortToByte(pid.asInstanceOf[Short]) ++ sizea
    val pakage = header ++ byt
    (pakage, pid)
  }


  def createPckgType0X10(): (Array[Byte], Int) = {
    val typ = Array[Byte](0x10)
     pidGen = (pidGen + 1.asInstanceOf[Short]).asInstanceOf[Short]
    val pid=pidGen
     val pakage = typ ++ shortToByte(pidGen)
    (pakage, pid)
  }
  def createPckgType0X11(pid: Int): Array[Byte] = {
    val typ = Array[Byte](0x11)


    val pakage = typ ++ shortToByte(pid.toShort)
    pakage
  }

  def stringToPakageType3(str: String): Array[Byte] = {
    val byt = str.getBytes()
    val size = byt.length
    val sizea = intToByte(size)
    val pakage = Array[Byte](0x2B) ++ sizea ++ byt

    pakage
  }

  def intToByte(i: Int): Array[Byte] = {
    val c = 4
    val arr = new Array[Byte](4)

    arr(0) = (i >> 24).asInstanceOf[Byte]
    arr(1) = (i >> 16).asInstanceOf[Byte];
    arr(2) = (i >> 8).asInstanceOf[Byte];
    arr(3) = i.asInstanceOf[Byte]
    arr
  }

  def shortToByte(i: Short): Array[Byte] = {

    val arr = new Array[Byte](2)
    arr(0) = (i >> 8).asInstanceOf[Byte];
    arr(1) = i.asInstanceOf[Byte]
    arr

  }


}
