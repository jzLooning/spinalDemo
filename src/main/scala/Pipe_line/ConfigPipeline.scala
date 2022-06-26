package Pipe_line

import spinal.core.{SpinalEnum, SpinalEnumEncoding}
import spinal.lib.experimental.chisel.Bundle

class ConfigPipeline extends SpinalEnum{
    /** ALU_OP */
    val ALU_OP = new Bundle {
        val add = 0
        val sub = 1
        val and = 2
        val or = 3
        val xor = 4
        val sll = 5
        val srl = 6
        val lui = 7
        val mul = 8
    }
}
