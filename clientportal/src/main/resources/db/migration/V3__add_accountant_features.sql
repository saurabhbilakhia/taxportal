-- Add role to users table
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'CLIENT';
ALTER TABLE users ADD CONSTRAINT valid_user_role
    CHECK (role IN ('CLIENT', 'ACCOUNTANT', 'ADMIN'));
CREATE INDEX idx_users_role ON users(role);

-- Extraction overrides audit table
CREATE TABLE extraction_overrides (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    extraction_result_id UUID NOT NULL REFERENCES extraction_results(id) ON DELETE CASCADE,
    previous_data JSONB NOT NULL,
    new_data JSONB NOT NULL,
    override_reason TEXT,
    overridden_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_extraction_overrides_result ON extraction_overrides(extraction_result_id);
CREATE INDEX idx_extraction_overrides_user ON extraction_overrides(overridden_by);
CREATE INDEX idx_extraction_overrides_created ON extraction_overrides(created_at DESC);
