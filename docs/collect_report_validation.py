"""Collect actual Gradle result counts; run tests before using this script."""
from pathlib import Path
import xml.etree.ElementTree as ET
import json

ROOT=Path(__file__).resolve().parents[1]
def totals(pattern):
    files=list(ROOT.glob(pattern))
    if not files: raise FileNotFoundError(pattern)
    tests=failures=0
    for path in files:
        result=ET.parse(path).getroot()
        tests+=int(result.get('tests',0))
        failures+=int(result.get('failures',0))+int(result.get('errors',0))
    return tests,failures

unit,unit_failed=totals('app/build/test-results/testDebugUnitTest/TEST-*.xml')
android,android_failed=totals('app/build/outputs/androidTest-results/connected/debug/TEST-*.xml')
issues=ET.parse(ROOT/'app/build/reports/lint-results-debug.xml').getroot().findall('issue')
data={
    'date':'2026-10-21','source_commit':'88a210d',
    'unit_tests':unit,'unit_failures':unit_failed,
    'android_tests':android,'android_failures':android_failed,
    'lint_errors':sum(i.get('severity') in ('Error','Fatal') for i in issues),
    'lint_warnings':sum(i.get('severity')=='Warning' for i in issues),
    'host_scripts_passed':6,'screenshots':len(list((ROOT/'docs/screenshots/current').glob('*.png'))),
    'notes':[
        'First Android run: 8/9 passed; the restored seed-reference test split SQL comments at a semicolon.',
        'Fixed by stripping full-line SQL comments in that test; complete Android suite rerun: 9/9 passed.',
        'Current source includes persistent theme toggle and users table v7; current Android suite includes UserTableMigrationTest.',
        'Host script PASS values refer to the six scripts executed on 2026-10-21, not rerun by this collector.'
    ]
}
(ROOT/'docs/report_validation_current.json').write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(data,ensure_ascii=True))
