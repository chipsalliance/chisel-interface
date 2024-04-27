import mill._
import mill.scalalib._
import mill.define.{TaskModule, Command}
import mill.scalalib.publish._
import mill.scalalib.scalafmt._
import $file.common

object v {
  val scala = "2.13.12"
  val chiselPlugin = ivy"org.chipsalliance:::chisel-plugin:7.0.0-M1"
  val chisel = ivy"org.chipsalliance::chisel:7.0.0-M1"
  val mainargs = ivy"com.lihaoyi::mainargs:0.7.0"
}

object dwbb extends common.DWBBModule with ScalafmtModule {
  m =>
  def millSourcePath = os.pwd / "dwbb"
  def scalaVersion = T(v.scala)
  def chiselIvy = Some(v.chisel)
  def chiselPluginIvy = Some(v.chiselPlugin)
  def mainargsIvy = v.mainargs
  def pomSettings = T(PomSettings(
    description = artifactName(),
    organization = "org.chipsalliance",
    url = "https://github.com/chipsalliance/dwbb",
    licenses = Seq(License.`Apache-2.0`),
    versionControl = VersionControl.github("chipsalliance", "dwbb"),
    developers = Seq(
      Developer("sequencer", "Jiuyang Liu", "https://github.com/sequencer")
    )
  ))
}
