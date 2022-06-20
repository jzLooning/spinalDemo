
import Cache.{ICache, mmap}
import spinal.core._
import Pipe_line._


class CpuTop extends Component {
    val config = new ConfigPipeline
    val if_stage = new IFStage(config)
    val mmap = new PipeMmap
    mmap.io_inst <> if_stage.io_mmap
}
object Main{
  def main(args: Array[String]):Unit = {
      SpinalConfig(targetDirectory = "./build/",oneFilePerComponent = true).generateVerilog(new CpuTop)
  }
}