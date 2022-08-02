package Tool

import Pipe_line.ConfigPipeline
import spinal.core._
class Alu extends Component {
    val io = new Bundle{
        val ready = out Bool()
        val alu_op = in Bits(11 bits)
        val alu_src1 = in Bits(32 bits)
        val alu_src2 = in Bits(32 bits)
        val alu_result = out Bits(32 bits)
    }
    val config = new ConfigPipeline

    val adder_op = io.alu_op(config.ALU_OP.sub) || io.alu_op(9)
    val adder_a = io.alu_src1
    val adder_b = adder_op ? ~io.alu_src2 | io.alu_src2
    val adder_result = adder_a.asUInt + adder_b.asUInt + adder_op.asUInt(32 bits)

    val add_sub_result = adder_result
    val and_result = io.alu_src1 & io.alu_src2
    val or_result = io.alu_src1 | io.alu_src2
    val xor_result = io.alu_src1 ^ io.alu_src2
    val lui_result = io.alu_src2(15 downto 0) ## B"16'b0"
    val sll_result = io.alu_src2 |<< io.alu_src1(5 downto 0).asUInt
    val srl_result = io.alu_src2 |>> io.alu_src1(5 downto 0).asUInt
    val mul_result = io.alu_src1.asSInt * io.alu_src2.asSInt
    val slt_result = adder_result(31).asUInt.resize(32)
    val sra_result = io.alu_src2.asSInt >> io.alu_src1(5 downto 0).asUInt
    val mul_result_r = RegNext(mul_result(31 downto 0))

    io.alu_result := io.alu_op.mux(
        1 -> adder_result.asBits,
        2 -> adder_result.asBits,
        4 -> and_result.asBits,
        8 -> or_result.asBits,
        16 -> xor_result.asBits,
        32 -> sll_result.asBits,
        64 -> srl_result.asBits,
        128 -> lui_result.asBits,
        256 -> mul_result_r.asBits,
        512 -> slt_result.asBits,
        1024 -> sra_result.asBits,
        default -> B(0)
    )
    val mul_op = io.alu_op(8)
    val mul_count = Reg(UInt(2 bits)) init(0)
    when(mul_count === U"2'h2") {
        mul_count := U"2'h0"
    } elsewhen (mul_op) {
        mul_count := mul_count + U"2'b1"
    } otherwise {
        mul_count := U"2'b0"
    }
    io.ready := !mul_op || mul_op && (mul_count === U"2'h2")
}
