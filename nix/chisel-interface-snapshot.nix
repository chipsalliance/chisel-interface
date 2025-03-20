{ lib
, git
, publishMillJar
, chisel-snapshot
}:
publishMillJar {
  name = "chisel-interface-with-chisel-snapshot";

  src = with lib.fileset; toSource {
    fileset = unions [
      ../build.mill
      ../common.mill
      ../axi4
      ../dwbb
      ../jtag
    ];
    root = ../.;
  };

  nativeBuildInputs = [ git ];

  buildInputs = [
    chisel-snapshot.setupHook
  ];

  publishTargets = [
    "dwbb[snapshot]"
    "jtag[snapshot]"
    "axi4[snapshot]"
  ];

  lockFile = ../chisel-interface-mill-lock.nix;
}
