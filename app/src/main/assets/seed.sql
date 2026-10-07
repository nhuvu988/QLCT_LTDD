INSERT INTO categories (id, name)
VALUES
    (1, 'Ăn uống'),
    (2, 'Di chuyển'),
    (3, 'Học tập'),
    (4, 'Mua sắm'),
    (5, 'Sinh hoạt'),
    (6, 'Lương / Trợ cấp'),
    (7, 'Khác');

INSERT OR IGNORE INTO local_account (
    username,
    salt,
    passwordHash,
    demoSeeded
)
VALUES (
    'vu98',
    'a108b12e1a04ea031f638422603a503985c4aa82fa657eca86038d079b3ab520',
    '5bce8fc6f406af2653fdb2b74a8527d1dcc079f0efb9d982933068a0e5a25b17',
    0
);
