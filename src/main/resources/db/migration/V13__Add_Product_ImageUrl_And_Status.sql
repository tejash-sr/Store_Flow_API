-- V13__Add_Product_ImageUrl_And_Status.sql
-- Adds image_url and status columns to the products table per PDF requirement P2-1.
-- The status enum replaces the boolean is_active as the authoritative product state.

ALTER TABLE products ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

ALTER TABLE products ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Migrate existing is_active values to status
UPDATE products SET status = 'INACTIVE' WHERE is_active = false;

CREATE INDEX IF NOT EXISTS idx_product_status ON products(status);
