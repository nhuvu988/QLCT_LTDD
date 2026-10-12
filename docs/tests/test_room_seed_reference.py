"""Check that reference SQL documents the same defaults as the runtime Room initializer."""
import json
import re
import sqlite3
from pathlib import Path

root = Path(__file__).resolve().parents[2]
source = (root / 'app/src/main/java/com/example/qlct/model/database/DatabaseInitializer.kt').read_text(encoding='utf-8')
schema = json.loads((root / 'app/schemas/com.example.qlct.model.database.ExpenseDatabase/6.json').read_text(encoding='utf-8'))['database']
with sqlite3.connect(':memory:') as db:
    for entity in schema['entities']:
        db.execute(entity['createSql'].replace('${TABLE_NAME}', entity['tableName']))
        for index in entity.get('indices', []):
            db.execute(index['createSql'].replace('${TABLE_NAME}', entity['tableName']))
    db.executescript((root / 'app/src/main/assets/seed.sql').read_text(encoding='utf-8'))
    expected = [(int(i), name) for i, name in re.findall(r'Category\((\d+), "([^"]+)"\)', source)]
    assert len(expected) == 7
    assert db.execute('SELECT id, name FROM categories ORDER BY id').fetchall() == expected
    account = tuple(re.search(field + r' = "([^"]+)"', source).group(1) for field in ('username', 'salt', 'passwordHash'))
    assert db.execute('SELECT username, salt, passwordHash FROM local_account').fetchall() == [account]
print('PASS: reference SQL matches Room initializer categories and demo credentials.')
