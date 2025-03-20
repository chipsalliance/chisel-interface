{ self }: final: prev: {
  circt = self.inputs.nixpkgs-for-circt.legacyPackages."${final.system}".circt;
  espresso = final.callPackage ./pkgs/espresso.nix { };
  circt-full = final.callPackage ./pkgs/circt-full.nix { };
  submodules = final.callPackage ./submodules.nix { };
  chisel-snapshot = final.callPackage ./chisel-snapshot.nix { };
  chisel-interface = final.callPackage ./chisel-interface.nix { };
  chisel-interface-snapshot = final.callPackage ./chisel-interface-snapshot.nix { };
  mill = let jre = final.jdk21; in
    (prev.mill.override { inherit jre; }).overrideAttrs rec {
      # Fixed the buggy sorting issue in target resolve
      version = "0.12.8-1-46e216";
      src = final.fetchurl {
        url = "https://repo1.maven.org/maven2/com/lihaoyi/mill-dist/${version}/mill-dist-${version}-assembly.jar";
        hash = "sha256-XNtl9NBQPlkYu/odrR/Z7hk3F01B6Rk4+r/8tMWzMm8=";
      };
      passthru = { inherit jre; };
    };
  bump-chisel-interface = final.callPackage ./bump-lock.nix { };
}
