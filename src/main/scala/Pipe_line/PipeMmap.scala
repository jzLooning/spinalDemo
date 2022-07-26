package Pipe_line

import spinal.core._

class PipeMmap extends Component {
    /**
     *  处理CPU指令读写请求
     */
    val io_inst = new Bundle {
        val inst_pc = in Bits(32 bits)
        val inst_ready = out Bool()
        val inst_data = out Bits(32 bits)
    }

    /**
     * 处理CPU数据读写请求
     */
    val io_data = new Bundle {
          val data_data_in = in Bits(32 bits)
          val data_addr = in Bits(32 bits)
          val data_re = in Bool()
          val data_we = in Bool()
          val data_byte_en = in Bits(4 bits)
    }
    /**
     * 访存的mem阶段
     */
    val io_mem = new Bundle {
          val data_data_out = out Bits(32 bits)
    }

    /**
     * 处理base_ram读写,处理ext_ram读写
     */
    val io_ram = new Bundle {
        val base_ram_data = inout (Analog(Bits(32 bits)))
        val base_ram_addr = out Bits(20 bits)
        val base_ram_be_n = out Bits(4 bits)
        val base_ram_ce_n = out Bool()
        val base_ram_re_n = out Bool()
        val base_ram_we_n = out Bool()


        val ext_ram_data = inout (Analog(Bits(32 bits)))
        val ext_ram_addr = out Bits(20 bits)
        val ext_ram_be_n = out Bits(4 bits)
        val ext_ram_ce_n = out Bool()
        val ext_ram_re_n = out Bool()
        val ext_ram_we_n = out Bool()

        val serial_rdata_en_sign = out Bool()
        val serial_rdata_en_data = out Bool()
        val serial_rdata = in Bits(8 bits)
        val serial_wdata = out Bits(8 bits)
        val serial_wdata_en = out Bool()
    }
    // 定义低电平的信号
    val n_enable = False
    val n_byte_enable = B"4'h0"
    val n_disable = True

    // 增加reg保护信号
    val base_ram_addr_r = Reg(Bits(20 bits)) init(0)
    val base_ram_be_n_r = Reg(Bits(4 bits)) init(0)
    val base_ram_ce_n_r = Reg(Bool()) init(True)
    val base_ram_re_n_r = Reg(Bool()) init(True)
    val base_ram_we_n_r = Reg(Bool()) init(True)

    val base_ram_rdata = Bits(32 bits)


    val ext_ram_addr_r = Reg(Bits(20 bits)) init(0)
    val ext_ram_be_n_r = Reg(Bits(4 bits)) init(0)
    val ext_ram_ce_n_r = Reg(Bool()) init(True)
    val ext_ram_re_n_r = Reg(Bool()) init(True)
    val ext_ram_we_n_r = Reg(Bool()) init(True)

    val ext_ram_rdata = Bits(32 bits)

    val serial_rdata_en_sign_r = Reg(Bool()) init(False)
    val serial_rdata_en_data_r = Reg(Bool()) init(False)
    val serial_wdata_r = Reg(Bits(8 bits)) init(0)
    val serial_wdata_en_r = Reg(Bool()) init(False)

    val inst_ready = Reg(Bool()) init(True)

    val ram_wdata = RegNext(io_data.data_data_in)
    base_ram_rdata := io_ram.base_ram_data
    ext_ram_rdata := io_ram.ext_ram_data

    // 设置初始状态,inst取指base_ram，ex写ext_ram
    inst_ready := True
    base_ram_addr_r := io_inst.inst_pc(21 downto 2)
    base_ram_be_n_r := n_byte_enable
    base_ram_ce_n_r := n_enable
    base_ram_re_n_r := n_enable
    base_ram_we_n_r := n_disable

    ext_ram_addr_r := io_data.data_addr(21 downto 2)
    ext_ram_be_n_r := ~io_data.data_byte_en
    ext_ram_ce_n_r := n_enable
    ext_ram_re_n_r := n_disable
    ext_ram_we_n_r := n_enable

    serial_rdata_en_sign_r := False
    serial_rdata_en_data_r := False
    serial_wdata_r := io_data.data_data_in(7 downto 0)
    serial_wdata_en_r := False

