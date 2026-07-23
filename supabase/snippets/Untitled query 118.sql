-- 1. Bucket erstellen (oder sicherstellen, dass er öffentlich ist)
INSERT INTO storage.buckets (id, name, public)
VALUES ('marker-images', 'marker-images', true)
ON CONFLICT (id) DO UPDATE SET public = true;

-- Alte Storage-Policies aufräumen
DROP POLICY IF EXISTS "Public Read Access" ON storage.objects;
DROP POLICY IF EXISTS "Authenticated Upload" ON storage.objects;
DROP POLICY IF EXISTS "Owner Delete" ON storage.objects;

-- 2. Policy: Jeder (oder authentifizierte User) darf Bilder LESEN
CREATE POLICY "Public Read Access" ON storage.objects
FOR SELECT TO public
USING (bucket_id = 'marker-images');

-- 3. Policy: Eingeloggte User dürfen Bilder HOCHLADEN
CREATE POLICY "Authenticated Upload" ON storage.objects
FOR INSERT TO authenticated
WITH CHECK (bucket_id = 'marker-images');

-- 4. Policy: Eigentümer darf eigene Bilder LÖSCHEN
CREATE POLICY "Owner Delete" ON storage.objects
FOR DELETE TO authenticated
USING (bucket_id = 'marker-images' AND auth.uid() = owner);