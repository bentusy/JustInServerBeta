package attracti.develop.callalign.server.utill

import attracti.develop.callalign.server.intents.IntentConteiner

/**
 * Created by Administrator on 1/20/2015.
 */
object IntentWeightOrder extends Ordering[IntentConteiner] {

  override def compare(x: IntentConteiner, y: IntentConteiner): Int = y.weight - x.weight

}
