package Pipe_line
import spinal.core._
class Regfile extends Component {
    val io = new Bundle{
        val raddr1 = in UInt(5 bits)
        val rdata1 = out Bits(32 bits)

        val raddr2 = in UInt(5 bits)
        val rdata2 = out Bits(32 bits)

        val we = in Bool()
        val waddr = in UInt(5 bits)
        val wdata = in Bits(32 bits)
    }

    val regfile = Reg(Vec(Bits(32 bits),32))
    regfile.foreach(_.init(0))
    when(io.we) {
        regfile(io.waddr) := io.wdata
    }

    io.rdata1 := (io.raddr1 === 0) ? B(0) | regfile(io.raddr1)
    io.rdata2 := (io.raddr2 === 0) ? B(0) | regfile(io.raddr2)
}
