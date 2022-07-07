package Pipe_line

import spinal.core._
import Tool.Alu
class EXStage extends Component {
    val io_ds_es = new Bundle {
        val es_allowin = out Bool()
        val ds_to_es_valid = in Bool()
        val op_st = in Bool()
        val op_mem_l = in Bool()
        val alu_op = in Bits(9 bits)
        val load_op = in Bool()
        val src1_is_sa = in Bool()
        val src1_is_pc = in Bool()
        val src2_is_imm = in Bool()
        val src2_is_8 = in Bool()
        val gr_we = in Bool()
        val mem_we = in Bool()
        val dest = in Bits(5 bits)
        val imm = in Bits(16 bits)
        val rs_value = in Bits(32 bits)
        val rt_value = in Bits(32 bits)
        val ds_pc = in Bits(32 bits)
        val imm_zexi = in Bool()
    }
    val io_es_ms = new Bundle {
        val ms_allowin = in Bool()
        val es_to_ms_valid = out Bool()
        val addr_2 = out Bits(2 bits)
        val op_mem_l = out Bool()
        val res_from_mem = out Bool()
        val gr_we = out Bool()
        val dest = out Bits(5 bits)
        val es_to_ms_result = out  Bits(32 bits)
        val es_pc = out Bits(32 bits)

    }
    val io_es_bubble = new Bundle {
        val reg_l_valid = out Bool()
        val reg_w_valid = out Bool()
        val dest = out Bits(5 bits)
        val result = out Bits(32 bits)
    }
    val io_data = new Bundle {
        val data_data_in = out Bits (32 bits)
        val data_addr = out Bits(32 bits)
        val data_re = out Bool()
        val data_we = out Bool()
        val data_byte_en = out Bits(4 bits)
    }
    // 流水级信号处理
    val es_valid = Reg(Bool()) init(false)
    val ds_bus_get = io_ds_es.ds_to_es_valid && io_ds_es.es_allowin
    val op_st = Reg(Bool()) init (false)
    val op_mem_l = Reg(Bool()) init (false)
    val alu_op = Reg(Bits(9 bits)) init (0)
    val load_op = Reg(Bool()) init (false)
    val src1_is_sa = Reg(Bool()) init (false)
    val src1_is_pc = Reg(Bool()) init (false)
    val src2_is_imm = Reg(Bool()) init (false)
    val src2_is_8 = Reg(Bool()) init (false)
    val gr_we = Reg(Bool()) init (false)
    val mem_we = Reg(Bool()) init (false)
    val dest = Reg(Bits(5 bits)) init (0)
    val imm = Reg(Bits(16 bits)) init (0)
    val rs_value = Reg(Bits(32 bits)) init (0)
    val rt_value = Reg(Bits(32 bits)) init (0)
    val es_pc = Reg(Bits(32 bits)) init (0)
    val imm_zexi = Reg(Bool()) init (false)
    val es_ready_go = True
    io_es_ms.es_to_ms_valid := es_valid && es_ready_go
    when(io_ds_es.es_allowin) {
        es_valid := io_ds_es.ds_to_es_valid
    }
    when(ds_bus_get){
        op_st := io_ds_es.op_st
        op_mem_l := io_ds_es.op_mem_l
        alu_op := io_ds_es.alu_op
        load_op := io_ds_es.load_op
        src1_is_sa := io_ds_es.src1_is_sa
        src1_is_pc := io_ds_es.src1_is_pc
        src2_is_imm := io_ds_es.src2_is_imm
        src2_is_8 := io_ds_es.src2_is_8
        gr_we := io_ds_es.gr_we
        mem_we := io_ds_es.mem_we
        dest := io_ds_es.dest
        imm := io_ds_es.imm
        rs_value := io_ds_es.rs_value
        rt_value := io_ds_es.rt_value
        es_pc := io_ds_es.ds_pc
        imm_zexi := io_ds_es.imm_zexi
    }
    io_ds_es.es_allowin := !es_valid || es_ready_go && io_es_ms.ms_allowin
    when(io_ds_es.es_allowin) {es_valid := io_ds_es.ds_to_es_valid}

    // 处理数据
    val alu_src1 = src1_is_sa ? imm(10 downto 6).asUInt.resize(32).asBits | (src1_is_pc ? es_pc | rs_value)
    val src2_imm = imm_zexi ? imm.asUInt.resize(32).asBits | imm.asSInt.resize(32).asBits
    val alu_src2 = src2_is_imm ? src2_imm | (src2_is_8 ? B"32'd8" | rt_value)
    val alu = new Alu
    alu.io.alu_op := alu_op
    alu.io.alu_src1 := alu_src1
    alu.io.alu_src2 := alu_src2
    val alu_result = alu.io.alu_result

    // 处理访存
    val st_value = rt_value(7 downto 0)##rt_value(7 downto 0)##rt_value(7 downto 0)##rt_value(7 downto 0)
    io_data.data_data_in := op_st ? st_value | rt_value
    io_data.data_addr := alu_result
    val byte_enable = Bits(4 bits)
    switch(alu_result(1 downto 0)){
        is(B"2'd0") {
            byte_enable := B"4'b0001"
        }
        is(B"2'd1") {
            byte_enable := B"4'b0010"
        }
        is(B"2'd2") {
            byte_enable := B"4'b0100"
        }
        is(B"2'd3") {
            byte_enable := B"4'b1000"
        }
    }
    io_data.data_byte_en := op_st ? byte_enable | B"4'b1111"
    io_data.data_we := mem_we && es_valid
    io_data.data_re := load_op && es_valid

    // 下一级信号
    io_es_ms.addr_2 := alu_result(1 downto 0)
    io_es_ms.op_mem_l := op_mem_l
    io_es_ms.res_from_mem := load_op
    io_es_ms.gr_we := gr_we
    io_es_ms.dest := dest
    io_es_ms.es_to_ms_result := alu_result
    io_es_ms.es_pc := es_pc

    // 流水线前递
    io_es_bubble.reg_l_valid := es_valid && !src2_is_8 && load_op
    io_es_bubble.reg_w_valid := es_valid && !src2_is_8 && gr_we && !load_op
    io_es_bubble.dest := dest
    io_es_bubble.result := alu_result
}
