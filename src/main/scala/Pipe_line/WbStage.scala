package Pipe_line
import spinal.core._
class WbStage extends Component {
    val io_ws_ds = new Bundle {
        val rf_we = out Bool()
        val rf_waddr = out Bits(5 bits)
        val rf_wdata = out Bits(32 bits)
    }
    val io_ms_ws = new Bundle {
        val ws_allowin = out Bool()
        val ms_to_ws_valid = in Bool()
        val gr_we = in Bool()
        val dest = in Bits(5 bits)
        val result = in Bits(32 bits)
        val ms_pc = in Bits(32 bits)
    }
    // 流水级信号处理
    val ws_ready_go = True
    val ws_valid = Reg(Bool()) init(false)
    val dest = Reg(Bits(5 bits)) init(0)
    val gr_we = Reg(Bool()) init(false)
    val result = Reg(Bits(32 bits)) init(0)
    val ws_pc = Reg(Bits(32 bits)) init(0)
    val ms_bus_get = io_ms_ws.ws_allowin && io_ms_ws.ms_to_ws_valid
    io_ms_ws.ws_allowin := !ws_valid || ws_ready_go
    when(io_ms_ws.ws_allowin) {
        ws_valid := io_ms_ws.ms_to_ws_valid
    }
    when(ms_bus_get) {
        gr_we := io_ms_ws.gr_we
        dest := io_ms_ws.dest
        result := io_ms_ws.result
        ws_pc := io_ms_ws.ms_pc
    }

    // 寄存器写回
    io_ws_ds.rf_we := gr_we
    io_ws_ds.rf_waddr := dest
    io_ws_ds.rf_wdata := result
}
