{ fetchMillDeps
, publishMillJar
, fetchFromGitHub
, git
}:
let
  chiselSrc = ((import ./_sources/generated.nix) {
    inherit fetchFromGitHub;
    fetchurl = null;
    fetchgit = null;
    dokerTools = null;
  }).chisel.src;
  chiselDeps = fetchMillDeps {
    name = "chisel-snapshot";
    src = chiselSrc;
    millDepsHash = "";
  };
in
publishMillJar {
  name = "chisel-snapshot";
  src = chiselSrc;

  publishTargets = [
    "unipublish"
  ];

  buildInputs = [
    chiselDeps.setupHook
  ];

  nativeBuildInputs = [
    # chisel requires git to generate version
    git
  ];

  passthru = {
    inherit chiselDeps;
  };
}
