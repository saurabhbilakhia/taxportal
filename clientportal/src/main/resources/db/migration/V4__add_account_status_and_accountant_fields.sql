-- Add account status and accountant-specific fields to users table

ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'APPROVED';
ALTER TABLE users ADD COLUMN license_number VARCHAR(100);
ALTER TABLE users ADD COLUMN firm_name VARCHAR(255);

-- Create index for status column for faster queries
CREATE INDEX idx_users_status ON users(status);
