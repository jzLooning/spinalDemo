import Cache.mmap
import Tool.Decode
import spinal.core._

class test extends Component {

}
object Main{
  def main(args: Array[String]):Unit = {
    SpinalConfig(targetDirectory = "./build/").generateVerilog(new mmap)
  }
}