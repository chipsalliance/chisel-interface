{ writeShellApplication
, mill
, mill-ivy-fetcher
, submodules
}:
writeShellApplication {
  name = "bump-chisel-interface";

  runtimeInputs = [
    mill-ivy-fetcher
    mill
  ];

  text = ''
    chiselDir=$(mktemp -d -t 'chisel_src_XXX')
    cp -rT ${submodules.sources.chisel.src} "$chiselDir"/chisel
    chmod -R u+w "$chiselDir"/chisel

    mif run -p "$chiselDir"/chisel -o ./nix/chisel-mill-lock.nix

    ivyLocal=$(nix build '.#chisel-snapshot' --no-link --print-out-paths -L -j auto)
    export JAVA_TOOL_OPTIONS="''${JAVA_TOOL_OPTIONS:-} -Dcoursier.ivy.home=$ivyLocal -Divy.home=$ivyLocal"
    mif run \
      --targets 'dwbb[snapshot]' \
      --targets 'jtag[snapshot]' \
      --targets 'axi4[snapshot]' \
      -o ./nix/chisel-interface-mill-lock.nix
  '';
}
