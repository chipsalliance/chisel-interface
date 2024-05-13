// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: 2024 Jiuyang Liu <liu@jiuyang.me>
package org.chipsalliance.amba.axi4

package object bundle {
  object verilog {
    object irrevocable {
      def apply(parameter: AXI4BundleParameter): AXI4VerilogBundle = {
        if (parameter.isRW) new AXI4RWIrrevocableVerilog(parameter)
        else if (parameter.isRO) new AXI4ROIrrevocableVerilog(parameter)
        else new AXI4WOIrrevocableVerilog(parameter)
      }
    }
  }
  object chisel {
    object irrevocable {
      def apply(parameter: AXI4BundleParameter): AXI4ChiselBundle = {
        if (parameter.isRW) new AXI4RWIrrevocable(parameter)
        else if (parameter.isRO) new AXI4ROIrrevocable(parameter)
        else new AXI4WOIrrevocable(parameter)
      }
    }
  }

  object AXI4BundleParameter {
    implicit def rw: upickle.default.ReadWriter[AXI4BundleParameter] =
      upickle.default.macroRW[AXI4BundleParameter]
  }

  /** All physical information to construct any [[AXI4Bundle]]. TODO: I'm
    * wondering how to express the `user` field in Chisel Type: This is not easy
    * because we don't have a serializable Chisel Type, Neither firrtl type can
    * be convert to Chisel Type. To keep the ABI stable. Users must convert
    * their chisel type to bare UInt.
    */
  case class AXI4BundleParameter(
      idWidth: Int,
      dataWidth: Int,
      addrWidth: Int,
      userReqWidth: Int,
      userDataWidth: Int,
      userRespWidth: Int,
      hasAW: Boolean,
      hasW: Boolean,
      hasB: Boolean,
      hasAR: Boolean,
      hasR: Boolean
  ) {
    val isRW: Boolean = hasAW && hasW && hasB && hasAR && hasR
    val isRO: Boolean = !isRW && hasAR && hasR
    val isWO: Boolean = !isRW && hasAW && hasW && hasB

    override def toString: String = Seq(
      Some(s"ID$idWidth"),
      Some(s"DATA$dataWidth"),
      Some(s"ADDR$addrWidth"),
      Option.when(userReqWidth != 0)(s"USER_REQ$userReqWidth"),
      Option.when(userDataWidth != 0)(s"USER_DATA$userDataWidth"),
      Option.when(userRespWidth != 0)(s"USER_RESP$userRespWidth")
    ).flatten.mkString("_")

    val awUserWidth: Int = userReqWidth
    val wUserWidth: Int = userDataWidth
    val bUserWidth: Int = userRespWidth
    val arUserWidth: Int = userReqWidth
    val rUserWidth: Int = userDataWidth + userRespWidth
    require(
      Seq(8, 16, 32, 64, 128, 256, 512, 1024).contains(dataWidth),
      "A1.2.1: The data bus, which can be 8, 16, 32, 64, 128, 256, 512, or 1024 bits wide. A read response signal indicating the completion status of the read transaction."
    )
    require(
      (0 <= userReqWidth && userReqWidth <= 128) &&
        (0 <= userReqWidth && userReqWidth <= 128) &&
        (0 <= userReqWidth && userReqWidth <= 128),
      "The presence and width of User signals is specified by the properties in Table A8-1"
    )
  }

  /** Generic bundle for AXI4, only used in this repo. TODO: Do we need to
    * support absent signals? The current is no, since PnR flow has a good
    * support to tie0/tie1. But we need some default signals support like
    * RocketChip did in the BundleMap. In the future, we may leverage
    * chipsalliance/chisel#3978 for better connect handing.
    */
  trait AXI4Bundle extends chisel3.Bundle {
    val parameter: AXI4BundleParameter
    override def typeName: String = super.typeName + "_" + parameter.toString
    val idWidth: Int = parameter.idWidth
    val dataWidth: Int = parameter.dataWidth
    val addrWidth: Int = parameter.addrWidth
    val awUserWidth: Int = parameter.awUserWidth
    val wUserWidth: Int = parameter.wUserWidth
    val bUserWidth: Int = parameter.bUserWidth
    val arUserWidth: Int = parameter.arUserWidth
    val rUserWidth: Int = parameter.rUserWidth
  }

  implicit val rwV2C: chisel3.experimental.dataview.DataView[
    AXI4RWIrrevocableVerilog,
    AXI4RWIrrevocable
  ] = chisel3.experimental.dataview
    .DataView[AXI4RWIrrevocableVerilog, AXI4RWIrrevocable](
      v => new AXI4RWIrrevocable(v.parameter),
      _.AWID -> _.aw.bits.id,
      _.AWADDR -> _.aw.bits.addr,
      _.AWLEN -> _.aw.bits.len,
      _.AWSIZE -> _.aw.bits.size,
      _.AWBURST -> _.aw.bits.burst,
      _.AWLOCK -> _.aw.bits.lock,
      _.AWCACHE -> _.aw.bits.cache,
      _.AWPROT -> _.aw.bits.prot,
      _.AWQOS -> _.aw.bits.qos,
      _.AWREGION -> _.aw.bits.region,
      _.AWUSER -> _.aw.bits.user,
      _.AWVALID -> _.aw.valid,
      _.AWREADY -> _.aw.ready,
      _.WDATA -> _.w.bits.data,
      _.WSTRB -> _.w.bits.strb,
      _.WLAST -> _.w.bits.last,
      _.WUSER -> _.w.bits.user,
      _.WVALID -> _.w.valid,
      _.WREADY -> _.w.ready,
      _.BID -> _.b.bits.id,
      _.BRESP -> _.b.bits.resp,
      _.BUSER -> _.b.bits.user,
      _.BVALID -> _.b.valid,
      _.BREADY -> _.b.ready,
      _.ARID -> _.ar.bits.id,
      _.ARADDR -> _.ar.bits.addr,
      _.ARLEN -> _.ar.bits.len,
      _.ARSIZE -> _.ar.bits.size,
      _.ARBURST -> _.ar.bits.burst,
      _.ARLOCK -> _.ar.bits.lock,
      _.ARCACHE -> _.ar.bits.cache,
      _.ARPROT -> _.ar.bits.prot,
      _.ARQOS -> _.ar.bits.qos,
      _.ARREGION -> _.ar.bits.region,
      _.ARUSER -> _.ar.bits.user,
      _.ARVALID -> _.ar.valid,
      _.ARREADY -> _.ar.ready,
      _.RID -> _.r.bits.id,
      _.RDATA -> _.r.bits.data,
      _.RRESP -> _.r.bits.resp,
      _.RLAST -> _.r.bits.last,
      _.RUSER -> _.r.bits.user,
      _.RVALID -> _.r.valid,
      _.RREADY -> _.r.ready
    )
  implicit val rwC2V: chisel3.experimental.dataview.DataView[
    AXI4RWIrrevocable,
    AXI4RWIrrevocableVerilog
  ] = rwV2C.invert(c => new AXI4RWIrrevocableVerilog(c.parameter))
  implicit val roV2C: chisel3.experimental.dataview.DataView[
    AXI4ROIrrevocableVerilog,
    AXI4ROIrrevocable
  ] = chisel3.experimental.dataview
    .DataView[AXI4ROIrrevocableVerilog, AXI4ROIrrevocable](
      v => new AXI4ROIrrevocable(v.parameter),
      _.ARID -> _.ar.bits.id,
      _.ARADDR -> _.ar.bits.addr,
      _.ARLEN -> _.ar.bits.len,
      _.ARSIZE -> _.ar.bits.size,
      _.ARBURST -> _.ar.bits.burst,
      _.ARLOCK -> _.ar.bits.lock,
      _.ARCACHE -> _.ar.bits.cache,
      _.ARPROT -> _.ar.bits.prot,
      _.ARQOS -> _.ar.bits.qos,
      _.ARREGION -> _.ar.bits.region,
      _.ARUSER -> _.ar.bits.user,
      _.ARVALID -> _.ar.valid,
      _.ARREADY -> _.ar.ready,
      _.RID -> _.r.bits.id,
      _.RDATA -> _.r.bits.data,
      _.RRESP -> _.r.bits.resp,
      _.RLAST -> _.r.bits.last,
      _.RUSER -> _.r.bits.user,
      _.RVALID -> _.r.valid,
      _.RREADY -> _.r.ready
    )
  implicit val roC2V: chisel3.experimental.dataview.DataView[
    AXI4ROIrrevocable,
    AXI4ROIrrevocableVerilog
  ] = roV2C.invert(c => new AXI4ROIrrevocableVerilog(c.parameter))
  implicit val woV2C: chisel3.experimental.dataview.DataView[
    AXI4WOIrrevocableVerilog,
    AXI4WOIrrevocable
  ] = chisel3.experimental.dataview
    .DataView[AXI4WOIrrevocableVerilog, AXI4WOIrrevocable](
      v => new AXI4WOIrrevocable(v.parameter),
      _.AWID -> _.aw.bits.id,
      _.AWADDR -> _.aw.bits.addr,
      _.AWLEN -> _.aw.bits.len,
      _.AWSIZE -> _.aw.bits.size,
      _.AWBURST -> _.aw.bits.burst,
      _.AWLOCK -> _.aw.bits.lock,
      _.AWCACHE -> _.aw.bits.cache,
      _.AWPROT -> _.aw.bits.prot,
      _.AWQOS -> _.aw.bits.qos,
      _.AWREGION -> _.aw.bits.region,
      _.AWUSER -> _.aw.bits.user,
      _.AWVALID -> _.aw.valid,
      _.AWREADY -> _.aw.ready,
      _.WDATA -> _.w.bits.data,
      _.WSTRB -> _.w.bits.strb,
      _.WLAST -> _.w.bits.last,
      _.WUSER -> _.w.bits.user,
      _.WVALID -> _.w.valid,
      _.WREADY -> _.w.ready,
      _.BID -> _.b.bits.id,
      _.BRESP -> _.b.bits.resp,
      _.BUSER -> _.b.bits.user,
      _.BVALID -> _.b.valid,
      _.BREADY -> _.b.ready
    )
  implicit val woC2V: chisel3.experimental.dataview.DataView[
    AXI4WOIrrevocable,
    AXI4WOIrrevocableVerilog
  ] = woV2C.invert(c => new AXI4WOIrrevocableVerilog(c.parameter))
}
