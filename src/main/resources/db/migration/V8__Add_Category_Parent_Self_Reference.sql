-- V8__Add_Category_Parent_Self_Reference.sql
-- Add parent field to Category for hierarchical category structure per PDF spec

-- Add parent_id column for self-referencing relationship
ALTER TABLE categories ADD COLUMN parent_id BIGINT;

-- Create foreign key constraint to allow self-referencing
ALTER TABLE categories 
ADD CONSTRAINT fk_category_parent 
FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL;

-- Create index for efficient parent queries
CREATE INDEX IF NOT EXISTS idx_category_parent_id ON categories(parent_id);

-- Add comment to document the relationship
COMMENT ON COLUMN categories.parent_id IS 'Self-referencing parent category ID for hierarchical structure';
