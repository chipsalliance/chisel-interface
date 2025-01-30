{ lib
, git
, fetchMillDeps
, publishMillJar
, chisel-snapshot
}:
let
  chiselInterfaceSrc = with lib.fileset; toSource {
    fileset = unions [
      ../build.mill
      ../common.mill
      ../axi4
      ../dwbb
      ../jtag
    ];
    root = ../.;
  };
  chiselInterfaceDeps = fetchMillDeps {
    name = "chisel-interface-snapshot";
    src = chiselInterfaceSrc;
    buildInputs = [ chisel-snapshot.setupHook ];
    millDepsHash = "sha256-10fioZDAhLjx/ekdAJlyvqPUGeZ5InAl038JIL8lxw8=";
  };
in
publishMillJar {
  name = "chisel-interface-with-chisel-snapshot";

  src = chiselInterfaceSrc;

  nativeBuildInputs = [ git ];

  buildInputs = [
    chisel-snapshot.setupHook
    chiselInterfaceDeps.setupHook
  ];

  publishTargets = [
    "dwbb[snapshot]"
    "jtag[snapshot]"
    "axi4[snapshot]"
  ];

  passthru = {
    inherit chiselInterfaceDeps;
  };
}
