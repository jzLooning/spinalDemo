
import spinal.core._
import Pipe_line._


class CpuTop extends Component {
    val if_stage = new IFStage
    val id_stage = new IDStage
    val ex_stage = new EXStage
    val mem_stage = new MemStage
    val wb_stage = new WbStage
    val pipe_bubble = new PipeBubble
    val pipe_mmap = new PipeMmap
    // 模块连线
    pipe_mmap.io_inst <> if_stage.io_mmap
    pipe_mmap.io_data <> ex_stage.io_data
    pipe_mmap.io_mem <> mem_stage.io_mem
    pipe_bubble.io_ds_bubble <> id_stage.io_ds_bubble
    pipe_bubble.io_es_bubble <> ex_stage.io_es_bubble
    pipe_bubble.io_ms_bubble <> mem_stage.io_ms_bubble
    if_stage.io_fs_ds <> id_stage.io_fs_ds
    id_stage.io_ds_es <> ex_stage.io_ds_es
    ex_stage.io_es_ms <> mem_stage.io_es_ms
    mem_stage.io_ms_ws <> wb_stage.io_ms_ws
    id_stage.io_ws_ds <> wb_stage.io_ws_ds
}
object Main{
  def main(args: Array[String]):Unit = {
      SpinalConfig(targetDirectory = "./build/",oneFilePerComponent = true).generateVerilog(new CpuTop)
  }
}