"""Exercise the shipped Room schema and seed against a persistent SQLite file."""
from pathlib import Path
import hashlib
import json
import sqlite3
import tempfile

root = Path(__file__).resolve().parents[2]
schema = json.loads((root / 'app/schemas/com.example.qlct.data.ExpenseDatabase/5.json').read_text())['database']
seed = (root / 'app/src/main/assets/seed.sql').read_text(encoding='utf-8')

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
    db.executescript(seed)
    user_id, salt, stored = db.execute("SELECT id,salt,passwordHash FROM local_account WHERE username='vu98'").fetchone()
    assert user_id != 1
    assert hashlib.pbkdf2_hmac('sha1', b'11111111', bytes.fromhex(salt), 600_000, 32).hex() == stored
    assert hashlib.pbkdf2_hmac('sha1', b'wrong-password', bytes.fromhex(salt), 600_000, 32).hex() != stored
    db.execute("INSERT INTO transactions(type,amount,category_id,date,note,user_id) VALUES ('EXPENSE',45000,1,'2026-10-07','Saved transaction',?)", (user_id,))
    db.execute('UPDATE local_account SET demoSeeded=1 WHERE id=?', (user_id,))
    db.commit()
    db.close()
    db = connect()
    db.executescript(seed)
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
    db.executescript(seed)
    assert db.execute('SELECT passwordHash FROM local_account WHERE id=?', (user_id,)).fetchone()[0] == 'changed-by-user'
    assert db.execute('PRAGMA foreign_key_check').fetchall() == []
    db.close()
print('PASS: demo password hash, persistent account/transactions, account FK, idempotent seed, existing password preservation')
