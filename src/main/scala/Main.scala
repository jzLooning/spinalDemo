import SpinalHDL_CPU.Tool.Decode
import SpinalHDL_CPU.Cache._
import spinal.core._

class test extends Component {

}
object Main{
  def main(args: Array[String]):Unit = {
    SpinalConfig(targetDirectory = "./build/").generateVerilog(new mmap)
  }
}