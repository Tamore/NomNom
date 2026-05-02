-- NomNom Supabase Schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table (extended from Supabase Auth)
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  email VARCHAR UNIQUE NOT NULL,
  username VARCHAR,
  avatar_id VARCHAR,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Collections table
CREATE TABLE IF NOT EXISTS collections (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name VARCHAR NOT NULL,
  description TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tags table
CREATE TABLE IF NOT EXISTS tags (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name VARCHAR NOT NULL,
  color VARCHAR DEFAULT '#000000',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, name)
);

-- Recipes table
CREATE TABLE IF NOT EXISTS recipes (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  title VARCHAR NOT NULL,
  ingredients TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
  steps TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
  source_url VARCHAR,
  source_type VARCHAR, -- 'instagram', 'youtube', 'text', 'custom'
  prep_time_minutes INTEGER,
  cook_time_minutes INTEGER,
  servings INTEGER,
  notes TEXT,
  tags TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
  is_global BOOLEAN DEFAULT false,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Recipe Tags junction table
CREATE TABLE IF NOT EXISTS recipe_tags (
  recipe_id UUID NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
  tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
  PRIMARY KEY (recipe_id, tag_id)
);

-- Recipe Collections junction table
CREATE TABLE IF NOT EXISTS recipe_collections (
  recipe_id UUID NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
  collection_id UUID NOT NULL REFERENCES collections(id) ON DELETE CASCADE,
  PRIMARY KEY (recipe_id, collection_id)
);

-- Row Level Security (RLS) Policies

-- Enable RLS
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE collections ENABLE ROW LEVEL SECURITY;
ALTER TABLE tags ENABLE ROW LEVEL SECURITY;
ALTER TABLE recipes ENABLE ROW LEVEL SECURITY;
ALTER TABLE recipe_tags ENABLE ROW LEVEL SECURITY;
ALTER TABLE recipe_collections ENABLE ROW LEVEL SECURITY;

-- Users RLS: Users can only see their own profile
CREATE POLICY users_own_profile ON users
  FOR SELECT USING (auth.uid() = id);

-- Collections RLS: Users can only see their own collections
CREATE POLICY collections_own_data ON collections
  FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY collections_insert_own ON collections
  FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY collections_update_own ON collections
  FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY collections_delete_own ON collections
  FOR DELETE USING (auth.uid() = user_id);

-- Tags RLS: Users can only see their own tags
CREATE POLICY tags_own_data ON tags
  FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY tags_insert_own ON tags
  FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY tags_update_own ON tags
  FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY tags_delete_own ON tags
  FOR DELETE USING (auth.uid() = user_id);

-- Recipes RLS: Users can see their own recipes OR global recipes
CREATE POLICY recipes_view_policy ON recipes
  FOR SELECT USING (auth.uid() = user_id OR is_global = true);

CREATE POLICY recipes_insert_own ON recipes
  FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY recipes_update_own ON recipes
  FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY recipes_delete_own ON recipes
  FOR DELETE USING (auth.uid() = user_id);

-- Recipe Tags RLS: Users can only manage tags for their own recipes
CREATE POLICY recipe_tags_own_data ON recipe_tags
  FOR SELECT USING (
    EXISTS (
      SELECT 1 FROM recipes WHERE recipes.id = recipe_tags.recipe_id AND recipes.user_id = auth.uid()
    )
  );

CREATE POLICY recipe_tags_insert_own ON recipe_tags
  FOR INSERT WITH CHECK (
    EXISTS (
      SELECT 1 FROM recipes WHERE recipes.id = recipe_id AND recipes.user_id = auth.uid()
    )
  );

CREATE POLICY recipe_tags_delete_own ON recipe_tags
  FOR DELETE USING (
    EXISTS (
      SELECT 1 FROM recipes WHERE recipes.id = recipe_id AND recipes.user_id = auth.uid()
    )
  );

-- Recipe Collections RLS: Users can only manage collections for their own recipes
CREATE POLICY recipe_collections_own_data ON recipe_collections
  FOR SELECT USING (
    EXISTS (
      SELECT 1 FROM recipes WHERE recipes.id = recipe_collections.recipe_id AND recipes.user_id = auth.uid()
    )
  );

CREATE POLICY recipe_collections_insert_own ON recipe_collections
  FOR INSERT WITH CHECK (
    EXISTS (
      SELECT 1 FROM recipes WHERE recipes.id = recipe_id AND recipes.user_id = auth.uid()
    )
  );

CREATE POLICY recipe_collections_delete_own ON recipe_collections
  FOR DELETE USING (
    EXISTS (
      SELECT 1 FROM recipes WHERE recipes.id = recipe_id AND recipes.user_id = auth.uid()
    )
  );

-- Create indexes for better performance
CREATE INDEX idx_recipes_user_id ON recipes(user_id);
CREATE INDEX idx_collections_user_id ON collections(user_id);
CREATE INDEX idx_tags_user_id ON tags(user_id);
CREATE INDEX idx_recipe_tags_recipe_id ON recipe_tags(recipe_id);
CREATE INDEX idx_recipe_tags_tag_id ON recipe_tags(tag_id);
CREATE INDEX idx_recipe_collections_recipe_id ON recipe_collections(recipe_id);
CREATE INDEX idx_recipe_collections_collection_id ON recipe_collections(collection_id);

-- Insert sample tags (these will be created per-user in the app)
-- This is just for reference - in production, users create their own tags