    when(io_data.data_we) {
        // 写数据
        when(io_data.data_addr === B"32'hbfd003f8") {
            // 写串口，不会与inst产生冲突
            ext_ram_ce_n_r := n_disable
            ext_ram_we_n_r := n_disable
            ext_ram_re_n_r := n_disable
            serial_wdata_en_r := True
        } elsewhen(io_data.data_addr(22)) {
            // 写ext_ram
        } otherwise {
            // 写base_ram
            inst_ready := False
            base_ram_addr_r := io_data.data_addr(21 downto 2)
            base_ram_be_n_r := ~io_data.data_byte_en
            base_ram_re_n_r := n_disable
            base_ram_we_n_r := n_enable

            ext_ram_ce_n_r := n_disable
            ext_ram_we_n_r := n_disable
        }
    } elsewhen(io_data.data_re) {
        // 读数据
        when(io_data.data_addr === B"32'hbfd003fc") {
            // 读串口信号
            ext_ram_ce_n_r := n_disable
            ext_ram_we_n_r := n_disable
            ext_ram_re_n_r := n_disable

            serial_rdata_en_sign_r := True
        } elsewhen(io_data.data_addr === B"32'hbfd003f8") {
            // 读串口数据
            ext_ram_ce_n_r := n_disable
            ext_ram_we_n_r := n_disable
            ext_ram_re_n_r := n_disable

            serial_rdata_en_data_r := True
        } elsewhen(io_data.data_addr(22)) {
            // 读ext_ram
            ext_ram_we_n_r := n_disable
            ext_ram_re_n_r := n_enable
        } otherwise {
            // 读base_ram
            inst_ready := False
            base_ram_addr_r := io_data.data_addr(21 downto 2)
            base_ram_be_n_r := ~io_data.data_byte_en

            ext_ram_ce_n_r := n_disable
            ext_ram_we_n_r := n_disable
            ext_ram_re_n_r := n_disable
        }
    } otherwise {
        // 不读也不写
        ext_ram_ce_n_r := n_disable
        ext_ram_we_n_r := n_disable
        ext_ram_re_n_r := n_disable
    }

    when(base_ram_we_n_r === n_enable) {
        io_ram.base_ram_data := ram_wdata
    }
    when(ext_ram_we_n_r === n_enable) {
        io_ram.ext_ram_data := ram_wdata
    }

    io_ram.base_ram_addr := base_ram_addr_r
    io_ram.base_ram_ce_n := base_ram_ce_n_r
    io_ram.base_ram_be_n := base_ram_be_n_r
    io_ram.base_ram_we_n := base_ram_we_n_r
    io_ram.base_ram_re_n := base_ram_re_n_r

    io_ram.ext_ram_addr := ext_ram_addr_r
    io_ram.ext_ram_ce_n := ext_ram_ce_n_r
    io_ram.ext_ram_be_n := ext_ram_be_n_r
    io_ram.ext_ram_re_n := ext_ram_re_n_r
    io_ram.ext_ram_we_n := ext_ram_we_n_r

    io_ram.serial_rdata_en_sign := serial_rdata_en_sign_r
    io_ram.serial_rdata_en_data := serial_rdata_en_data_r
    io_ram.serial_wdata := io_data.data_data_in(7 downto 0)
    io_ram.serial_wdata_en := serial_wdata_en_r

