-- Shared categories. Reapplying this seed preserves existing records.
INSERT OR IGNORE INTO categories (id, name)
VALUES
    (1, 'Ăn uống'),
    (2, 'Di chuyển'),
    (3, 'Học tập'),
    (4, 'Mua sắm'),
    (5, 'Sinh hoạt'),
    (6, 'Lương / Trợ cấp'),
    (7, 'Khác');

-- Demo account: PBKDF2-HMAC-SHA1, 600000 iterations, 256-bit output.
-- Do not replace an existing account or reset its password/ID.
-- Room generates the ID. DemoDataSeeder links transactions.user_id to that ID.
INSERT OR IGNORE INTO local_account (
    username,
    salt,
    passwordHash,
    demoSeeded
)
VALUES (
    'vu98',
    'b2ea81dcc8c674ea3914db94b644bf0fae3a677bf0387dbd8bc06a3deea60b92',
    'a014aeb700922c52bd3cc1cd5ecde76baf01f54ea9454097acb3e45c9fecf021',
    0
);
