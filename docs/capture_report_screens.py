"""Capture actual UI on a separate report preview installation; never clears the main app."""
from pathlib import Path
import subprocess, time, re, xml.etree.ElementTree as ET, sys, json

ROOT = Path(__file__).resolve().parents[1]
ADB = 'C:/Users/admin/AppData/Local/Android/Sdk/platform-tools/adb.exe'
PACKAGE = 'com.example.qlct.reportpreview'
OUT = ROOT / 'docs/screenshots/current'
OUT.mkdir(parents=True, exist_ok=True)

def adb(*args):
    return subprocess.check_output([ADB, *args])

def nodes():
    adb('shell', 'uiautomator', 'dump', '/sdcard/qlct-report.xml')
    return list(ET.fromstring(adb('shell', 'cat', '/sdcard/qlct-report.xml')).iter('node'))

def tap_node(node):
    x1,y1,x2,y2 = map(int,re.findall(r'\d+', node.get('bounds')))
    adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2))
    time.sleep(.35)

def tap(text):
    found = [n for n in nodes() if n.get('text') == text or n.get('content-desc') == text]
    if not found: raise RuntimeError('Missing UI control: '+text)
    tap_node(found[-1])

def fill(index, value):
    fields = [n for n in nodes() if n.get('class') == 'android.widget.EditText']
    tap_node(fields[index])
    adb('shell','input','keyevent','KEYCODE_MOVE_END')
    for _ in range(len(fields[index].get('text',''))): adb('shell','input','keyevent','KEYCODE_DEL')
    adb('shell','input','text',value.replace(' ', '%s'))
    adb('shell','input','keyevent','KEYCODE_BACK')
    time.sleep(.25)

def shot(name):
    time.sleep(.5)
    (OUT/(name+'.png')).write_bytes(adb('exec-out','screencap','-p'))
    print(name, flush=True)

def scroll():
    adb('shell','input','swipe','520','1800','520','700','500')
    time.sleep(.5)

def phase_one():
    adb('install','-r',str(ROOT/'app/build/outputs/apk/debug/app-debug.apk'))
    adb('shell','am','start','-W','-n',PACKAGE+'/com.example.qlct.MainActivity')
    time.sleep(2)
    shot('01_login')
    tap('Chưa có tài khoản? Đăng ký')
    shot('02_register')
    tap('Đã có tài khoản? Đăng nhập')
    fill(0,'vu98'); fill(1,'11111111'); tap('Đăng nhập')
    for _ in range(8):
        if any(n.get('text') == 'Tài khoản: vu98' for n in nodes()): break
        time.sleep(1)
    if not any(n.get('text') == 'Tài khoản: vu98' for n in nodes()):
        raise RuntimeError('Demo login failed')
    shot('03_overview')
    tap('Giao dịch'); shot('04_transactions')
    fill(0,'an uong'); shot('05_search')
    tap('Xóa')
    tap('Thống kê'); scroll(); shot('06_category_statistics')
    scroll(); shot('07_month_statistics')
    tap('Ngân sách'); shot('08_budget_empty')

def phase_two():
    tap('Đặt ngân sách'); shot('09_budget_editor')
    fill(0,'1500000'); tap('Lưu'); time.sleep(.6); shot('10_budget_warning')
    tap('Sửa ngân sách'); fill(0,'1000000'); tap('Lưu'); time.sleep(.6); shot('11_budget_exceeded')
    tap('Thêm giao dịch'); shot('12_add_transaction')
    tap('Lưu'); shot('13_amount_validation')
    fill(0,'3000000'); tap('Thu tiền')
    # The category picker and calendar are UI-only; dismiss without saving.
    category = next(n.get('text') for n in nodes() if n.get('text','').startswith('Danh mục:'))
    tap(category); shot('14_category_picker')
    tap('Lương / Trợ cấp')
    date = next(n.get('text') for n in nodes() if n.get('text','').startswith('Ngày:'))
    tap(date); shot('15_date_picker'); tap('Hủy')
    fill(1,'DEMO_bao_cao_thu_nhap'); shot('16_income_form'); tap('Lưu')
    time.sleep(.8)
    tap('Giao dịch'); shot('17_saved_income')
    # Opening edit/delete does not alter the saved demonstration record.
    row = next(n for n in nodes() if 'DEMO_bao_cao_thu_nhap' in n.get('text',''))
    tap_node(row); shot('18_edit_transaction'); tap('Hủy')
    trash = [n for n in nodes() if 'Xóa giao dịch' in n.get('content-desc','')]
    if trash:
        tap_node(trash[0]); shot('19_delete_confirmation'); tap('Hủy')
    tap('Tổng quan'); shot('20_overview_income')

if __name__ == '__main__':
    {'one':phase_one,'two':phase_two}[sys.argv[1]]()
