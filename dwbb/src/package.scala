// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: 2024 Jiuyang Liu <liu@jiuyang.me>
package org.chipsalliance

package object dwbb {
  import scala.reflect.runtime.universe._
  import scala.reflect.runtime.{currentMirror => cm}

  def packageName[P: TypeTag] = {
    def enclosingPackage(sym: Symbol): Symbol = {
      if (sym == NoSymbol) NoSymbol
      else if (sym.isPackage) sym
      else enclosingPackage(sym.owner)
    }
    val pkg = enclosingPackage(typeOf[P].typeSymbol)
    if (pkg == cm.EmptyPackageClass) ""
    else pkg.fullName.split('.').last
  }
}
