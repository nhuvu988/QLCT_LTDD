"""Host-side SQLite contract tests, not Android instrumentation tests."""
import sqlite3
import tempfile
from pathlib import Path

schema = (Path(__file__).parent / 'schema.sql').read_text(encoding='utf-8')
with tempfile.TemporaryDirectory(dir=Path(__file__).parent) as folder:
    path = Path(folder) / 'qlct.db'
    db = sqlite3.connect(path)
    db.execute('PRAGMA foreign_keys=ON')
    db.executescript(schema)
    assert db.execute('SELECT count(*) FROM categories').fetchone()[0] == 7
    assert db.execute('SELECT count(*) FROM transactions').fetchone()[0] == 8
    totals = dict(db.execute('SELECT type,sum(amount) FROM transactions GROUP BY type'))
    assert totals == {'INCOME': 4200000, 'EXPENSE': 1345000}, totals
    assert totals['INCOME'] - totals['EXPENSE'] == 2855000
    for amount, category, kind in [(0,1,'EXPENSE'),(-1,1,'EXPENSE'),(1000000000000,1,'EXPENSE'),(1,999,'EXPENSE'),(1,1,'INVALID')]:
        try:
            db.execute("INSERT INTO transactions(type,amount,category_id,date) VALUES (?,?,?,'2026-10-05')",(kind,amount,category))
        except sqlite3.IntegrityError:
            pass
        else:
            raise AssertionError('Invalid row accepted')
    note = "Tiền sách 'SQL'; không thực thi"
    row_id = db.execute("INSERT INTO transactions(type,amount,category_id,date,note) VALUES ('EXPENSE',10000,3,'2026-01-01',?)",(note,)).lastrowid
    db.execute('UPDATE transactions SET amount=20000 WHERE id=?',(row_id,))
    db.commit()
    db.close()
    db = sqlite3.connect(path)
    assert db.execute('SELECT amount,note FROM transactions WHERE id=?',(row_id,)).fetchone() == (20000,note)
    assert db.execute("SELECT count(*) FROM transactions WHERE date LIKE '2026-01-%' AND id=?",(row_id,)).fetchone()[0] == 1
    db.execute('DELETE FROM transactions WHERE id=?',(row_id,))
    db.execute('DELETE FROM transactions')
    db.commit()
    db.close()
    db = sqlite3.connect(path)
    assert db.execute('SELECT count(*) FROM transactions').fetchone()[0] == 0
    db.close()
print('PASS: seed totals, constraints, insert/update/delete, date query, persistence, empty database.')
