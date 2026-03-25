$ErrorActionPreference = "Stop"
Set-Location (Split-Path -Parent $PSScriptRoot)
& .\mvnw.cmd -Pdist -pl jwms-frontend -am package
