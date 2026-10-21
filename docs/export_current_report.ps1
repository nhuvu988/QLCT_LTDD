param([string]$ReportFileName = 'Bao_cao_Do_an_QLCT.docx')
$ErrorActionPreference = 'Stop'
$reportPath = Join-Path $PSScriptRoot $ReportFileName
$pdfPath = [System.IO.Path]::ChangeExtension($reportPath, '.pdf')
$word = $null
$document = $null
try {
    $word = New-Object -ComObject Word.Application
    $word.Visible = $false
    $word.DisplayAlerts = 0
    $document = $word.Documents.Open($reportPath, $false, $false)
    $document.Repaginate()
    foreach ($toc in $document.TablesOfContents) { $toc.Update() }
    $document.Fields.Update() | Out-Null
    $document.Repaginate()
    $document.Fields.Update() | Out-Null
    $document.Save()
    $document.ExportAsFixedFormat($pdfPath, 17)
    $pages = $document.ComputeStatistics(2)
    Write-Output "Exported PDF; pages: $pages"
} finally {
    if ($null -ne $document) {
        try { $document.Close(0) } catch [System.Runtime.InteropServices.COMException] { Write-Warning 'Word already closed the document.' }
    }
    if ($null -ne $word) {
        try { $word.Quit() } catch [System.Runtime.InteropServices.COMException] { Write-Warning 'Word is no longer available after export.' }
    }
}
