{
  description = "Development environment for gha-mvn-rflow";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            # Core Languages & Build Tools
            jdk17
            maven

            # Git & Version Control
            git
            pre-commit

            # Linting & Validation
            actionlint
            hadolint
            commitlint
            jreleaser-cli

            # Development Tools
            curl
            jq
            act
            gh
          ];

          shellHook = ''
            export JAVA_HOME=${pkgs.jdk17}

            # Install pre-commit hooks if config exists
            if [ -f .pre-commit-config.yaml ]; then
              pre-commit install --install-hooks > /dev/null 2>&1 || true
            fi

            echo "gha-mvn-rflow development environment"
            echo "Java: $(java -version 2>&1 | head -1)"
            echo "Maven: $(mvn -version 2>&1 | head -1)"
          '';
        };
      }
    );
}
