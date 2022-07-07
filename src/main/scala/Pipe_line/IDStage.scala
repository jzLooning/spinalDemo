package Pipe_line

import spinal.core._
import Tool.Decode
import spinal.core.internals.Operator
class IDStage() extends Component {
    val io_fs_ds = new Bundle {
        val ds_allowin = out Bool()
        val fs_to_ds_valid = in Bool()
        val fs_inst = in Bits(32 bits)
        val fs_pc = in Bits(32 bits)
        val br_bus = out Bits(33 bits)
    }
    val io_ds_es = new Bundle {
        val es_allowin = in Bool()
        val ds_to_es_valid = out Bool()
        val op_st = out Bool()
        val op_mem_l = out Bool()
        val alu_op = out Bits(9 bits)
        val load_op = out Bool()
        val src1_is_sa = out Bool()
        val src1_is_pc = out Bool()
        val src2_is_imm = out Bool()
        val src2_is_8 = out Bool()
        val gr_we = out Bool()
        val mem_we = out Bool()
        val dest = out Bits(5 bits)
        val imm = out Bits(16 bits)
        val rs_value = out Bits(32 bits)
        val rt_value = out Bits(32 bits)
        val ds_pc = out Bits(32 bits)
        val imm_zexi = out Bool()
    }
    val io_ws_ds = new Bundle {
        val rf_we = in Bool()
        val rf_waddr = in Bits(5 bits)
        val rf_wdata = in Bits(32 bits)
    }

