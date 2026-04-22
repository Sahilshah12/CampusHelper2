param(
    [switch]$SkipBuild,
    [switch]$SkipInstall,
    [string]$JavaHomeOverride = "C:\Program Files\Java\jdk-24"
)

$ErrorActionPreference = "Stop"

function Write-Step($msg) {
    Write-Host "[STEP] $msg" -ForegroundColor Cyan
}

function Write-Info($msg) {
    Write-Host "[INFO] $msg" -ForegroundColor Gray
}

function Write-Ok($msg) {
    Write-Host "[PASS] $msg" -ForegroundColor Green
}

function Write-Warn($msg) {
    Write-Host "[WARN] $msg" -ForegroundColor Yellow
}

function Read-SdkDir {
    $line = Get-Content -Path "local.properties" | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
    if (-not $line) {
        throw "sdk.dir not found in local.properties"
    }

    $raw = ($line -replace '^sdk\.dir=', '')
    return [Regex]::Unescape($raw)
}

function Invoke-Gradle($task) {
    Write-Step "Running Gradle task: $task"
    & .\gradlew.bat $task --no-daemon
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle task failed: $task"
    }
    Write-Ok "$task succeeded"
}

Push-Location (Split-Path -Parent $MyInvocation.MyCommand.Path)
Push-Location ".."

try {
    Write-Info "Workspace: $(Get-Location)"

    $oldJavaHome = $env:JAVA_HOME
    if (Test-Path $JavaHomeOverride) {
        $env:JAVA_HOME = $JavaHomeOverride
        Write-Info "JAVA_HOME set to $JavaHomeOverride for this run"
    } else {
        Write-Warn "Java override path not found: $JavaHomeOverride"
    }

    if (-not $SkipBuild) {
        Invoke-Gradle ":app:compileDebugKotlin"
        Invoke-Gradle ":app:assembleDebug"
    } else {
        Write-Warn "Build steps skipped"
    }

    Write-Step "Resolving adb path"
    $sdkDir = Read-SdkDir
    $adbPath = Join-Path $sdkDir "platform-tools\adb.exe"
    Write-Info "SDK_DIR=$sdkDir"
    Write-Info "ADB_PATH=$adbPath"

    if (-not (Test-Path $adbPath)) {
        throw "adb not found at $adbPath"
    }

    Write-Step "Checking connected devices"
    $adbOutput = & $adbPath devices
    $adbOutput | ForEach-Object { Write-Host $_ }

    $deviceSerial = $null
    foreach ($line in $adbOutput) {
        if ($line -match '^(\S+)\s+device$') {
            $deviceSerial = $Matches[1]
            break
        }
    }

    if (-not $deviceSerial) {
        Write-Warn "No connected device in 'device' state. Manual smoke cannot run."
        exit 0
    }

    Write-Ok "Device detected: $deviceSerial"

    if (-not $SkipInstall) {
        Invoke-Gradle ":app:installDebug"

        Write-Step "Launching app"
        & $adbPath -s $deviceSerial shell am start -n com.campushelper.app/.ui.auth.SplashActivity
        if ($LASTEXITCODE -eq 0) {
            Write-Ok "App launch command sent"
        } else {
            Write-Warn "Launch command returned exit code $LASTEXITCODE"
        }
    } else {
        Write-Warn "Install/launch steps skipped"
    }

    Write-Host ""
    Write-Ok "Preflight complete. Run manual checklist now: SMOKE_TEST_CHECKLIST_FIREBASE.md"
}
finally {
    if ($null -ne $oldJavaHome) {
        $env:JAVA_HOME = $oldJavaHome
    }
    Pop-Location
    Pop-Location
}
