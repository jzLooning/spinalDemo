package Pipe_line
import spinal.core._
import spinal.lib.bus.regif.CHeaderGenerator
class MemStage extends Component {
    val io_es_ms = new Bundle {
        val ms_allowin = out Bool()
        val es_to_ms_valid = in Bool()
        val addr_2 = in Bits(2 bits)
        val op_mem_l = in Bool()
        val res_from_mem = in Bool()
        val gr_we = in Bool()
        val dest = in Bits(5 bits)
        val es_to_ms_result = in Bits(32 bits)
        val es_pc = in Bits(32 bits)
    }
    val io_ms_ws = new Bundle {
        val ws_allowin = in Bool()
        val ms_to_ws_valid = out Bool()
        val gr_we = out Bool()
        val dest = out Bits(5 bits)
        val result = out Bits(32 bits)
        val ms_pc = out Bits(32 bits)
    }
    val io_mem = new Bundle {
        val data_data_out = in Bits(32 bits)
        val data_data_ready = in Bool()
    }
    val io_ms_bubble = new Bundle {
        val reg_w_valid = out Bool()
        val dest = out Bits(5 bits)
        val result = out Bits(32 bits)
    }
    // 流水级信号处理
    val es_to_ms_valid = Reg(Bool()) init(false)
    val addr_2 = Reg(Bits(2 bits)) init(0)
    val op_mem_l = Reg(Bool()) init(false)
    val res_from_mem = Reg(Bool()) init(false)
    val gr_we = Reg(Bool()) init(false)
    val dest = Reg(Bits(5 bits)) init(0)
    val es_to_ms_result = Reg(Bits(32 bits) init(0))
    val ms_pc = Reg(Bits(32 bits) init(0))
    val ms_ready_go = io_mem.data_data_ready
    val ms_valid = Reg(Bool()) init(false)
    io_es_ms.ms_allowin := !ms_valid || ms_ready_go && io_ms_ws.ws_allowin
    io_ms_ws.ms_to_ws_valid := ms_valid && ms_ready_go
    val es_bus_get = io_es_ms.es_to_ms_valid && io_es_ms.ms_allowin
    when(io_es_ms.ms_allowin) {
        ms_valid := io_es_ms.es_to_ms_valid
    }
    when(es_bus_get) {
        addr_2 := io_es_ms.addr_2
        op_mem_l := io_es_ms.op_mem_l
        res_from_mem := io_es_ms.res_from_mem
        gr_we :=  io_es_ms.gr_we
        dest := io_es_ms.dest
        es_to_ms_result := es_to_ms_result
        ms_pc := io_es_ms.es_pc
    }

    // 访存部分
    val mem_result = op_mem_l ? io_mem.data_data_out(7 downto 0).asUInt.resize(32).asBits | io_mem.data_data_out
    val final_result = res_from_mem ? mem_result | es_to_ms_result

    // 打包信号到下一级
    io_ms_ws.dest := dest
    io_ms_ws.gr_we := gr_we
    io_ms_ws.result := final_result
    io_ms_ws.ms_pc := ms_pc

    // 流水线前递
    io_ms_bubble.reg_w_valid := ms_valid && gr_we
    io_ms_bubble.dest := dest
    io_ms_bubble.result := final_result
}
