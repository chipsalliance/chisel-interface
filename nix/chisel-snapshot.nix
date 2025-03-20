{ publishMillJar
, fetchFromGitHub
, git
}:
publishMillJar {
  name = "chisel-snapshot";
  src = ((import ./_sources/generated.nix) {
    inherit fetchFromGitHub;
    fetchurl = null;
    fetchgit = null;
    dockerTools = null;
  }).chisel.src;

  publishTargets = [
    "unipublish"
  ];

  nativeBuildInputs = [
    # chisel requires git to generate version
    git
  ];

  lockFile = ./chisel-mill-lock.nix;
}
