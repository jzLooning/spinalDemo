package Cache

import spinal.core._

class ICache extends Component {
    val io = new Bundle {
        val readAddress = in UInt(5 bits)
        val readData = out UInt(32 bits)
    }

    val mem = Mem(Bits(32 bits),wordCount = 32)
    io.readData := mem.readAsync(io.readAddress).asUInt
}
