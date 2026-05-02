-- ════════════════════════════════════════════════════════════════
-- NomNom Database Schema
-- Run this in Supabase SQL Editor to create all required tables
-- ════════════════════════════════════════════════════════════════

-- Enable UUID generation
create extension if not exists "uuid-ossp";

-- ── 1. Recipes ───────────────────────────────────────────────────────
create table if not exists public.recipes (
    id               uuid default uuid_generate_v4() primary key,
    user_id          uuid references auth.users(id) on delete cascade not null,
    title            text not null,
    ingredients      text[] not null default '{}',
    steps            text[] not null default '{}',
    source_url       text,
    source_type      text,
    prep_time_minutes int,
    cook_time_minutes int,
    servings         int,
    notes            text,
    created_at       timestamptz default now(),
    updated_at       timestamptz default now()
);

-- ── 2. Collections ───────────────────────────────────────────────────
create table if not exists public.collections (
    id          uuid default uuid_generate_v4() primary key,
    user_id     uuid references auth.users(id) on delete cascade not null,
    name        text not null,
    description text,
    created_at  timestamptz default now()
);

-- ── 3. Recipe ↔ Collection join table ────────────────────────────────
create table if not exists public.recipe_collections (
    recipe_id     uuid references public.recipes(id) on delete cascade,
    collection_id uuid references public.collections(id) on delete cascade,
    primary key (recipe_id, collection_id)
);

-- ── Enable Row Level Security ────────────────────────────────────────
alter table public.recipes          enable row level security;
alter table public.collections      enable row level security;
alter table public.recipe_collections enable row level security;

-- ── RLS Policies: Recipes ────────────────────────────────────────────
create policy "recipes: select own"
    on public.recipes for select
    using (auth.uid() = user_id);

create policy "recipes: insert own"
    on public.recipes for insert
    with check (auth.uid() = user_id);

create policy "recipes: update own"
    on public.recipes for update
    using (auth.uid() = user_id);

create policy "recipes: delete own"
    on public.recipes for delete
    using (auth.uid() = user_id);

-- ── RLS Policies: Collections ────────────────────────────────────────
create policy "collections: select own"
    on public.collections for select
    using (auth.uid() = user_id);

create policy "collections: insert own"
    on public.collections for insert
    with check (auth.uid() = user_id);

create policy "collections: delete own"
    on public.collections for delete
    using (auth.uid() = user_id);

-- ── RLS Policies: Recipe-Collection links ────────────────────────────
create policy "recipe_collections: select own"
    on public.recipe_collections for select
    using (
        exists (
            select 1 from public.recipes r
            where r.id = recipe_id and r.user_id = auth.uid()
        )
    );

create policy "recipe_collections: insert own"
    on public.recipe_collections for insert
    with check (
        exists (
            select 1 from public.recipes r
            where r.id = recipe_id and r.user_id = auth.uid()
        )
    );

create policy "recipe_collections: delete own"
    on public.recipe_collections for delete
    using (
        exists (
            select 1 from public.recipes r
            where r.id = recipe_id and r.user_id = auth.uid()
        )
    );

-- ── Auto-update updated_at on recipe edits ───────────────────────────
create or replace function public.handle_updated_at()
returns trigger as $$
begin
    new.updated_at = now();
    return new;
end;
$$ language plpgsql;

create trigger recipes_updated_at
    before update on public.recipes
    for each row execute procedure public.handle_updated_at();
