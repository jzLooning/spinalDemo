package tool

import spinal.core._


/**
 * Decode(a,b): 实现一个a到b的译码器
 */
class Decode(in_bit:Int,out_bit:Int) extends Component{
    val io = new Bundle {
        val input = in UInt (in_bit bits)
        val output = out Bits (out_bit bits)
    }
    for (i <- 0 to out_bit-1) {io.output(i) := (io.input === i)}
}
