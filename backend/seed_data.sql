-- NomNom Seed Data Script (FINAL VERSION)
-- Prepared for User: 78133ba3-e4df-4593-a26b-ef84f283b8c8

INSERT INTO recipes (
    user_id, 
    title, 
    ingredients, 
    steps, 
    prep_time_minutes, 
    cook_time_minutes, 
    servings, 
    tags, 
    image_url, 
    notes,
    is_global
) VALUES 
(
    '78133ba3-e4df-4593-a26b-ef84f283b8c8', 
    'Avocado Toast with Poached Egg', 
    ARRAY['2 slices sourdough bread', '1 ripe avocado', '2 eggs', '1 tsp red pepper flakes', 'Salt and pepper', 'Microgreens for garnish'], 
    ARRAY['Toast the bread until golden brown.', 'Mash the avocado in a small bowl with salt, pepper, and a squeeze of lemon.', 'Bring a pot of water to a gentle simmer and poach the eggs for 3-4 minutes.', 'Spread avocado on toast, top with a poached egg, and sprinkle with red pepper flakes and microgreens.'], 
    10, 5, 2, 
    ARRAY['Quick', 'Healthy', 'Breakfast', 'Main Course'], 
    'https://images.unsplash.com/photo-1525351484163-7529414344d8?auto=format&fit=crop&q=80&w=800', 
    'A classic, high-protein breakfast that looks as good as it tastes.',
    true
),
(
    '78133ba3-e4df-4593-a26b-ef84f283b8c8', 
    'Butter Chicken (Murgh Makhani)', 
    ARRAY['500g Chicken thighs', '1 cup Tomato puree', '100ml Heavy cream', '2 tbsp Butter', '1 tbsp Ginger-garlic paste', '1 tsp Garam masala', '1 tsp Kasuri methi'], 
    ARRAY['Marinate chicken in yogurt and spices.', 'Grill or pan-fry until cooked.', 'Prepare gravy with tomatoes, butter, and spices.', 'Add chicken and finish with cream and dried fenugreek leaves.'], 
    20, 30, 4, 
    ARRAY['Indian', 'Main Course', 'Curry'], 
    'https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?auto=format&fit=crop&q=80&w=800', 
    'Rich, creamy, and mildly spicy - a worldwide favorite.',
    true
),
(
    '78133ba3-e4df-4593-a26b-ef84f283b8c8', 
    'One-Pot Creamy Mushroom Pasta', 
    ARRAY['250g Fettuccine', '200g Sliced mushrooms', '2 cloves Garlic', '1 cup Vegetable broth', '1/2 cup Heavy cream', 'Parmesan cheese'], 
    ARRAY['In a large pot, sauté garlic and mushrooms.', 'Add dry pasta, broth, and cream.', 'Simmer until pasta is cooked and sauce is thickened.', 'Top with plenty of parmesan.'], 
    5, 15, 2, 
    ARRAY['One Pot Meal', 'Vegetarian', 'Quick', 'Main Course'], 
    'https://images.unsplash.com/photo-1473093226795-af9932fe5856?auto=format&fit=crop&q=80&w=800', 
    'Minimal cleanup, maximum flavor.',
    true
),
(
    '78133ba3-e4df-4593-a26b-ef84f283b8c8', 
    'Bruschetta with Tomato & Basil', 
    ARRAY['Baguette slices', '3 ripe tomatoes', 'Fresh basil', 'Garlic cloves', 'Balsamic glaze', 'Olive oil'], 
    ARRAY['Toast bread slices until crispy.', 'Dice tomatoes and mix with chopped basil, olive oil, and minced garlic.', 'Spoon mixture onto bread.', 'Drizzle with balsamic glaze.'], 
    10, 0, 4, 
    ARRAY['Continental', 'Starter', 'Vegetarian', 'Quick'], 
    'https://images.unsplash.com/photo-1572656631137-7935297eff55?auto=format&fit=crop&q=80&w=800', 
    'A perfect summer appetizer.',
    true
),
(
    '78133ba3-e4df-4593-a26b-ef84f283b8c8', 
    'Paneer Tikka', 
    ARRAY['200g Paneer cubes', '1 Bell pepper', '1 Red onion', '1/2 cup Greek yogurt', '1 tbsp Tandoori masala', 'Lemon juice'], 
    ARRAY['Marinate paneer and veg in yogurt and spices for 30 mins.', 'Skewer them alternately.', 'Grill or bake until edges are charred.', 'Serve hot with mint chutney.'], 
    35, 15, 2, 
    ARRAY['Indian', 'Starter', 'Vegetarian', 'Healthy'], 
    'https://images.unsplash.com/photo-1567188040759-fb8a883dc6d8?auto=format&fit=crop&q=80&w=800', 
    'Smoky and flavorful cottage cheese skewers.',
    true
),
(
    '78133ba3-e4df-4593-a26b-ef84f283b8c8', 
    'Classic Italian Carbonara', 
    ARRAY['200g Spaghetti', '100g Guanciale or pancetta', '2 large eggs', '50g Pecorino Romano', '50g Parmesan', 'Black pepper'], 
    ARRAY['Boil pasta in salted water until al dente.', 'While pasta cooks, fry guanciale until crispy.', 'Whisk eggs and grated cheese in a bowl.', 'Drain pasta, reserving some water. Toss pasta with guanciale, remove from heat, and quickly stir in egg mixture and pasta water until creamy.'], 
    10, 15, 2, 
    ARRAY['Dinner', 'Comfort Food', 'Italian', 'Main Course'], 
    'https://images.unsplash.com/photo-1612874742237-6526221588e3?auto=format&fit=crop&q=80&w=800', 
    'Authentic Roman carbonara. No cream allowed!',
    true
),
(
    '78133ba3-e4df-4593-a26b-ef84f283b8c8', 
    'Berry Bliss Smoothie Bowl', 
    ARRAY['1 cup frozen mixed berries', '1 frozen banana', '1/2 cup almond milk', '1 tbsp honey', 'Granola', 'Fresh strawberries', 'Chia seeds'], 
    ARRAY['Blend frozen berries, banana, milk, and honey until thick and smooth.', 'Pour into a bowl.', 'Top with rows of granola, sliced strawberries, and chia seeds.', 'Serve immediately before it melts!'], 
    5, 0, 1, 
    ARRAY['Healthy', 'Dessert', 'Vegetarian', 'Starter'], 
    'https://images.unsplash.com/photo-1590301157890-4810ed352733?auto=format&fit=crop&q=80&w=800', 
    'A vibrant and refreshing way to start your day.',
    true
),
(
    '78133ba3-e4df-4593-a26b-ef84f283b8c8', 
    'Honey Garlic Glazed Salmon', 
    ARRAY['2 salmon fillets', '3 tbsp honey', '2 tbsp soy sauce', '1 tbsp lemon juice', '3 cloves garlic, minced', 'Roasted asparagus'], 
    ARRAY['Whisk honey, soy sauce, lemon juice, and garlic.', 'Season salmon with salt and pepper.', 'Sear salmon in a pan over medium-high heat for 5 minutes per side.', 'Pour in glaze and cook until thickened. Serve over roasted asparagus.'], 
    10, 15, 2, 
    ARRAY['Healthy', 'High Protein', 'Quick', 'Main Course'], 
    'https://images.unsplash.com/photo-1467003909585-2f8a72700288?auto=format&fit=crop&q=80&w=800', 
    'Sweet, savory, and ready in under 30 minutes.',
    true
),
(
    '78133ba3-e4df-4593-a26b-ef84f283b8c8', 
    'Authentic Shakshuka', 
    ARRAY['1 onion, diced', '1 bell pepper, diced', '3 cloves garlic', '1 can crushed tomatoes', '1 tsp cumin', '4 eggs', 'Fresh cilantro', 'Crumbled feta'], 
    ARRAY['Sauté onion and pepper until soft. Add garlic and spices.', 'Pour in tomatoes and simmer for 10 minutes until thickened.', 'Make 4 wells in the sauce and crack an egg into each.', 'Cover and cook for 5-8 minutes until whites are set but yolks are runny. Garnish with feta and cilantro.'], 
    15, 20, 2, 
    ARRAY['Breakfast', 'Dinner', 'Vegetarian', 'Main Course'], 
    'https://images.unsplash.com/photo-1590412200988-a436970781fa?auto=format&fit=crop&q=80&w=800', 
    'The ultimate brunch dish. Serve with crusty bread for dipping.',
    true
),
(
    '78133ba3-e4df-4593-a26b-ef84f283b8c8', 
    'Thai Green Curry', 
    ARRAY['400ml Coconut milk', '2 tbsp Green curry paste', '300g Chicken breast', '1 Bamboo shoot', 'Thai basil', 'Fish sauce', 'Palm sugar'], 
    ARRAY['Fry curry paste in a bit of oil until fragrant.', 'Add chicken and cook until no longer pink.', 'Pour in coconut milk and bring to a simmer.', 'Add bamboo shoots and season with fish sauce and sugar. Garnish with basil.'], 
    15, 15, 3, 
    ARRAY['Asian', 'Spicy', 'Dinner', 'Thai', 'Main Course'], 
    'https://images.unsplash.com/photo-1455619452474-d2be8b1e70cd?auto=format&fit=crop&q=80&w=800', 
    'A fragrant and spicy Thai classic.',
    true
),
(
    '78133ba3-e4df-4593-a26b-ef84f283b8c8', 
    'Rainbow Buddha Bowl', 
    ARRAY['Quinoa', 'Chickpeas', 'Sweet potato', 'Kale', 'Tahini', 'Lemon', 'Avocado'], 
    ARRAY['Roast sweet potato and chickpeas.', 'Cook quinoa according to package instructions.', 'Massage kale with lemon juice.', 'Assemble bowl with quinoa, veg, and top with tahini dressing.'], 
    15, 25, 1, 
    ARRAY['Vegan', 'Healthy', 'Gluten-Free', 'Lunch', 'Main Course'], 
    'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&q=80&w=800', 
    'A colorful and nutrient-packed vegan meal.',
    true
),
(
    '78133ba3-e4df-4593-a26b-ef84f283b8c8', 
    'Truffle Mushroom Risotto', 
    ARRAY['Arborio rice', 'Mixed mushrooms', 'Shallots', 'White wine', 'Parmesan', 'Truffle oil', 'Vegetable stock'], 
    ARRAY['Sauté mushrooms and shallots.', 'Add rice and toast slightly.', 'Deglaze with white wine.', 'Slowly add stock one ladle at a time until rice is creamy. Stir in cheese and truffle oil.'], 
    10, 30, 2, 
    ARRAY['Gourmet', 'Italian', 'Vegetarian', 'Comfort Food', 'Main Course'], 
    'https://images.unsplash.com/photo-1476124369491-e7addf5db371?auto=format&fit=crop&q=80&w=800', 
    'Indulgent and earthy risotto for special occasions.',
    true
),
(
    '78133ba3-e4df-4593-a26b-ef84f283b8c8', 
    'Spicy Ahi Tuna Tacos', 
    ARRAY['Fresh tuna', 'Corn tortillas', 'Sriracha mayo', 'Slaw mix', 'Lime', 'Cilantro'], 
    ARRAY['Sear tuna lightly on all sides.', 'Slice tuna into strips.', 'Warm tortillas.', 'Assemble with slaw, tuna, and spicy mayo.'], 
    15, 5, 2, 
    ARRAY['Seafood', 'Quick', 'Fusion', 'Spicy', 'Starter'], 
    'https://images.unsplash.com/photo-1512838243191-e81e8f66f1fd?auto=format&fit=crop&q=80&w=800', 
    'Fresh and zesty tacos with a kick.',
    true
),
(
    '78133ba3-e4df-4593-a26b-ef84f283b8c8', 
    'Classic Beef Burger', 
    ARRAY['Ground beef', 'Brioche bun', 'Cheddar cheese', 'Lettuce', 'Tomato', 'Pickles', 'NomNom Sauce'], 
    ARRAY['Shape beef into patties and grill.', 'Melt cheese on top.', 'Toast buns.', 'Assemble with toppings and sauce.'], 
    10, 10, 2, 
    ARRAY['Dinner', 'Comfort Food', 'Classic', 'Main Course'], 
    'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&q=80&w=800', 
    'The ultimate juicy burger.',
    true
);
