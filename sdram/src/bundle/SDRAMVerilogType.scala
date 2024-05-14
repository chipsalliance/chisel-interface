// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: 2024 Jiuyang Liu <liu@jiuyang.me>
package org.chipsalliance.jedec.sdram

import chisel3.experimental.{
  Analog,
  SerializableModule,
  SerializableModuleParameter
}
import chisel3.{
  Bool,
  Bundle,
  Clock,
  Data,
  FixedIORawModule,
  FlatIO,
  Flipped,
  Record,
  UInt,
  VecInit,
  fromIntToWidth
}

import scala.collection.immutable.{ListMap, SeqMap}

object SDRAMParameter {
  implicit def rw: upickle.default.ReadWriter[SDRAMParameter] =
    upickle.default.macroRW[SDRAMParameter]
}

case class SDRAMParameter(dataWidth: Int, csWidth: Int) {
  require(Seq(8, 16, 32, 64).contains(dataWidth))
}

class SDRAMVerilogType(parameter: SDRAMParameter) extends Record {
  private val _dq =
    Seq.tabulate(parameter.dataWidth)(i => s"DQ$i" -> Analog(1.W))
  private val _dqm =
    Seq.tabulate(parameter.dataWidth / 8)(i => s"DQM$i" -> Bool())
  private val _a =
    Seq.tabulate(13)(i => (if (i == 11) "A10_AP" else s"A$i") -> Bool())
  private val _ck = Seq.tabulate(parameter.csWidth)(i => s"CK$i" -> Clock())
  private val _cs = Seq.tabulate(parameter.csWidth)(i => s"CS$i" -> Bool())
  private val _cke = Seq.tabulate(parameter.csWidth)(i => s"CKE$i" -> Bool())
  private val _ba = Seq.tabulate(2)(i => s"BA$i" -> Bool())
  private val _ras = Some("RAS" -> Bool())
  private val _cas = Some("CAS" -> Bool())
  private val _we = Some("WE" -> Bool())
  override def elements: SeqMap[String, Data] =
    (_dq ++ _dqm ++ _a ++ _ck ++ _cs ++ _cke ++ _ba ++ _ras ++ _cas ++ _we)
      .to(ListMap)
  def apply(name: String): Data = elements(name)

  /** Data Bit Input/Output pins */
  def dq(index: Int): Analog = _dq(index)._2

  /** The Data Input/Output masks, associated with one data byte, place the DQ
    * buffers in a high impedance state when sampled high. In Read mode, DQMB
    * controls the output buffers like an output enable. In Write mode, DQMB
    * operates as a byte mask by allowing input data to be written if it is low
    * but blocks the write operation if it is high.
    */
  def dqm(index: Int): Bool = _dqm(index)._2

  /** During a Bank Activate command cycle, A0-A13 defines the row address
    * (RA0-RA13) when sampled at the rising clock edge. During a Read or Write
    * command cycle, A0-A11 defines the column address (CA0-CA11) when sampled
    * at the rising clock edge.
    */
  def a(index: Int): Bool = _a(index)._2

  /** The system clock inputs. All of the SDRAM inputs are sampled on the rising
    * edge of their associated clock.
    */
  def ck(index: Int): Clock = _ck(index)._2

  /** Enables the associated SDRAM command decoder when low and disables the
    * command decoder when high. When the command decoder is disabled, new
    * commands are ignored but previous operations continue. Physical Bank 0 is
    * selected by S0.
    */
  def cs(index: Int): Bool = _cs(index)._2

  /** Activates the SDRAM CK signal when high and deactivates the CK signal when
    * low. By deactivating the clocks, CKE low initiates the Power Down mode,
    * Suspend mode, or the Self Refresh mode.
    */
  def cke(index: Int): Bool = _cke(index)._2

  /** Selects which SDRAM bank of four is activated. */
  def ba(index: Int): Bool = _ba(index)._2

  /** When sampled at the positive rising edge of the clock, RAS define the
    * operation to be executed by the SDRAM.
    */
  def ras: Bool = _ras.get._2

  /** When sampled at the positive rising edge of the clock, CAS define the
    * operation to be executed by the SDRAM.
    */
  def cas: Bool = _cas.get._2

  /** When sampled at the positive rising edge of the clock, WE define the
    * operation to be executed by the SDRAM.
    */
  def we: Bool = _we.get._2

  /** In addition to the column address, AP is used to invoke autoprecharge
    * operation at the end of the burst read or write cycle. If AP is high,
    * autoprecharge is selected and BA0, BA1 defines the bank to be precharged.
    * If AP is low, autoprecharge is disabled. During a Precharge command cycle,
    * AP is used in conjunction with BA0, BA1 to control which bank(s) to
    * precharge. If AP is high, all banks will be precharged regardless of the
    * state of BA0 or BA1. If AP is low, then BA0 and BA1 are used to define
    * which bank to precharge.
    */
  def ap: Bool = _a(10)._2
}

class SDRAMChiselType(parameter: SDRAMParameter) extends Bundle {

  /** assert to output, deassert to input */
  val dqDir = Bool()
  val dqi = Flipped(UInt(parameter.dataWidth.W))
  val dqo = UInt(parameter.dataWidth.W)
  val dqm = UInt((parameter.dataWidth / 8).W)
  val a = UInt(13.W)
  val ck = VecInit.fill(parameter.csWidth)(Clock())
  val cs = UInt(parameter.csWidth.W)
  val cke = UInt(parameter.csWidth.W)
  val ba = UInt(2.W)
  val ras = Bool()
  val cas = Bool()
  val we = Bool()
}

case class SDRAMTypeConverterParameter(sdramParameter: SDRAMParameter)
    extends SerializableModuleParameter

class SDRAMTypeConverterInterface(val parameter: SDRAMTypeConverterParameter)
    extends Bundle {
  val chiselType = new SDRAMChiselType(parameter.sdramParameter)
  val verilogType = new SDRAMVerilogType(parameter.sdramParameter)
}

// TODO: How to resolve the inout in [[SDRAMVerilogType]]
class SDRAMTypeConverter(val parameter: SDRAMTypeConverterParameter)
    extends FixedIORawModule(FlatIO(new SDRAMTypeConverterInterface(parameter)))
    with SerializableModule[SDRAMTypeConverterParameter] {
  Seq.tabulate(2) { i => io.verilogType.cke(i) := io.chiselType.cke(i) }

}
