# This file was generated by nvfetcher, please do not modify it manually.
{ fetchgit, fetchurl, fetchFromGitHub, dockerTools }:
{
  chisel = {
    pname = "chisel";
    version = "8ca43912a43bba06d2627a22520618019c1710f9";
    src = fetchFromGitHub {
      owner = "chipsalliance";
      repo = "chisel";
      rev = "8ca43912a43bba06d2627a22520618019c1710f9";
      fetchSubmodules = false;
      sha256 = "sha256-c0Q2Uf+MYORqvqzja4Er53h/RNOUVVm2bReMfTjiwp8=";
    };
    date = "2025-03-19";
  };
}
