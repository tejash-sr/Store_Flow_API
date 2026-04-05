-- V9__Create_Promotion_Table.sql
-- Create Promotion table for discount/seasonal promotions per PDF spec

CREATE TABLE IF NOT EXISTS promotions (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    discount_percentage DECIMAL(5, 2) NOT NULL CHECK (discount_percentage > 0 AND discount_percentage <= 100),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_promotion_product 
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_promotion_dates 
        CHECK (end_date > start_date)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_promotion_product_id ON promotions(product_id);
CREATE INDEX IF NOT EXISTS idx_promotion_code ON promotions(code);
CREATE INDEX IF NOT EXISTS idx_promotion_active ON promotions(active);
CREATE INDEX IF NOT EXISTS idx_promotion_dates ON promotions(start_date, end_date);

-- Add comments
COMMENT ON TABLE promotions IS 'Time-limited promotional discounts for products';
COMMENT ON COLUMN promotions.code IS 'Unique promotion/coupon code';
COMMENT ON COLUMN promotions.discount_percentage IS 'Discount percentage (0-100)';
COMMENT ON COLUMN promotions.active IS 'Whether promotion is currently active';