    val io_ds_bubble = new Bundle {
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

    val configPipeline = new ConfigPipeline

    //设置流水级信号
    val ds_valid = Reg(Bool()) init(False)
    when(io_fs_ds.ds_allowin) {
        ds_valid := io_fs_ds.fs_to_ds_valid
    }
    val ds_ready_go = io_ds_bubble.ds_ready_go_i
    io_fs_ds.ds_allowin := !ds_valid || ds_ready_go && io_ds_es.es_allowin
    io_ds_es.ds_to_es_valid := ds_valid && ds_ready_go

    //设置if送过来的数据
    val ds_pc = Reg(Bits(32 bits)) init(0)
    val ds_inst = Reg(Bits(32 bits)) init(0)
    when(io_fs_ds.fs_to_ds_valid && io_fs_ds.ds_allowin) {
        ds_pc := io_fs_ds.fs_pc
        ds_inst := io_fs_ds.fs_inst
    }


    //解码
    val op = ds_inst(31 downto 26)
    val rs = ds_inst(25 downto 21)
    val rt = ds_inst(20 downto 16)
    val rd = ds_inst(15 downto 11)
    val sa = ds_inst(10 downto 6)
    val func = ds_inst(5 downto 0)
    val imm = ds_inst(15 downto 0)
    val jidx = ds_inst(25 downto 0)

        // 译码器
    val decoder_op = new Decode(6,64)
    val decoder_func = new Decode(6,64)
    val decoder_rs = new Decode(5,32)
    val decoder_rt = new Decode(5,32)
    val decoder_rd = new Decode(5,32)
    val decoder_sa = new Decode(5,32)
    val op_d = decoder_op.io.output
    val func_d = decoder_func.io.output
    val rs_d = decoder_rs.io.output
    val rt_d = decoder_rt.io.output
    val rd_d = decoder_rd.io.output
    val sa_d = decoder_sa.io.output
    decoder_op.io.input := op.asUInt
    decoder_func.io.input := func.asUInt
    decoder_rs.io.input := rs.asUInt
    decoder_rt.io.input := rt.asUInt
    decoder_rd.io.input := rd.asUInt
    decoder_sa.io.input := sa.asUInt

        //指令解码
    val inst_or = op_d(U"6'h00") && func_d(U"6'h25") && sa_d(U"5'h00")
    val inst_xor = op_d(U"6'h00") && func_d(U"6'h26") && sa_d(U"5'h00")
    val inst_and = op_d(U"6'h00") && func_d(U"6'h24") && sa_d(U"5'h00")
    val inst_andi = op_d(U"6'h0c")
    val inst_ori = op_d(U"6'h0d")
    val inst_xori = op_d(U"6'h0e")
    val inst_addu = op_d(U"6'h00") && func_d(U"6'h21") && sa_d(U"5'h00")
    val inst_addiu = op_d(U"6'h09")
    val inst_mul = op_d(U"6'h1c") && func_d(U"6'h02") && sa_d(U"5'h00")
    val inst_sub = op_d(U"6'h00") && func_d(U"6'h22") && sa_d(U"5'h00")
    val inst_sll = op_d(U"6'h00") && func_d(U"6'h00") && rs_d(U"5'h00")
    val inst_srl = op_d(U"6'h00") && func_d(U"6'h02") && rs_d(U"5'h00")
    val inst_sllv = op_d(U"6'h00") && func_d(U"6'h04") && sa_d(U"5'h00");
    val inst_beq = op_d(U"6'h04")
    val inst_bne = op_d(U"6'h05")
    val inst_blez = op_d(U"6'h06") && rt_d(U"5'h00")
    val inst_bgtz = op_d(U"6'h07") && rt_d(U"5'h00")
    val inst_j = op_d(U"6'h02")
    val inst_jal = op_d(U"6'h03")
    val inst_jr = op_d(U"6'h00") && func_d(U"6'h08") && rt_d(U"5'h00") && rd_d(U"5'h00") && sa_d(U"5'h00")
    val inst_lb = op_d(U"6'h20")
    val inst_lw = op_d(U"6'h23")
    val inst_lui = op_d(U"6'h0f") && rs_d(U"5'h00")
    val inst_sb = op_d(U"6'h28")
    val inst_sw = op_d(U"6'h2b")

    // 设置ALU_OP
    val alu_op = Bits(9 bits)
    alu_op(configPipeline.ALU_OP.add) := inst_addu || inst_addiu || inst_lw || inst_sw || inst_lb || inst_sb
    alu_op(configPipeline.ALU_OP.sub) := inst_sub
    alu_op(configPipeline.ALU_OP.and) := inst_and || inst_andi
    alu_op(configPipeline.ALU_OP.or) := inst_or || inst_ori
    alu_op(configPipeline.ALU_OP.xor) := inst_xor || inst_xori
    alu_op(configPipeline.ALU_OP.sll) := inst_sll || inst_sllv
    alu_op(configPipeline.ALU_OP.srl) := inst_srl
    alu_op(configPipeline.ALU_OP.lui) := inst_lui
    alu_op(configPipeline.ALU_OP.mul) := inst_mul

    //设置相关控制信号
    val contral_sign = new Bundle {
        val src1_is_sa = inst_sll || inst_srl
        val src1_is_pc = inst_jal
        val src2_is_imm = inst_addiu || inst_ori || inst_xori || inst_lw || inst_sw || inst_lb || inst_sb || inst_lui
        val src2_is_8 = inst_jal
        val load_op = inst_lw || inst_lb
        val dst_is_r31 = inst_jal
        val dst_is_rt = inst_addiu || inst_lui || inst_lw || inst_andi || inst_ori || inst_xori || inst_lb
        val gr_we = ~inst_sw && ~inst_beq && ~inst_bne && ~inst_jr && ~inst_bgtz && ~inst_blez && ~inst_j && ~inst_sb
        val imm_zexi = inst_ori || inst_andi || inst_xori
        val op_mem_l = inst_lb
        val op_st = inst_sb
        val mem_we = inst_sw || inst_sb
        val dest = dst_is_r31 ? B"5'd31" | (dst_is_rt ? rt.asBits | rd.asBits)
    }

    //读写寄存器和流水线前递
    val my_regfile = new Regfile
    my_regfile.io.raddr1 := rs.asUInt
    my_regfile.io.raddr2 := rt.asUInt
    my_regfile.io.we := io_ws_ds.rf_we
    my_regfile.io.waddr := io_ws_ds.rf_waddr.asUInt
    my_regfile.io.wdata := io_ws_ds.rf_wdata

    val rs_value = io_ds_bubble.rs_bypass_enabled ? io_ds_bubble.rs_bypass_value | my_regfile.io.rdata1
    val rt_value = io_ds_bubble.rt_bypass_enabled ? io_ds_bubble.rt_bypass_value | my_regfile.io.rdata2

    //设置跳转指令
    val rs_eq_rt = rs_value === rt_value
    val re_lg_zero = (rs_value.asSInt === 0) || (rs_value(31).asUInt === 1)
    val re_gt_zero = (rs_value =/= 0) && (rs_value(0).asUInt === 0)
    val br_taken = inst_bne && !rs_eq_rt || inst_beq && rs_eq_rt || inst_blez && re_lg_zero || inst_bgtz && re_gt_zero || inst_jr || inst_j || inst_jal
    val is_b_inst = inst_beq || inst_bne || inst_bgtz || inst_blez
    val br_target = is_b_inst ? (io_fs_ds.fs_pc.asSInt + (imm ## B"2'b00").asSInt) | (inst_jr ? rs_value.asSInt | (io_fs_ds.fs_pc(31 downto 28) ## jidx(25 downto 0) ## B"2'b0").asSInt)

    io_fs_ds.br_bus := br_taken.asBits(1 bits) ## br_target.asBits

    //打包上车送入下一级
    io_ds_es.op_st := contral_sign.op_st
    io_ds_es.op_mem_l := contral_sign.op_mem_l
    io_ds_es.alu_op := alu_op
    io_ds_es.load_op := contral_sign.load_op
    io_ds_es.src1_is_sa := contral_sign.src1_is_sa
    io_ds_es.src1_is_pc := contral_sign.src1_is_pc
    io_ds_es.src2_is_imm := contral_sign.src2_is_imm
    io_ds_es.src2_is_8 := contral_sign.src2_is_8
    io_ds_es.gr_we := contral_sign.gr_we
    io_ds_es.mem_we := contral_sign.mem_we
    io_ds_es.dest := contral_sign.dest
    io_ds_es.imm := imm
    io_ds_es.rs_value := rs_value
    io_ds_es.rt_value := rt_value
    io_ds_es.ds_pc := ds_pc
    io_ds_es.imm_zexi := contral_sign.imm_zexi

    //设置流水线前递信号
    io_ds_bubble.rs := rs
    io_ds_bubble.rt := rt
    io_ds_bubble.rs_read := inst_or || inst_xor || inst_and || inst_andi || inst_ori || inst_addu || inst_addiu || inst_mul || inst_sub || inst_sllv || inst_beq  || inst_bne || inst_blez || inst_bgtz || inst_jr
    io_ds_bubble.rt_read := inst_or || inst_xor || inst_and || inst_addu || inst_mul || inst_sub || inst_sll || inst_srl || inst_sllv || inst_beq || inst_bne || inst_lb || inst_lw || inst_sb || inst_sw
}
