// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: 2024 Jiuyang Liu <liu@jiuyang.me>
package org.chipsalliance.dwbb.reference

import chisel3.{Data, FixedIORawModule}
import chisel3.experimental.{SerializableModule, SerializableModuleParameter}

abstract class ReferenceModule[I <: Data, P <: SerializableModuleParameter](
    ioGenerator: I,
    final val parameter: P
) extends FixedIORawModule[I](ioGenerator)
    with SerializableModule[P] {
  override def desiredName: String = super.desiredName + "_REF"
}
