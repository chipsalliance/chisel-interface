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
    dockerTools = null;
  }).chisel.src;
  chiselDeps = fetchMillDeps {
    name = "chisel-snapshot";
    src = chiselSrc;
    millDepsHash = "sha256-NBHUq5MaGiiaDA5mjeP0xcU5jNe9wWordL01a6khy7I=";
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
