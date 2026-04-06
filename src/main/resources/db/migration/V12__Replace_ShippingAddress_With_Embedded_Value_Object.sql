-- V12__Replace_ShippingAddress_With_Embedded_Value_Object.sql
-- Replace shippingAddress String column with ShippingAddress @Embeddable value object
-- This aligns the Order entity model with PDF spec requirements

-- Drop the old single-column shipping address
ALTER TABLE orders DROP COLUMN IF EXISTS shipping_address;

-- Add the embeddable shipping address columns
ALTER TABLE orders ADD COLUMN shipping_street VARCHAR(255);
ALTER TABLE orders ADD COLUMN shipping_city VARCHAR(100);
ALTER TABLE orders ADD COLUMN shipping_state VARCHAR(100);
ALTER TABLE orders ADD COLUMN shipping_postal_code VARCHAR(20);
ALTER TABLE orders ADD COLUMN shipping_country VARCHAR(100);

-- Add comment to document the embedded columns
COMMENT ON COLUMN orders.shipping_street IS 'Part of ShippingAddress embedded value object';
COMMENT ON COLUMN orders.shipping_city IS 'Part of ShippingAddress embedded value object';
COMMENT ON COLUMN orders.shipping_state IS 'Part of ShippingAddress embedded value object';
COMMENT ON COLUMN orders.shipping_postal_code IS 'Part of ShippingAddress embedded value object';
COMMENT ON COLUMN orders.shipping_country IS 'Part of ShippingAddress embedded value object';
