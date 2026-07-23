SELECT 
    column_name, 
    data_type, 
    is_identity, 
    identity_generation, 
    generation_expression
FROM information_schema.columns
WHERE table_name = 'markers';