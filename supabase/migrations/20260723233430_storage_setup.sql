-- Storage Bucket anlegen
INSERT INTO storage.buckets (id, name, public)
VALUES ('marker-images', 'marker-images', true)
ON CONFLICT (id) DO UPDATE SET public = true;

-- Storage Policies
CREATE POLICY "Public Read Access" ON storage.objects FOR SELECT TO public USING (bucket_id = 'marker-images');
CREATE POLICY "Authenticated Upload" ON storage.objects FOR INSERT TO authenticated WITH CHECK (bucket_id = 'marker-images');
CREATE POLICY "Owner Delete" ON storage.objects FOR DELETE TO authenticated USING (bucket_id = 'marker-images' AND auth.uid() = owner);