package Pipe_line

import spinal.core._

class PipeMmap extends Component {
    /**
     * 处理CPU指令读写请求
     */
    val io_inst = new Bundle {

      val inst_pc = in Bits(32 bits)
      val inst_re = in Bool()
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
        val data_data_out = out Bits(32 bits)
        val data_byte_en = in Bits(4 bits)
        val data_data_ready = out Bool()
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

    }
    val mem_option : Bool = new Bool()


    /**
     * 综合处理cpu发过来的请求，优先处理inst请求
     */

    val mem_request : Bool = io_inst.inst_re || io_data.data_we || io_data.data_re
    val request_addr : Bits = (io_data.data_we || io_data.data_re) ? io_data.data_addr | io_inst.inst_pc
    io_data.data_data_ready := True
    io_inst.inst_ready := !(io_data.data_we || io_data.data_re)

    when (!mem_option && (io_inst.inst_re || io_data.data_re) && !io_data.data_we) {
      io_ram.base_ram_re_n.clear()
    } otherwise {
      io_ram.base_ram_re_n.set()
    }
    when (!mem_option && io_data.data_we) {
      io_ram.base_ram_we_n.clear()
    } otherwise {
      io_ram.base_ram_we_n.set()
    }
    when (mem_option && (io_inst.inst_re || io_data.data_re) && !io_data.data_we) {
      io_ram.ext_ram_re_n.clear()
    } otherwise {
      io_ram.ext_ram_re_n.set()
    }
    when (mem_option  && io_data.data_we) {
      io_ram.ext_ram_we_n.clear()
    } otherwise {
      io_ram.ext_ram_we_n.set()
    }

    /**
     * 内存片选
     */
    mem_option := request_addr(22)
    io_ram.base_ram_ce_n := mem_option
    io_ram.ext_ram_ce_n := !mem_option

    /**
     * 请求地址
     */
    io_ram.base_ram_addr := request_addr(21 downto 2)
    io_ram.ext_ram_addr := request_addr(21 downto 2)

    /**
     * 处理收到的信号
     */
    val data_from_ram : Bits = mem_option ? io_ram.ext_ram_data | io_ram.base_ram_data
    io_data.data_data_out := data_from_ram
    io_inst.inst_data := data_from_ram

    /**
     * 写数据
     */
    when(io_data.data_we) {
      when(mem_option) {
        io_ram.ext_ram_data := io_data.data_data_in
      }.otherwise {
        io_ram.base_ram_data := io_data.data_data_in
      }
    }
    when(io_data.data_we || io_data.data_re) {
        io_ram.base_ram_be_n := ~io_data.data_byte_en
        io_ram.ext_ram_be_n := ~io_data.data_byte_en
    } otherwise {
        io_ram.base_ram_be_n := 0x0
        io_ram.ext_ram_be_n := 0x0
    }
}
