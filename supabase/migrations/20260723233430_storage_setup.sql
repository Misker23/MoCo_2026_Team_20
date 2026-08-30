-- Storage Bucket anlegen
INSERT INTO storage.buckets (id, name, public)
VALUES ('marker-images', 'marker-images', true)
ON CONFLICT (id) DO UPDATE SET public = true;

-- Alte Storage Policies aufräumen
DROP POLICY IF EXISTS "Public Read Access" ON storage.objects;
DROP POLICY IF EXISTS "Authenticated Upload" ON storage.objects;
DROP POLICY IF EXISTS "Authenticated Update" ON storage.objects;
DROP POLICY IF EXISTS "Owner Delete" ON storage.objects;

-- 1. Öffentlich lesen
CREATE POLICY "Public Read Access"
ON storage.objects
FOR SELECT
TO public
USING (bucket_id = 'marker-images');

-- 2. Authentifizierte Uploads (Neue Dateien)
CREATE POLICY "Authenticated Upload"
ON storage.objects
FOR INSERT
TO authenticated
WITH CHECK (bucket_id = 'marker-images');

-- 3. Authentifizierte Updates (Notwendig für upsert = true beim Überschreiben)
CREATE POLICY "Authenticated Update"
ON storage.objects
FOR UPDATE
TO authenticated
USING (bucket_id = 'marker-images' AND auth.uid() = owner)
WITH CHECK (bucket_id = 'marker-images' AND auth.uid() = owner);

-- 4. Eigentümer darf löschen
CREATE POLICY "Owner Delete"
ON storage.objects
FOR DELETE
TO authenticated
USING (
    bucket_id = 'marker-images'
    AND auth.uid() = owner
);