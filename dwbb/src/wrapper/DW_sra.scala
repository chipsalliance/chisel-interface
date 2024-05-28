// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: 2024 Jiuyang Liu <liu@jiuyang.me>
package org.chipsalliance.dwbb.wrapper.DW_sra

import chisel3.experimental.IntParam
import org.chipsalliance.dwbb.interface.DW_sra._
import org.chipsalliance.dwbb.wrapper.WrapperModule

import scala.collection.immutable.SeqMap

class DW_sra(parameter: Parameter)
    extends WrapperModule[Interface, Parameter](
      new Interface(parameter),
      parameter,
      p =>
        SeqMap(
          "A_width" -> IntParam(p.aWidth),
          "SH_width" -> IntParam(p.shWidth)
        )
    )
