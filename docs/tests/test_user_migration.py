"""Verify actual v6->v7 SQL, data, FK definitions, indexes and ID sequences."""
from pathlib import Path
import json, re, sqlite3

root=Path(__file__).resolve().parents[2]
schemas=root/'app/schemas/com.example.qlct.model.database.ExpenseDatabase'
v6=json.loads((schemas/'6.json').read_text(encoding='utf-8'))['database']
v7=json.loads((schemas/'7.json').read_text(encoding='utf-8'))['database']
source=(root/'app/src/main/java/com/example/qlct/model/database/ExpenseDatabase.kt').read_text(encoding='utf-8')
block=source.split('val MIGRATION_6_7 =',1)[1].split('fun getInstance',1)[0]
statements=re.findall(r'db.execSQL\("([^"\n]+)"\)',block)
assert len(statements)==17
for foreign_keys in (False,True):
    for empty in (False,True):
        with sqlite3.connect(':memory:') as db:
            db.execute(f'PRAGMA foreign_keys={int(foreign_keys)}')
            # Simulate old SQLite rename behavior too: references may retain the old name.
            db.execute('PRAGMA legacy_alter_table=ON')
            for entity in v6['entities']:
                db.execute(entity['createSql'].replace('${TABLE_NAME}',entity['tableName']))
                for index in entity.get('indices',[]):
                    db.execute(index['createSql'].replace('${TABLE_NAME}',entity['tableName']))
            if not empty:
                db.execute("INSERT INTO categories VALUES (1,'Food')")
                db.execute("INSERT INTO local_account VALUES (7,'first','salt','hash',1)")
                db.execute("INSERT INTO local_account VALUES (9,'second','salt2','hash2',0)")
                db.execute("INSERT INTO transactions VALUES (42,'EXPENSE',45,1,'2026-10-21','keep',7)")
                db.execute("INSERT INTO transactions VALUES (99,'EXPENSE',70,1,'2026-10-21','unassigned',NULL)")
                db.execute("INSERT INTO transactions VALUES (9000,'EXPENSE',1,1,'2026-10-21','deleted',9)")
                db.execute('DELETE FROM transactions WHERE id=9000')
                db.execute("INSERT INTO budgets VALUES (7,'2026-10',2000)")
                db.execute("INSERT INTO budgets VALUES (9,'2026-10',3000)")
            before={table:db.execute('SELECT * FROM '+table).fetchall() for table in ('local_account','transactions','budgets')}
            for sql in statements: db.execute(sql)
            assert before['local_account']==db.execute('SELECT * FROM users').fetchall()
            for table in ('transactions','budgets'): assert before[table]==db.execute('SELECT * FROM '+table).fetchall()
            assert db.execute("SELECT name FROM sqlite_master WHERE name='local_account'").fetchall()==[]
            assert db.execute('PRAGMA foreign_key_check').fetchall()==[]
            for entity in v7['entities']:
                table=entity['tableName']
                columns={r[1]:r for r in db.execute(f'PRAGMA table_info({table})')}
                for field in entity['fields']:
                    col=columns[field['columnName']]
                    assert col[2]==field['affinity'] and bool(col[3])==field.get('notNull',False)
                indexes={r[1] for r in db.execute(f'PRAGMA index_list({table})')}
                for index in entity.get('indices',[]): assert index['name'] in indexes
                actual={r[2] for r in db.execute(f'PRAGMA foreign_key_list({table})')}
                assert actual=={fk['table'] for fk in entity.get('foreignKeys',[])}
            if not empty:
                row=db.execute("INSERT INTO transactions(type,amount,category_id,date,note,user_id) VALUES ('INCOME',10,1,'2026-10-21','new',7)")
                assert row.lastrowid>9000
print('PASS: v6-v7 users rename, empty/populated data, passwords/flags/ownership, budgets, FKs, indexes and autoincrement sequence; FK on/off and legacy rename.')
