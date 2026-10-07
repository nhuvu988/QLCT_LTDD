"""Run actual migration SQL and compare its resulting schema with Room v6."""
from pathlib import Path
import json, re, sqlite3, tempfile
root=Path(__file__).resolve().parents[2]
schemas=root/'app/schemas/com.example.qlct.data.ExpenseDatabase'
v5=json.loads((schemas/'5.json').read_text())['database']
v6=json.loads((schemas/'6.json').read_text())['database']
source=(root/'app/src/main/java/com/example/qlct/data/ExpenseDatabase.kt').read_text(encoding='utf-8')
block=source.split('val MIGRATION_5_6 =',1)[1].split('fun getInstance',1)[0]
statements=re.findall(r'db.execSQL\("([^"\n]+)"\)',block)
assert statements
with tempfile.TemporaryDirectory() as folder:
    path=Path(folder)/'qlct.db'
    db=sqlite3.connect(path)
    db.execute('PRAGMA foreign_keys=ON')
    for entity in v5['entities']:
        db.execute(entity['createSql'].replace('${TABLE_NAME}',entity['tableName']))
        for index in entity['indices']: db.execute(index['createSql'].replace('${TABLE_NAME}',entity['tableName']))
    db.executescript((root/'app/src/main/assets/seed.sql').read_text(encoding='utf-8'))
    owner=db.execute("SELECT id FROM local_account WHERE username='vu98'").fetchone()[0]
    db.execute("INSERT INTO local_account(username,salt,passwordHash,demoSeeded) VALUES ('second','s','h',0)")
    other=db.execute("SELECT id FROM local_account WHERE username='second'").fetchone()[0]
    db.execute("INSERT INTO transactions(type,amount,category_id,date,note,user_id) VALUES ('EXPENSE',800,1,'2026-10-07','keep',?)",(owner,))
    before=db.execute('SELECT * FROM transactions').fetchall()
    accounts=db.execute('SELECT * FROM local_account').fetchall()
    for sql in statements: db.execute(sql)
    assert before==db.execute('SELECT * FROM transactions').fetchall()
    assert accounts==db.execute('SELECT * FROM local_account').fetchall()
    cols={r[1]:r for r in db.execute('PRAGMA table_info(budgets)')}
    entity=next(e for e in v6['entities'] if e['tableName']=='budgets')
    for field in entity['fields']:
        actual=cols[field['columnName']]
        assert actual[2]==field['affinity'] and bool(actual[3])==field['notNull']
    assert cols['user_id'][5]==1 and cols['month'][5]==2
    db.executemany('INSERT INTO budgets VALUES (?,?,?)',[(owner,'2026-10',1000),(owner,'2026-11',3000),(other,'2026-10',9000)])
    try:
        db.execute('INSERT INTO budgets VALUES (?,?,?)',(owner,'2026-10',500))
        raise AssertionError('Duplicate month accepted')
    except sqlite3.IntegrityError: pass
    try:
        db.execute("INSERT INTO budgets VALUES (99999,'2026-10',500)")
        raise AssertionError('Missing account accepted')
    except sqlite3.IntegrityError: pass
    db.execute("UPDATE budgets SET amount=2000 WHERE user_id=? AND month='2026-10'",(owner,))
    db.commit(); db.close()
    db=sqlite3.connect(path)
    assert db.execute("SELECT amount FROM budgets WHERE user_id=? AND month='2026-10'",(owner,)).fetchone()[0]==2000
    db.execute("DELETE FROM budgets WHERE user_id=? AND month='2026-10'",(owner,))
    assert db.execute('SELECT amount FROM budgets WHERE user_id=?',(other,)).fetchone()[0]==9000
    assert before==db.execute('SELECT * FROM transactions').fetchall()
    assert db.execute('PRAGMA foreign_key_check').fetchall()==[]
    db.close()
print('PASS: v5-v6 migration, data preserved, budget schema, owner/month uniqueness, FK and persistence')
