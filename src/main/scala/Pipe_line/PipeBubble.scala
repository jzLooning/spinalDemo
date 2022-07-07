package Pipe_line
import spinal.core._
class PipeBubble extends Component {
    val io_ds_bubble = new Bundle {
      val rs_read = in Bool()
      val rt_read = in Bool()
      val rs = in Bits(5 bits)
      val rt = in Bits(5 bits)

      val ds_ready_go_i = out Bool()
      val rs_bypass_enabled = out Bool()
      val rt_bypass_enabled = out Bool()
      val rs_bypass_value = out Bits(32 bits)
      val rt_bypass_value = out Bits(32 bits)
    }

    val io_es_bubble = new Bundle {
      val reg_l_valid = in Bool()
      val reg_w_valid = in Bool()
      val dest = in Bits(5 bits)
      val result = in Bits(32 bits)
    }

    val io_ms_bubble = new Bundle {
      val reg_w_valid = in Bool()
      val dest = in Bits(5 bits)
      val result = in Bits(32 bits)
    }

    val io_ws_bubble = new Bundle {
        val reg_w_valid = in Bool()
        val dest = in Bits(5 bits)
        val result = in Bits(32 bits)
    }

    // 阻塞:只有lw和lb指令会阻塞
    io_ds_bubble.ds_ready_go_i := !(io_es_bubble.reg_l_valid && (io_ds_bubble.rs_read && (io_ds_bubble.rs === io_es_bubble.dest) || io_ds_bubble.rt_read && (io_ds_bubble.rt === io_es_bubble.dest)))

    // 流水线前递
    val es_rs_sign = io_es_bubble.reg_w_valid && io_ds_bubble.rs_read && (io_ds_bubble.rs === io_es_bubble.dest)
    val es_rt_sign = io_es_bubble.reg_w_valid && io_ds_bubble.rt_read && (io_ds_bubble.rt === io_es_bubble.dest)
    val ms_rs_sign = io_ms_bubble.reg_w_valid && io_ds_bubble.rs_read && (io_ds_bubble.rs === io_ms_bubble.dest)
    val ms_rt_sign = io_ms_bubble.reg_w_valid && io_ds_bubble.rt_read && (io_ds_bubble.rt === io_ms_bubble.dest)
    val ws_rs_sign = io_ws_bubble.reg_w_valid && io_ds_bubble.rs_read && (io_ds_bubble.rs === io_ws_bubble.dest)
    val ws_rt_sign = io_ws_bubble.reg_w_valid && io_ds_bubble.rt_read && (io_ds_bubble.rt === io_ws_bubble.dest)
    io_ds_bubble.rs_bypass_enabled := es_rs_sign || ms_rs_sign || ws_rs_sign
    io_ds_bubble.rt_bypass_enabled := es_rt_sign || ms_rt_sign || ws_rt_sign
    io_ds_bubble.rs_bypass_value := es_rs_sign ? io_es_bubble.result | (ms_rs_sign ? io_ms_bubble.result | io_ws_bubble.result)
    io_ds_bubble.rt_bypass_value := es_rt_sign ? io_es_bubble.result | (ms_rt_sign ? io_ms_bubble.result | io_ws_bubble.result)
}
