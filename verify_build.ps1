$ErrorActionPreference = "Stop"
$MavenVersion = "3.9.6"
$MavenZip = "apache-maven-$MavenVersion-bin.zip"
$MavenFolder = "apache-maven-$MavenVersion"
$Url = "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/binaries/$MavenZip"

if (-not (Test-Path "maven-bin")) {
    Write-Host "Downloading Apache Maven $MavenVersion..."
    Invoke-WebRequest -Uri $Url -OutFile $MavenZip
    Write-Host "Extracting Maven..."
    Expand-Archive -Path $MavenZip -DestinationPath "maven-bin"
    Remove-Item $MavenZip
}

Write-Host "Running Maven compile and test..."
& "maven-bin/$MavenFolder/bin/mvn.cmd" clean test
