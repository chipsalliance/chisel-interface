{ lib
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
    millDepsHash = "sha256-53K85Of2kPaleNbuGPkaucN1fDHiHAG1+I76rTR5N+0=";
  };
in
publishMillJar {
  name = "chisel-interface-with-chisel-snapshot";

  src = chiselInterfaceSrc;

  buildInputs = [
    chisel-snapshot.setupHook
    chiselInterfaceDeps.setupHook
  ];

  preBuild = ''
    _localVersion="0.1.0-SNAPSHOT"
    echo "$_localVersion" > dwbb/version
    echo "$_localVersion" > jtag/version
    echo "$_localVersion" > axi4/version
  '';

  publishTargets = [
    "dwbb[snapshot]"
    "jtag[snapshot]"
    "axi4[snapshot]"
  ];

  passthru = {
    inherit chiselInterfaceDeps;
  };
}
