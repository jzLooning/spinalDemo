package Pipe_line

import spinal.core.{Bits, Bool, Bundle, SpinalEnum, SpinalEnumEncoding}
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
    def getBoolValue(dest:Bool,io:Bundle):Bool = {
        val name = dest.getName()
        var return_element = Bool(false)
        for ((io_name,io_element) <- io.elements) {
            if(name == io_name) {
                return_element = io_element.asBits.asBool
            }
        }
        return_element
    }
    def getBitsValue(dest:Bits,io:Bundle):Bits = {
        val name = dest.getName()
        var return_element = Bits()
        for ((io_name,io_element) <- io.elements) {
            if (name == io_name) {
                return_element = io_element.asBits
            }
        }
        return_element
    }
}
