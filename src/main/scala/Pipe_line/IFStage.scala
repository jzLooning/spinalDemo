package Pipe_line
import spinal.core._
class IFStage() extends Component {
    val io_fs_ds = new Bundle {
        val ds_allowin = in Bool()
        val br_bus = in Bits(33 bits)
        val fs_to_ds_valid = out Bool()
        val fs_pc = out Bits(32 bits)
        val fs_inst = out Bits(32 bits)
    }
    val io_mmap = new Bundle {
        val inst_pc = out Bits(32 bits)
        val inst_re = out Bool()
        val inst_ready = in Bool()
        val inst_data = in Bits(32 bits)
    }
    //设置并初始化fs_pc
    val fs_pc = Reg(UInt(32 bits)) init(U"32'h7ffffffc")
    val fs_inst = Bits(32 bits)
    io_fs_ds.fs_pc := fs_pc.asBits
    io_fs_ds.fs_inst := fs_inst.asBits
    //处理br_bus
    val br_taken = io_fs_ds.br_bus(32)
    val br_target = io_fs_ds.br_bus(31 downto 0)

    // pre-IF stage
    val seq_pc = fs_pc + 4
    val next_pc = br_taken ? br_target.asUInt | seq_pc

    //IF stage
    val fs_valid = Reg(Bool()) init(False)
    val fs_ready_go = io_mmap.inst_ready
    val fs_allowin = !fs_valid || fs_ready_go && io_fs_ds.ds_allowin
    fs_valid := True
    io_fs_ds.fs_to_ds_valid := fs_valid && fs_ready_go
    fs_pc := next_pc

    //读取数据
    fs_inst := io_mmap.inst_data
    io_mmap.inst_pc := next_pc.asBits
    io_mmap.inst_re := True
}
