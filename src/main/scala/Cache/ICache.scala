package Cache

import spinal.core._
import spinal.lib._
import spinal.lib.fsm.{EntryPoint, State, StateMachine}

class ICache extends Component {
    /**
     * 与mmp交互的接口
     */
    val io_mmap = new Bundle {
        val inst_pc: Bits = out Bits(32 bits)
        val inst_re: Bool = out Bool()
        val inst_data: Bits = in Bits(32 bits)
    }



    /**
     * 与cpu交互，送去四个指令
     */
    val io_inst = new Bundle {
        val inst_bus = out Bits(128 bits)
        val inst_bus_en = out Vec(Bool(),4)
        val inst_hit = out Bool()

        val inst_pc = in Bits(32 bits)
        val inst_re = in Bool()
    }



    /**
     * 状态机
     */
    val cache_fsm = new StateMachine {val load = Bool(false)
        val cache_line_write = Vec(Bits( 1 bits),Bits(11 bits),Bits(32 bits),Bits(32 bits),Bits(32 bits),Bits(32 bits))
        val cache_way1 = Mem(Bits(cache_line_write.getBitsWidth bits),256)
        val cache_way2 = Mem(Bits(cache_line_write.getBitsWidth bits),256)
        val cache_swap = Bits(256 bits)

        cache_swap := 0
        /**
         * 处理pc信号
         */
        val index = io_inst.inst_pc(11 downto 4).asUInt
        val tag = io_inst.inst_pc(22 downto 12).asUInt
        val cache_line_read_way1 = cache_way1(index)
        val cache_line_read_way2 = cache_way2(index)
        io_mmap.inst_pc := 0
        io_mmap.inst_re := False
        io_inst.inst_bus := 0
        io_inst.inst_hit := False
        cache_line_write.foreach(_ := 0)


        /**
         * cache初始化
         */
        cache_way1.initBigInt(Seq.fill(256)(0))
        cache_way2.initBigInt(Seq.fill(256)(0))

        /**
         * 返回字使能
         */
        val pc_2: Bits = io_inst.inst_pc(3 downto 2)
        io_inst.inst_bus_en(0) := True
        io_inst.inst_bus_en(1) := pc_2(0) || pc_2(1)
        io_inst.inst_bus_en(2) := pc_2(1)
        io_inst.inst_bus_en(3) := pc_2(0) && pc_2(1)


        val counter = new Area{
            val counter = Reg(UInt(3 bits)) init(0)
            when (load) {
                counter := counter + 1
            } otherwise {
                counter := 0
            }
        }

        /**
         * 初始状态，clk上升沿到来时接收信号，并转入state_tag_check状态
         */
        val state_wait: State = new State with EntryPoint {
            whenIsActive {
               val state_trigger = Reg(Bool) init(False)
                state_trigger := !state_trigger
                when(state_trigger.rise()) {
                    goto(state_tag_check)
                }
            }
        }

        /**
         * 命中检查
         */
        val state_tag_check: State = new State {

            whenIsActive {
                val tag_1_hit = cache_line_read_way1(139) && (cache_line_read_way1(138 downto 128).asUInt === tag)
                val tag_2_hit = cache_line_read_way2(139) && (cache_line_read_way2(138 downto 128).asUInt === tag)
                when(tag_1_hit) {
                    cache_swap(index) := False
                    io_inst.inst_bus := cache_line_read_way1(127 downto 0)
                    io_inst.inst_hit := True
                    goto(state_wait)
                } elsewhen (tag_2_hit) {
                    cache_swap(index) := True
                    io_inst.inst_bus := cache_line_read_way2(127 downto 0)
                    io_inst.inst_hit := True
                    goto(state_wait)
                } otherwise {
                    io_inst.inst_bus := 0
                    io_inst.inst_hit := False
                    goto(state_load_data)
                }
            }
        }

        /**
         * 位命中后数据加载
         */
        val state_load_data: State = new State {
            whenIsNext {
                load := True
                cache_line_write(1) := io_inst.inst_pc(22 downto 12)
            }
            whenIsActive {
                switch(counter.counter) {
                    is(0) {
                        io_mmap.inst_pc := io_inst.inst_pc(31 downto 4) ## U"4'h0"
                        io_mmap.inst_re := True
                        cache_line_write(2) := io_mmap.inst_data
                    }
                    is(1) {
                        io_mmap.inst_pc := io_inst.inst_pc(31 downto 4) ## U"4'h4"
                        io_mmap.inst_re := True
                        cache_line_write(3) := io_mmap.inst_data

                    }
                    is(2) {
                        io_mmap.inst_pc := io_inst.inst_pc(31 downto 4) ## U"4'h8"
                        io_mmap.inst_re := True
                        cache_line_write(4) := io_mmap.inst_data
                    }
                    is(3) {
                        io_mmap.inst_pc := io_inst.inst_pc(31 downto 4) ## U"4'hc"
                        io_mmap.inst_re := True
                        cache_line_write(5) := io_mmap.inst_data
                    }
                    is(4) {
                        load := False
                        cache_line_write(0) := 1
                        cache_way1.write(index,cache_line_write.asBits,cache_swap(index))
                        cache_way2.write(index,cache_line_write.asBits,!cache_swap(index))
                        goto(state_tag_check)
                    }
                    default {
                        cache_line_write.foreach(_ := 0)
                        io_mmap.inst_pc := 0
                    }
                }
            }
        }
    }



}
