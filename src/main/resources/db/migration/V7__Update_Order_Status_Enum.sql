-- V7__Update_Order_Status_Enum.sql
-- Migrate Order.status enum from (PENDING, PROCESSING, SHIPPED, DELIVERED, COMPLETED, CANCELLED)
-- to (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED) per spec requirements

-- 1. Create new status column with correct values
ALTER TABLE orders ADD COLUMN status_new VARCHAR(20);

-- 2. Migrate existing data
UPDATE orders 
SET status_new = 'PENDING' 
WHERE status IN ('PENDING');

UPDATE orders 
SET status_new = 'CONFIRMED' 
WHERE status IN ('PROCESSING');

UPDATE orders 
SET status_new = 'SHIPPED' 
WHERE status IN ('SHIPPED');

UPDATE orders 
SET status_new = 'DELIVERED' 
WHERE status IN ('DELIVERED', 'COMPLETED');

UPDATE orders 
SET status_new = 'CANCELLED' 
WHERE status IN ('CANCELLED');

-- 3. Set default for any unmapped values
UPDATE orders 
SET status_new = 'PENDING' 
WHERE status_new IS NULL;

-- 4. Drop old column and rename new one
ALTER TABLE orders DROP COLUMN status;
ALTER TABLE orders RENAME COLUMN status_new TO status;

-- 5. Add back constraint (PostreSQL syntax)
ALTER TABLE orders ALTER COLUMN status SET NOT NULL;
ALTER TABLE orders ALTER COLUMN status SET DEFAULT 'PENDING';

-- Create index on new status column for query performance
CREATE INDEX IF NOT EXISTS idx_order_status ON orders(status);
