import spinal.core._
class Decode_3_8 extends Component {
  val io = new Bundle {
    val code_3 = in UInt (3 bit)
    val code_8 = out Bits(8 bit)
  }
  for (i <- 0 to 7) {io.code_8(i) := (io.code_3 === i)}
}

object Demo01{
  def main(args: Array[String]):Unit = {
    SpinalConfig(targetDirectory = "./build/").generateVerilog(new Decode_3_8)
  }
}