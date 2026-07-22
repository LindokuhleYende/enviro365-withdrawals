-- Seed data for Enviro365 Investments demo/testing.
-- Ages are computed at runtime from date_of_birth, so they will drift slightly year to year,
-- but John/Peter remain comfortably under 65 and Mary remains comfortably over 65.

INSERT INTO investors (full_name, email, date_of_birth) VALUES
    ('John Smith', 'john.smith@example.com', '1979-03-10'),
    ('Mary Johnson', 'mary.johnson@example.com', '1955-01-20'),
    ('Peter Ndlovu', 'peter.ndlovu@example.com', '1990-06-15');

INSERT INTO products (product_name, type, balance, investor_id) VALUES
    ('Discretionary Investment Plan', 'DISCRETIONARY', 250000.00, 1),
    ('Retirement Annuity Fund', 'RETIREMENT', 500000.00, 1),
    ('Retirement Annuity Fund', 'RETIREMENT', 800000.00, 2),
    ('Tax-Free Savings Account', 'TAX_FREE_SAVINGS', 120000.00, 2),
    ('Unit Trust Growth Fund', 'UNIT_TRUST', 90000.00, 3),
    ('Discretionary Investment Plan', 'DISCRETIONARY', 45000.00, 3);
