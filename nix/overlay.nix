{ self }: final: prev: {
  circt = self.inputs.nixpkgs-for-circt.legacyPackages."${final.system}".circt;
  espresso = final.callPackage ./pkgs/espresso.nix { };
  circt-full = final.callPackage ./pkgs/circt-full.nix { };
  submodules = final.callPackage ./submodules.nix { };
  chisel-snapshot = final.callPackage ./chisel-snapshot.nix { };
  chisel-interface = final.callPackage ./chisel-interface.nix { };
  chisel-interface-snapshot = final.callPackage ./chisel-interface-snapshot.nix { };
  mill = prev.mill.overrideAttrs {
    version = "unstable-0.12.5-173-15dded";
    src = final.fetchurl {
      url = "https://github.com/com-lihaoyi/mill/releases/download/0.12.5/0.12.5-173-15dded-assembly";
      hash = "sha256-xP59tONOu0CG5Gce4ru+st5KUH7Wcd10d/pQdELjSJM=";
    };
  };
}
