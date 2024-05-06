// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: 2024 Jiuyang Liu <liu@jiuyang.me>
package org.chipsalliance.dwbb.testbench

import chisel3._
import chisel3.experimental.{
  SerializableModule,
  SerializableModuleGenerator,
  SerializableModuleParameter
}
import chisel3.layer.{Convention, Layer, block}
import chisel3.ltl.{AssertProperty, AssumeProperty}
import org.chipsalliance.dwbb.packageName

import scala.reflect.runtime.universe
import scala.reflect.runtime.universe.{runtimeMirror, typeOf}
object Verification extends Layer(Convention.Bind) {
  object BMC extends Layer(Convention.Bind)
  object Debug extends Layer(Convention.Bind)
}
class TestBench[
    Param <: SerializableModuleParameter: universe.TypeTag,
    IO <: Record,
    Ref <: SerializableModule[Param] with FixedIORawModule[
      IO
    ]: universe.TypeTag,
    DW <: SerializableModule[Param] with FixedIOExtModule[IO]: universe.TypeTag
](val parameter: Param)
    extends Module
    with SerializableModule[Param] {
  override def desiredName: String = packageName[Param] + "_TestBench"
  val ref = Module(
    new SerializableModuleGenerator(
      runtimeMirror(getClass.getClassLoader)
        .runtimeClass(typeOf[Ref].typeSymbol.asClass)
        .asInstanceOf[Class[Ref]],
      parameter
    ).module()
  ).suggestName(s"${packageName[Param]}_Ref_inst")
  val dw = Module(
    new SerializableModuleGenerator(
      runtimeMirror(getClass.getClassLoader)
        .runtimeClass(typeOf[DW].typeSymbol.asClass)
        .asInstanceOf[Class[DW]],
      parameter
    ).module()
  ).suggestName(s"${packageName[Param]}_DW_inst")
  val keys: Seq[String] = ref.io.elements.keys.toSeq
  keys.foreach { key =>
    val r: Data = ref.io.elements(key).suggestName(s"ref_${key}")
    val d: Data = dw.io.elements(key).suggestName(s"dw_${key}")
    import chisel3.ltl.Sequence.BoolSequence
    chisel3.reflect.DataMirror.directionOf(r) match {
      // This is enough for BMC, maybe change to LTL version to get label nice.
      case ActualDirection.Output =>
        block(Verification) {
          block(Verification.BMC) {
            AssertProperty(r.asUInt === d.asUInt, label = Some(s"ASSERT_$key"))
          }
          block(Verification.Debug) {
            printf(p"output $key: ref: $r, dw: $d")
          }
        }
      case ActualDirection.Input =>
        val input = IO(chiselTypeOf(r)).suggestName(s"io_$key")
        r := input
        d := input
        block(Verification) {
          block(Verification.BMC) {
            AssumeProperty(r.asUInt === d.asUInt, label = Some(s"ASSUME_$key"))
          }
          block(Verification.Debug) {
            printf(p"input $key: $input")
          }
        }
      case _ => throw new Exception("Bidirectional not allowed here.")
    }
  }
}
