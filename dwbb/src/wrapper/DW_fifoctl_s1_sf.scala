// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: 2024 Jiuyang Liu <liu@jiuyang.me>
package org.chipsalliance.dwbb.wrapper.DW_fifoctl_s1_sf

import chisel3.experimental.IntParam
import org.chipsalliance.dwbb.interface.DW_fifoctl_s1_sf._
import org.chipsalliance.dwbb.wrapper.WrapperModule

import scala.collection.immutable.SeqMap

class DW_fifoctl_s1_sf(parameter: Parameter)
    extends WrapperModule[Interface, Parameter](
      new Interface(parameter),
      parameter,
      p =>
        SeqMap(
          "depth" -> IntParam(p.depth),
          "ae_level" -> IntParam(p.aeLevel),
          "af_level" -> IntParam(p.afLevel),
          "err_mode" -> IntParam(p.errMode match {
            case "pointer_latched" => 0
            case "latched"         => 1
            case "unlatched"       => 2
          }),
          "rst_mode" -> IntParam(p.rstMode match {
            case "async" => 0
            case "sync"  => 1
          })
        )
    )
