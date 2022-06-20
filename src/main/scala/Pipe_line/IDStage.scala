package Pipe_line

import spinal.core._
import Tool.Decode
class IDStage(configPipeline: ConfigPipeline) extends Component {
    val io_fs_ds = new Bundle {
        val ds_allowin = out Bool()
        val fs_to_ds_valid = in Bool()
        val fs_to_ds_bus = in Bits(configPipeline.FS_TO_DS_BUS_WD bits)
        val br_bus = out Bits(configPipeline.BR_BUS_WD bits)
    }
    val io_ds_es = new Bundle {
        val es_allowin = in Bool()
        val ds_to_es_valid = out Bool()
        val ds_to_es_bus = out Bits(configPipeline.DS_TO_ES_BUS_WD bits)
    }
    val io_ws_ds = new Bundle {
        val ws_to_rf_bus = in Bits(configPipeline.WS_TO_RF_BUS_WD bits)
    }

    val io_ds_bubble = new Bundle {
        val dest = out Bits(5 bits)
        val rs_read = out Bool()
        val rt_read = out Bool()
        val rs = out Bits(5 bits)
        val rt = out Bits(5 bits)

        val ds_ready_go_i = in Bool()
        val rs_bypass_enabled = in Bool()
        val rt_bypass_enabled = in Bool()
        val rs_bypass_value = in Bits(32 bits)
        val rt_bypass_value = in Bits(32 bits)
    }

    //设置流水级信号
    val ds_valid = Reg(Bool()) init(False)
    when(io_fs_ds.ds_allowin) {
        ds_valid := io_fs_ds.fs_to_ds_valid
    }
    val ds_ready_go = io_ds_bubble.ds_ready_go_i
    io_fs_ds.ds_allowin := !ds_valid || ds_ready_go && io_ds_es.es_allowin
    io_ds_es.ds_to_es_valid := ds_valid && ds_ready_go

    //设置if送过来的数据
    val fs_to_ds_bus_r = Reg(Bits(configPipeline.FS_TO_DS_BUS_WD bits)) init(0)
    when(io_fs_ds.fs_to_ds_valid && io_fs_ds.ds_allowin) {
        fs_to_ds_bus_r := io_fs_ds.fs_to_ds_bus
    }
    val ds_inst = fs_to_ds_bus_r(31 downto 0).asBits
    val ds_pc = fs_to_ds_bus_r(63 downto 32)

    //解码
    val op = ds_inst(31 downto 26)
    val rs = ds_inst(25 downto 21)
    val rt = ds_inst(20 downto 16)
    val rd = ds_inst(15 downto 11)
    val sa = ds_inst(10 downto 6)
    val func = ds_inst(5 downto 0)
    val imm = ds_inst(15 downto 0)
    val jidx = ds_inst(25 downto 0)

    val decoder_op = new Decode(6,64)
    decoder_op.io.input := op
    val op_d = decoder_op.io.output

    val decoder_rs = new Decode(6,64)
    decoder_rs.io.input := rs
    val rs_d = decoder_rs.io.output

    val deocode_rt = new Decode()


}
