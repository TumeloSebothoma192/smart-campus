param(
    [int]$Port = 8081
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$ToolsDir = Join-Path $ProjectRoot "tools"
$TomcatVersion = "10.1.34"
$TomcatDir = Join-Path $ToolsDir "apache-tomcat-$TomcatVersion"
$TomcatZip = Join-Path $ToolsDir "apache-tomcat-$TomcatVersion.zip"
$WarName = "CampusServiceManagementSystemBackend.war"
$ContextName = "CampusServiceManagementSystemBackend"
$BuildDir = Join-Path $ProjectRoot "build\local-war"
$DistDir = Join-Path $ProjectRoot "dist"
$WarPath = Join-Path $DistDir $WarName

function Get-CommandPath($Name) {
    $cmd = Get-Command $Name -ErrorAction SilentlyContinue
    if ($cmd) {
        return $cmd.Source
    }
    return $null
}

function Download-File($Url, $OutFile) {
    Write-Host "Downloading $Url"
    Invoke-WebRequest -Uri $Url -OutFile $OutFile
}

Set-Location $ProjectRoot
New-Item -ItemType Directory -Force -Path $ToolsDir, $DistDir | Out-Null

if (!(Test-Path $TomcatDir)) {
    if (!(Test-Path $TomcatZip)) {
        Download-File `
            "https://archive.apache.org/dist/tomcat/tomcat-10/v$TomcatVersion/bin/apache-tomcat-$TomcatVersion.zip" `
            $TomcatZip
    }
    Write-Host "Extracting Tomcat $TomcatVersion"
    Expand-Archive -Path $TomcatZip -DestinationPath $ToolsDir -Force
}

$LibDir = Join-Path $ProjectRoot "lib"
New-Item -ItemType Directory -Force -Path $LibDir | Out-Null
$JaxRsApiJar = Join-Path $LibDir "jakarta.ws.rs-api-3.1.0.jar"
if (!(Test-Path $JaxRsApiJar)) {
    Download-File `
        "https://repo1.maven.org/maven2/jakarta/ws/rs/jakarta.ws.rs-api/3.1.0/jakarta.ws.rs-api-3.1.0.jar" `
        $JaxRsApiJar
}
$PostgresJar = Join-Path $LibDir "postgresql-42.7.7.jar"
if (!(Test-Path $PostgresJar)) {
    Download-File `
        "https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.7/postgresql-42.7.7.jar" `
        $PostgresJar
}

$Javac = Get-CommandPath "javac"
if (!$Javac) {
    throw "javac was not found. Install JDK 17+ and make sure javac is on PATH."
}

$JavaSettings = cmd /c "java -XshowSettings:properties -version 2>&1"
$JavaHomeLine = ($JavaSettings | Select-String "^\s+java.home\s+=" | Select-Object -First 1)
if ($JavaHomeLine) {
    $DetectedJavaHome = ($JavaHomeLine.ToString() -replace "^\s+java.home\s+=\s+", "").Trim()
    $DetectedJavaBin = Join-Path $DetectedJavaHome "bin\javac.exe"
    if (Test-Path $DetectedJavaBin) {
        $env:JAVA_HOME = $DetectedJavaHome
        $env:JRE_HOME = $DetectedJavaHome
    } elseif ((Split-Path -Leaf $DetectedJavaHome) -ieq "jre") {
        $ParentJavaHome = Split-Path -Parent $DetectedJavaHome
        if (Test-Path (Join-Path $ParentJavaHome "bin\javac.exe")) {
            $env:JAVA_HOME = $ParentJavaHome
            $env:JRE_HOME = $ParentJavaHome
        }
    }
}
if (!$env:JAVA_HOME -or !(Test-Path (Join-Path $env:JAVA_HOME "bin\javac.exe"))) {
    $JavacPath = (Get-Command "javac" -ErrorAction SilentlyContinue).Source
    if (!$JavacPath -or $JavacPath -like "*Common Files\Oracle\Java\javapath*") {
        $JavacPath = Get-ChildItem -Path "C:\Program Files\Java" -Recurse -Filter "javac.exe" -ErrorAction SilentlyContinue |
            Where-Object { $_.FullName -notlike "*jdk1.8*" } |
            Sort-Object FullName -Descending |
            Select-Object -First 1 -ExpandProperty FullName
    }
    if (!$JavacPath) {
        throw "javac was not found. Install a full JDK 17+ so Tomcat can run the backend."
    }
    $env:JAVA_HOME = Split-Path -Parent (Split-Path -Parent $JavacPath)
    $env:JRE_HOME = $env:JAVA_HOME
}

$Jar = Get-CommandPath "jar"
if (!$Jar -and $env:JAVA_HOME) {
    $CandidateJar = Join-Path $env:JAVA_HOME "bin\jar.exe"
    if (Test-Path $CandidateJar) {
        $Jar = $CandidateJar
    }
}
if (!$Jar) {
    $Jar = Get-ChildItem -Path "C:\Program Files\Java" -Recurse -Filter "jar.exe" -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
}
if (!$Jar) {
    throw "jar.exe was not found. Install a full JDK 17+ so the WAR can be packaged correctly."
}

$ServletApiJar = Join-Path $TomcatDir "lib\servlet-api.jar"
if (!(Test-Path $ServletApiJar)) {
    throw "Tomcat servlet-api.jar was not found at $ServletApiJar"
}

Write-Host "Building backend WAR"
Remove-Item -Recurse -Force $BuildDir -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $BuildDir | Out-Null
Copy-Item -Recurse (Join-Path $ProjectRoot "web\*") $BuildDir

$ClassesDir = Join-Path $BuildDir "WEB-INF\classes"
$WebInfLibDir = Join-Path $BuildDir "WEB-INF\lib"
New-Item -ItemType Directory -Force -Path $ClassesDir, $WebInfLibDir | Out-Null
Copy-Item (Join-Path $LibDir "*.jar") $WebInfLibDir -Force

$JavaFiles = Get-ChildItem -Recurse -Filter "*.java" (Join-Path $ProjectRoot "src\java") | ForEach-Object { $_.FullName }
$CompileClasspath = @($ServletApiJar) + (Get-ChildItem -LiteralPath $LibDir -Filter "*.jar" | ForEach-Object { $_.FullName })
& $Javac -encoding UTF-8 -cp ($CompileClasspath -join [IO.Path]::PathSeparator) -d $ClassesDir $JavaFiles
if ($LASTEXITCODE -ne 0) {
    throw "Java compilation failed."
}

Remove-Item -Force $WarPath -ErrorAction SilentlyContinue
& $Jar --create --file $WarPath -C $BuildDir .
if ($LASTEXITCODE -ne 0) {
    throw "WAR packaging failed."
}

try {
    $JavaProcesses = Get-CimInstance Win32_Process -Filter "name = 'java.exe'" |
        Where-Object { $_.CommandLine -like "*$TomcatDir*" }
    foreach ($Process in $JavaProcesses) {
        Write-Host "Stopping existing backend Tomcat process $($Process.ProcessId)"
        Stop-Process -Id $Process.ProcessId -Force
    }
} catch {
    Write-Host "Skipping existing Tomcat process cleanup: $($_.Exception.Message)"
}

Start-Sleep -Seconds 2

Write-Host "Deploying $WarPath"
$WebappsDir = Join-Path $TomcatDir "webapps"
$DeployWar = Join-Path $WebappsDir $WarName
$DeployDir = Join-Path $WebappsDir $ContextName
Remove-Item -Recurse -Force $DeployWar, $DeployDir -ErrorAction SilentlyContinue
Copy-Item $WarPath $DeployWar

$ServerXml = Join-Path $TomcatDir "conf\server.xml"
$ServerXmlText = Get-Content $ServerXml -Raw
$ShutdownPort = $Port + 1000
$AjpPort = $Port + 1001
$ServerXmlText = [regex]::Replace($ServerXmlText, '<Server port="\d+" shutdown="SHUTDOWN">', "<Server port=`"$ShutdownPort`" shutdown=`"SHUTDOWN`">")
$ServerXmlText = [regex]::Replace($ServerXmlText, 'port="\d+" protocol="(HTTP/1\.1|org\.apache\.coyote\.http11\.Http11Protocol|org\.apache\.coyote\.http11\.Http11Nio2Protocol)"', "port=`"$Port`" protocol=`"org.apache.coyote.http11.Http11Nio2Protocol`"")
$ServerXmlText = [regex]::Replace($ServerXmlText, 'port="\d+" protocol="AJP/1\.3"', "port=`"$AjpPort`" protocol=`"AJP/1.3`"")
Set-Content -Path $ServerXml -Value $ServerXmlText -Encoding UTF8

$Startup = Join-Path $TomcatDir "bin\startup.bat"
Write-Host "Starting backend Tomcat on port $Port"
$env:CATALINA_HOME = $TomcatDir
$env:CATALINA_BASE = $TomcatDir
Start-Process -FilePath $Startup -WorkingDirectory (Join-Path $TomcatDir "bin") -WindowStyle Hidden

$Url = "http://localhost:$Port/$ContextName/web"
Write-Host ""
Write-Host "Backend API URL: $Url"
Write-Host "Frontend should use: VITE_API_URL=$Url"
