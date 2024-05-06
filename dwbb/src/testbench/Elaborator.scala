// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: 2024 Jiuyang Liu <liu@jiuyang.me>
package org.chipsalliance.dwbb.testbench

import chisel3.experimental.{
  SerializableModuleGenerator,
  SerializableModuleParameter
}
import mainargs.TokensReader

trait Elaborator {
  import scala.reflect.runtime.universe._

  implicit object PathRead extends TokensReader.Simple[os.Path] {
    def shortName = "path"
    def read(strs: Seq[String]) = Right(os.Path(strs.head, os.pwd))
  }

  def configImpl[P <: SerializableModuleParameter: TypeTag](
      parameter: P,
      path: os.Path
  )(implicit rwP: upickle.default.Writer[P]) = os.write(
    path,
    upickle.default.write(parameter)
  )

  def testbenchImpl[
      M <: TestBench[P, _, _, _]: TypeTag,
      P <: SerializableModuleParameter: TypeTag
  ](parameter: os.Path, runFirtool: Boolean)(implicit
      rwP: upickle.default.Reader[P]
  ) = {
    var fir: firrtl.ir.Circuit = null
    val annos = Seq(
      new chisel3.stage.phases.Elaborate,
      new chisel3.stage.phases.Convert
    ).foldLeft(
      Seq(
        chisel3.stage.ChiselGeneratorAnnotation(() =>
          SerializableModuleGenerator(
            runtimeMirror(getClass.getClassLoader)
              .runtimeClass(typeOf[M].typeSymbol.asClass)
              .asInstanceOf[Class[M]],
            upickle.default.read[P](os.read(parameter))
          ).module()
        )
      ): firrtl.AnnotationSeq
    ) { case (annos, stage) => stage.transform(annos) }
      .flatMap {
        case firrtl.stage.FirrtlCircuitAnnotation(circuit) =>
          fir = circuit
          None
        case _: chisel3.stage.DesignAnnotation[_]     => None
        case _: chisel3.stage.ChiselCircuitAnnotation => None
        case a                                        => Some(a)
      }
    val annoJsonFile = os.pwd / s"${fir.main}.anno.json"
    val firFile = os.pwd / s"${fir.main}.fir"
    val svFile = os.pwd / s"${fir.main}.sv"
    os.write(firFile, fir.serialize)
    os.write(
      annoJsonFile,
      firrtl.annotations.JsonProtocol.serializeRecover(annos)
    )
    if (runFirtool) {
      os.proc(
        "firtool",
        s"--annotation-file=${annoJsonFile}",
        s"${firFile}",
        s"-o",
        s"${svFile}",
        "--strip-debug-info",
        "--verification-flavor=sva",
        "--extract-test-code"
      ).call(os.pwd)
    }
  }
}