    io_inst.inst_ready := inst_ready
    io_inst.inst_data := base_ram_rdata
    io_mem.data_data_out := (serial_rdata_en_data_r || serial_rdata_en_sign_r) ? (B"24'h000000" ## io_ram.serial_rdata) | (ext_ram_re_n_r ? base_ram_rdata | ext_ram_rdata)
////    val serial_rdata_en_data = Bool()
////    val serial_rdata = in Bits(8 bits)
////    val serial_wdata = out Bits(8 bits)
////    val serial_wdata_en = out Bool()
//
//
//
//    // 串口
//    val data_to_serial_req_sign = RegNext(io_data.data_re && (io_data.data_addr === B"32'hbfd003fc"))
//    val data_to_serial_req_data_r = RegNext(io_data.data_re && (io_data.data_addr === B"32'hbfd003f8"))
//    val data_to_serial_req_data_w = io_data.data_we && (io_data.data_addr === B"32'hbfd003f8")
//    val data_to_serial_req_data = data_to_serial_req_data_r || data_to_serial_req_data_w
//    val data_to_serial_req =  data_to_serial_req_sign || data_to_serial_req_data
//    io_ram.serial_rdata_en_sign := data_to_serial_req_sign
//    io_ram.serial_rdata_en_data := data_to_serial_req_data_r
//    io_ram.serial_wdata_en := data_to_serial_req_data_w
//    val serial_r_data = io_ram.serial_rdata.asSInt.resize(32 bits).asBits
//    io_ram.serial_wdata := io_data.data_data_in(7 downto 0)
//
//    // 处理inst请求
//    val req_inst = io_inst.inst_re
//    val ram_en_inst = io_inst.inst_pc(22)
//
//    // 处理data请求
//    val req_data = (io_data.data_re || io_data.data_we) && !data_to_serial_req
//    val ram_en_data = io_data.data_addr(22)
//
//    // 处理交互请求
//    val inst_ready = Bool();
//    val inst_ready_r = RegNext(inst_ready)
//    io_inst.inst_ready := inst_ready_r && inst_ready
//    io_mem.data_data_ready := True
//        // data 优先
//    when(req_inst && req_data && (ram_en_inst === ram_en_data)) {
//        inst_ready := False
//    } otherwise {
//        inst_ready := True
//    }
//    val ram_en_data_r = RegNext(ram_en_data)
//    // 处理下层内存请求
//    when(~(base_ram_we_n_r && ext_ram_we_n_r)) {
//        when(ram_en_data_r) {
//            io_ram.ext_ram_data := wdata
//        }otherwise {
//            io_ram.base_ram_data := wdata
//        }
//    }
//    base_ram_addr_r := (req_data && !ram_en_data) ? io_data.data_addr(21 downto 2).asBits | io_inst.inst_pc(21 downto 2).asBits
//    base_ram_be_n_r := (req_data && !ram_en_data) ? ~io_data.data_byte_en | B"4'h0"
//    base_ram_ce_n_r := !req_inst && !req_data || !req_inst && ram_en_data || ram_en_data && ram_en_inst || !req_data && ram_en_inst
//    base_ram_re_n_r := !(req_inst || req_data)
//    base_ram_we_n_r := ~io_data.data_we || ram_en_data
//
//
//    ext_ram_addr_r :=  (req_data && ram_en_data) ? io_data.data_addr(21 downto 2).asBits | io_inst.inst_pc(21 downto 2).asBits
//    ext_ram_be_n_r :=  (req_data && ram_en_data) ? ~io_data.data_byte_en | B"4'h0"
//    ext_ram_ce_n_r :=  !req_inst && !req_data || !req_inst && !ram_en_data || !ram_en_inst && !ram_en_data || !req_data && !ram_en_inst
//    ext_ram_re_n_r := !(req_inst || req_data)
//    ext_ram_we_n_r := ~io_data.data_we || ~ram_en_data
//
//    io_inst.inst_data := ram_en_inst ? io_ram.ext_ram_data | io_ram.base_ram_data
//    io_mem.data_data_out := (data_to_serial_req_sign || data_to_serial_req_data_r) ? serial_r_data | (ram_en_data_r ? io_ram.ext_ram_data | io_ram.base_ram_data)
//
//    io_ram.base_ram_addr := base_ram_addr_r
//    io_ram.base_ram_be_n := base_ram_be_n_r
//    io_ram.base_ram_ce_n := base_ram_ce_n_r
//    io_ram.base_ram_re_n := base_ram_re_n_r
//    io_ram.base_ram_we_n := base_ram_we_n_r
//
//    io_ram.ext_ram_addr := ext_ram_addr_r
//    io_ram.ext_ram_be_n := ext_ram_be_n_r
//    io_ram.ext_ram_ce_n := ext_ram_ce_n_r
//    io_ram.ext_ram_re_n := ext_ram_re_n_r
//    io_ram.ext_ram_we_n := ext_ram_we_n_r

}