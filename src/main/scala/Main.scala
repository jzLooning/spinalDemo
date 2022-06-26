
import Cache.{ICache, mmap}
import spinal.core._
import Pipe_line._


class CpuTop extends Component {
    val a = SInt(32 bits)
    val b = SInt(16 bits)
    val c = SInt(32 bits)
    a := 12
    b := -13
    c := a + b
}
object Main{
  def main(args: Array[String]):Unit = {
      SpinalConfig(targetDirectory = "./build/",oneFilePerComponent = true).generateVerilog(new CpuTop)
  }
}