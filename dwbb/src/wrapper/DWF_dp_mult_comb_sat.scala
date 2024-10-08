// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: 2024 Jiuyang Liu <liu@jiuyang.me>
package org.chipsalliance.dwbb.wrapper.DWF_dp_mult_comb_sat

import chisel3.experimental.IntParam
import org.chipsalliance.dwbb.interface.DWF_dp_mult_comb_sat._
import org.chipsalliance.dwbb.wrapper.WrapperModule

import scala.collection.immutable.SeqMap

class DWF_dp_mult_comb_sat(parameter: Parameter)
    extends WrapperModule[Interface, Parameter](
      new Interface(parameter),
      parameter,
      p =>
        SeqMap(
          "a_width" -> IntParam(p.aWidth),
          "b_width" -> IntParam(p.bWidth),
          "p_width" -> IntParam(p.pWidth)
        )
    )
