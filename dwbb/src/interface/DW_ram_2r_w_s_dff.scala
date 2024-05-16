// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: 2024 Jiuyang Liu <liu@jiuyang.me>
package org.chipsalliance.dwbb.interface.DW_ram_2r_w_s_dff

import chisel3._
import chisel3.experimental.SerializableModuleParameter
import chisel3.util.log2Ceil
import upickle.default

object Parameter {
  implicit def rw: default.ReadWriter[Parameter] =
    upickle.default.macroRW[Parameter]
}

case class Parameter(
    dataWidth: Int = 4,
    depth: Int = 8,
    rstMode: Boolean = true
) extends SerializableModuleParameter {
  require(
    Range.inclusive(1, 2048).contains(dataWidth),
    "Data width must be between 1 and 2048"
  )
  require(
    Range.inclusive(2, 1024).contains(depth),
    "Depth must be between 2 and 1024"
  )
  val logDepth: Int = log2Ceil(depth)
}

class Interface(parameter: Parameter) extends Bundle {
  val clk: Clock = Input(Clock())
  val rst_n: Bool = Input(Bool())
  val cs_n: Bool = Input(Bool())
  val wr_n: Bool = Input(Bool())
  val rd1_addr: UInt = Input(UInt(parameter.logDepth.W))
  val rd2_addr: UInt = Input(UInt(parameter.logDepth.W))
  val wr_addr: UInt = Input(UInt(parameter.logDepth.W))
  val data_in: UInt = Input(UInt(parameter.dataWidth.W))
  val data_rd1_out: UInt = Output(UInt(parameter.dataWidth.W))
  val data_rd2_out: UInt = Output(UInt(parameter.dataWidth.W))
}
