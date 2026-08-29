DROP POLICY IF EXISTS "Eigene Freundschaften lesen" ON public.friendships;
DROP POLICY IF EXISTS "Freundschaften anlegen" ON public.friendships;
DROP POLICY IF EXISTS "Freundschaften lesen" ON public.friendships;
DROP POLICY IF EXISTS "Freundschaften löschen" ON public.friendships;
DROP POLICY IF EXISTS "Nutzer können Freundschaften anlegen" ON public.friendships;
DROP POLICY IF EXISTS "Nutzer können eigene Freundschaften löschen" ON public.friendships;
DROP POLICY IF EXISTS "Nutzer können eigene Freundschaften sehen" ON public.friendships;
DROP POLICY IF EXISTS "friendships_select_policy" ON public.friendships;
DROP POLICY IF EXISTS "friendships_insert_policy" ON public.friendships;
DROP POLICY IF EXISTS "friendships_update_policy" ON public.friendships;
DROP POLICY IF EXISTS "friendships_delete_policy" ON public.friendships;


CREATE POLICY "friendships_select_policy"
ON public.friendships
FOR SELECT
TO authenticated
USING (
    user_id = auth.uid()
    OR friend_id = auth.uid()
);


CREATE POLICY "friendships_insert_policy"
ON public.friendships
FOR INSERT
TO authenticated
WITH CHECK (
    user_id = auth.uid()
);


CREATE POLICY "friendships_update_policy"
ON public.friendships
FOR UPDATE
TO authenticated
USING (
    user_id = auth.uid()
    OR friend_id = auth.uid()
)
WITH CHECK (
    user_id = auth.uid()
    OR friend_id = auth.uid()
);


CREATE POLICY "friendships_delete_policy"
ON public.friendships
FOR DELETE
TO authenticated
USING (
    user_id = auth.uid()
    OR friend_id = auth.uid()
);