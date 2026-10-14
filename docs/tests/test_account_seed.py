"""Check runtime initializer credentials and SQLite persistence on the host.

Room DAO execution is covered by DatabaseInitializerTest on Android.
"""
from pathlib import Path
import hashlib
import json
import re
import sqlite3
import tempfile

root = Path(__file__).resolve().parents[2]
schema = json.loads((root / 'app/schemas/com.example.qlct.model.database.ExpenseDatabase/5.json').read_text())['database']
source = (root / 'app/src/main/java/com/example/qlct/model/database/DatabaseInitializer.kt').read_text(encoding='utf-8')
categories = [(int(i), name) for i, name in re.findall(r'Category\((\d+), "([^"]+)"\)', source)]
account = tuple(re.search(field + r' = "([^"]+)"', source).group(1) for field in ('username', 'salt', 'passwordHash'))
assert len(categories) == 7

def initialize_reference(db):
    db.executemany('INSERT OR IGNORE INTO categories(id,name) VALUES (?,?)', categories)
    db.execute('INSERT OR IGNORE INTO local_account(username,salt,passwordHash,demoSeeded) VALUES (?,?,?,0)', account)
    db.commit()

with tempfile.TemporaryDirectory() as folder:
    path = Path(folder) / 'qlct.db'
    def connect():
        db = sqlite3.connect(path)
        db.execute('PRAGMA foreign_keys=ON')
        return db
    db = connect()
    for entity in schema['entities']:
        db.execute(entity['createSql'].replace('${TABLE_NAME}', entity['tableName']))
        for index in entity['indices']:
            db.execute(index['createSql'].replace('${TABLE_NAME}', entity['tableName']))
    # Existing accounts mean the demo ID must not be assumed to be 1.
    db.execute("INSERT INTO local_account(username,salt,passwordHash,demoSeeded) VALUES ('existing','salt','hash',0)")
    db.commit()
    initialize_reference(db)
    user_id, salt, stored = db.execute("SELECT id,salt,passwordHash FROM local_account WHERE username='vu98'").fetchone()
    assert user_id != 1
    assert hashlib.pbkdf2_hmac('sha1', b'11111111', bytes.fromhex(salt), 600_000, 32).hex() == stored
    assert hashlib.pbkdf2_hmac('sha1', b'wrong-password', bytes.fromhex(salt), 600_000, 32).hex() != stored
    db.execute("INSERT INTO transactions(type,amount,category_id,date,note,user_id) VALUES ('EXPENSE',45000,1,'2026-10-07','Saved transaction',?)", (user_id,))
    db.execute('UPDATE local_account SET demoSeeded=1 WHERE id=?', (user_id,))
    db.commit()
    db.close()
    db = connect()
    initialize_reference(db)
    assert db.execute('SELECT COUNT(*) FROM categories').fetchone()[0] == 7
    assert db.execute('SELECT COUNT(*) FROM local_account').fetchone()[0] == 2
    assert db.execute('SELECT amount FROM transactions WHERE user_id=?', (user_id,)).fetchone()[0] == 45000
    assert db.execute('SELECT COUNT(*) FROM transactions WHERE user_id=1').fetchone()[0] == 0
    assert db.execute('SELECT demoSeeded FROM local_account WHERE id=?', (user_id,)).fetchone()[0] == 1
    try:
        db.execute("INSERT INTO transactions(type,amount,category_id,date,note,user_id) VALUES ('EXPENSE',1,1,'2026-10-07','',99999)")
        raise AssertionError('Invalid account accepted')
    except sqlite3.IntegrityError:
        pass
    db.execute("UPDATE local_account SET passwordHash='changed-by-user' WHERE id=?", (user_id,))
    db.commit()
    initialize_reference(db)
    assert db.execute('SELECT passwordHash FROM local_account WHERE id=?', (user_id,)).fetchone()[0] == 'changed-by-user'
    assert db.execute('PRAGMA foreign_key_check').fetchall() == []
    db.close()
print('PASS: demo password hash, persistent account/transactions, account FK, idempotent seed, existing password preservation')
