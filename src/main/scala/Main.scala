import spinal.core._
import tool.Decode
object Main{
  def main(args: Array[String]):Unit = {
    SpinalConfig(targetDirectory = "./build/").generateVerilog(new Decode(3,8))
  }
}