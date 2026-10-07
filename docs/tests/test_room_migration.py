"""Host SQLite migration check against KSP's exported Room schema.

This executes the actual SQL from MIGRATION_1_2. It complements, but does not
replace, the Android instrumentation test for Room opening/validation and CRUD.
"""
import json
import re
import sqlite3
from pathlib import Path

root = Path(__file__).resolve().parents[2]
source = (root / 'app/src/main/java/com/example/qlct/data/ExpenseDatabase.kt').read_text(encoding='utf-8')
migration = source.split('override fun migrate(db: SupportSQLiteDatabase) {', 1)[1].split('\n            }', 1)[0]
statements = re.findall(r'db\.execSQL\("([^"]+)"\)', migration)
assert len(statements) == 13, 'Review parser when migration implementation changes'
legacy = (root / 'docs/tests/schema.sql').read_text(encoding='utf-8')
room = json.loads((root / 'app/schemas/com.example.qlct.data.ExpenseDatabase/2.json').read_text(encoding='utf-8'))['database']

for empty in (False, True):
    with sqlite3.connect(':memory:') as db:
        db.execute('PRAGMA foreign_keys=ON')
        db.executescript(legacy)
        if empty:
            db.execute('DELETE FROM transactions')
        else:
            db.execute("UPDATE transactions SET note=? WHERE id=1", ("Ghi chú 'SQL'; giữ nguyên",))
        before = db.execute('SELECT * FROM transactions ORDER BY id').fetchall()
        categories = db.execute('SELECT * FROM categories ORDER BY id').fetchall()
        for sql in statements:
            db.execute(sql)
        assert db.execute('SELECT * FROM transactions ORDER BY id').fetchall() == before
        assert db.execute('SELECT * FROM categories ORDER BY id').fetchall() == categories
        assert db.execute('PRAGMA foreign_key_check').fetchall() == []
        for entity in room['entities']:
            table = entity['tableName']
            columns = {row[1]: row for row in db.execute(f'PRAGMA table_info({table})')}
            for field in entity['fields']:
                actual = columns[field['columnName']]
                assert actual[2] == field['affinity'], (table, field, actual)
                assert bool(actual[3]) == field['notNull'], (table, field, actual)
                assert bool(actual[5]) == (field['columnName'] in entity['primaryKey']['columnNames'])
            indices = {row[1]: row for row in db.execute(f'PRAGMA index_list({table})')}
            for index in entity['indices']:
                assert bool(indices[index['name']][2]) == index['unique']
                assert [row[2] for row in db.execute(f"PRAGMA index_info({index['name']})")] == index['columnNames']
        assert columns['note'][4] == "''"
        assert db.execute('PRAGMA foreign_key_list(transactions)').fetchone()[2:7] == ('categories', 'category_id', 'id', 'NO ACTION', 'NO ACTION')
        db.execute("INSERT INTO transactions(type,amount,category_id,date,note) VALUES ('EXPENSE',1,1,'2026-01-01','Mới')")
        assert db.execute('SELECT MAX(id) FROM transactions').fetchone()[0] > max([r[0] for r in before], default=0)
        assert not db.execute("SELECT name FROM sqlite_master WHERE name LIKE '%backup'").fetchall()
print('PASS: legacy data and empty database preserved; migrated columns, indexes, foreign key and default match Room schema.')
