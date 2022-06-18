
import Cache.{ICache, mmap}
import spinal.core._


class test extends Component {
    val cache = new ICache
    val mmap_ = new mmap
    cache.io_mmap <> mmap_.io_inst
}
object Main{
  def main(args: Array[String]):Unit = {
      SpinalConfig(targetDirectory = "./build/",oneFilePerComponent = true).generateVerilog(new test)
  }
}