-- Extraction results table to store Nanonets extraction data
CREATE TABLE extraction_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    nanonets_request_id VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    extracted_data JSONB,
    raw_response JSONB,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT valid_extraction_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

-- Notifications table for tracking sent notifications
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    message TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_notification_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

-- Indexes
CREATE INDEX idx_extraction_results_document_id ON extraction_results(document_id);
CREATE INDEX idx_extraction_results_order_id ON extraction_results(order_id);
CREATE INDEX idx_extraction_results_status ON extraction_results(status);
CREATE INDEX idx_extraction_results_nanonets_request_id ON extraction_results(nanonets_request_id);
CREATE INDEX idx_notifications_order_id ON notifications(order_id);
CREATE INDEX idx_notifications_status ON notifications(status);
