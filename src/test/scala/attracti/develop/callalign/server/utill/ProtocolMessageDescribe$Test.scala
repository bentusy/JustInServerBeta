package attracti.develop.callalign.server.utill
import attracti.develop.callalign.server.utill.ProtocolMessageDescribe._
import org.scalatest.{Matchers, FlatSpec}

/**
 * Created by Darcklight on 12/4/2014.
 */
class ProtocolMessageDescribe$Test extends FlatSpec with Matchers {

  "Tests of TCP describes" should "return string value for protocol" in {


    println(error("67", "1121"))
    error("67", "1121") should be ("01/67/1121\n")
    regOk("+380995450041") should be ("11/+380995450041/\n")
    autSuccessful("+380995450041") should be ("12/+380995450041/\n")
  }




}
