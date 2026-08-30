create extension if not exists "postgis" with schema "public";


  create table "public"."profiles" (
    "id" uuid not null,
    "username" text not null,
    "created_at" timestamp with time zone default now()
      );


alter table "public"."profiles" enable row level security;


  create table "public"."friendships" (
    "user_id" uuid not null,
    "friend_id" uuid not null,
    "status" text default 'pending'::text,
    "created_at" timestamp with time zone default now(),
    "color" text default '#2196F3'::text
      );


alter table "public"."friendships" enable row level security;


  create table "public"."markers" (
    "id" uuid not null default gen_random_uuid(),
    "user_id" uuid not null,
    "position" public.geometry(Point,4326),
    "color" text,
    "image_url" text,
    "description" text,
    "created_at" timestamp with time zone default now(),
    "updated_at" timestamp with time zone default now(),
    "lat" double precision,
    "lon" double precision
      );


alter table "public"."markers" enable row level security;


  create table "public"."shared_markers" (
    "marker_id" uuid not null,
    "friend_user_id" uuid not null
      );


alter table "public"."shared_markers" enable row level security;


  create table "public"."user_fog" (
    "user_id" uuid not null,
    "fog_polygon" public.geometry(MultiPolygon,4326) default public.st_geomfromtext('MULTIPOLYGON EMPTY'::text, 4326)
      );


alter table "public"."user_fog" enable row level security;

CREATE UNIQUE INDEX friendships_pkey ON public.friendships USING btree (user_id, friend_id);

CREATE UNIQUE INDEX markers_pkey ON public.markers USING btree (id);

CREATE UNIQUE INDEX profiles_pkey ON public.profiles USING btree (id);

CREATE UNIQUE INDEX profiles_username_key ON public.profiles USING btree (username);

CREATE UNIQUE INDEX shared_markers_pkey ON public.shared_markers USING btree (marker_id, friend_user_id);

CREATE UNIQUE INDEX user_fog_pkey ON public.user_fog USING btree (user_id);

alter table "public"."friendships" add constraint "friendships_pkey" PRIMARY KEY using index "friendships_pkey";

alter table "public"."markers" add constraint "markers_pkey" PRIMARY KEY using index "markers_pkey";

alter table "public"."profiles" add constraint "profiles_pkey" PRIMARY KEY using index "profiles_pkey";

alter table "public"."shared_markers" add constraint "shared_markers_pkey" PRIMARY KEY using index "shared_markers_pkey";

alter table "public"."user_fog" add constraint "user_fog_pkey" PRIMARY KEY using index "user_fog_pkey";

alter table "public"."profiles" add constraint "profiles_id_fkey" FOREIGN KEY (id) REFERENCES auth.users(id) ON DELETE CASCADE not valid;

alter table "public"."profiles" validate constraint "profiles_id_fkey";

alter table "public"."profiles" add constraint "profiles_username_key" UNIQUE using index "profiles_username_key";

alter table "public"."friendships" add constraint "friendships_user_profile_fkey" FOREIGN KEY (user_id) REFERENCES public.profiles(id) ON DELETE CASCADE not valid;

alter table "public"."friendships" validate constraint "friendships_user_profile_fkey";

alter table "public"."friendships" add constraint "friendships_friend_id_fkey" FOREIGN KEY (friend_id) REFERENCES public.profiles(id) ON DELETE CASCADE not valid;

alter table "public"."friendships" validate constraint "friendships_friend_id_fkey";

alter table "public"."markers" add constraint "markers_user_id_fkey" FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE not valid;

alter table "public"."markers" validate constraint "markers_user_id_fkey";

alter table "public"."shared_markers" add constraint "shared_markers_marker_id_fkey" FOREIGN KEY (marker_id) REFERENCES public.markers(id) ON DELETE CASCADE not valid;

alter table "public"."shared_markers" validate constraint "shared_markers_marker_id_fkey";

alter table "public"."shared_markers" add constraint "shared_markers_friend_user_id_fkey" FOREIGN KEY (friend_user_id) REFERENCES auth.users(id) ON DELETE CASCADE not valid;

alter table "public"."shared_markers" validate constraint "shared_markers_friend_user_id_fkey";

alter table "public"."user_fog" add constraint "user_fog_user_id_fkey" FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE not valid;

alter table "public"."user_fog" validate constraint "user_fog_user_id_fkey";

set check_function_bodies = off;

CREATE OR REPLACE FUNCTION public.sync_marker_position()
 RETURNS trigger
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
BEGIN
    IF NEW.lat IS NOT NULL AND NEW.lon IS NOT NULL THEN
        NEW.position := ST_SetSRID(ST_MakePoint(NEW.lon, NEW.lat), 4326);
    END IF;
    NEW.updated_at := NOW();
    RETURN NEW;
END;
$function$
;

CREATE OR REPLACE FUNCTION public.add_fog_point(new_lat double precision, new_lon double precision)
 RETURNS void
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
DECLARE
    new_area geometry;
BEGIN
    new_area := ST_Buffer(
        ST_SetSRID(ST_MakePoint(new_lon, new_lat), 4326)::geography,
        50
    )::geometry;

    UPDATE public.user_fog
    SET fog_polygon = ST_Multi(
        ST_Union(
            COALESCE(fog_polygon, ST_GeomFromText('MULTIPOLYGON EMPTY', 4326)),
            new_area
        )
    )
    WHERE user_id = auth.uid();
END;
$function$
;

CREATE OR REPLACE FUNCTION public.create_marker(lat double precision, lon double precision, description text, color text, image_url text, user_id uuid)
 RETURNS void
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
BEGIN
  INSERT INTO markers (user_id, position, description, color, image_url)
  VALUES (
    user_id,
    ST_SetSRID(ST_MakePoint(lon, lat), 4326),
    description,
    color,
    image_url
  );
END;
$function$
;

CREATE OR REPLACE FUNCTION public.ensure_user_fog()
 RETURNS void
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
BEGIN
    INSERT INTO public.user_fog (user_id)
    VALUES (auth.uid())
    ON CONFLICT (user_id) DO NOTHING;
END;
$function$
;

CREATE OR REPLACE FUNCTION public.get_user_fog()
 RETURNS json
 LANGUAGE sql
 SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
    SELECT COALESCE(
        ST_AsGeoJSON(
            ST_Difference(
                ST_Buffer(
                    ST_SetSRID(ST_MakePoint(7.5648, 51.0264), 4326)::geography,
                    70000
                )::geometry,
                COALESCE(fog_polygon, ST_GeomFromText('MULTIPOLYGON EMPTY', 4326))
            )
        )::json,
        '{}'::json
    )
    FROM public.user_fog
    WHERE user_id = auth.uid();
$function$
;

CREATE OR REPLACE FUNCTION public.handle_new_user()
 RETURNS trigger
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
begin
  insert into public.profiles (id, username)
  values (
    new.id,
    coalesce(new.raw_user_meta_data->>'username', split_part(new.email, '@', 1))
  );
  return new;
end;
$function$
;

CREATE OR REPLACE FUNCTION public.is_marker_owner(p_marker_id uuid, p_user_id uuid)
 RETURNS boolean
 LANGUAGE sql
 SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
  SELECT EXISTS (
    SELECT 1
    FROM public.markers
    WHERE id = p_marker_id
      AND user_id = p_user_id
  );
$function$
;

CREATE OR REPLACE FUNCTION public.is_marker_shared_with_user(p_marker_id uuid, p_user_id uuid)
 RETURNS boolean
 LANGUAGE sql
 SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
  SELECT EXISTS (
    SELECT 1
    FROM public.shared_markers
    WHERE marker_id = p_marker_id
      AND friend_user_id = p_user_id
  );
$function$
;

grant delete on table "public"."friendships" to "authenticated";

grant insert on table "public"."friendships" to "authenticated";

grant select on table "public"."friendships" to "authenticated";

grant update on table "public"."friendships" to "authenticated";

grant delete on table "public"."markers" to "authenticated";

grant insert on table "public"."markers" to "authenticated";

grant select on table "public"."markers" to "authenticated";

grant update on table "public"."markers" to "authenticated";

grant delete on table "public"."profiles" to "authenticated";

grant insert on table "public"."profiles" to "authenticated";

grant select on table "public"."profiles" to "authenticated";

grant update on table "public"."profiles" to "authenticated";

grant delete on table "public"."shared_markers" to "authenticated";

grant insert on table "public"."shared_markers" to "authenticated";

grant select on table "public"."shared_markers" to "authenticated";

grant update on table "public"."shared_markers" to "authenticated";

grant delete on table "public"."user_fog" to "authenticated";

grant insert on table "public"."user_fog" to "authenticated";

grant select on table "public"."user_fog" to "authenticated";

grant update on table "public"."user_fog" to "authenticated";

grant execute on function public.add_fog_point(double precision, double precision) to "authenticated";

grant execute on function public.ensure_user_fog() to "authenticated";

grant execute on function public.get_user_fog() to "authenticated";


  create policy "friendships_select_policy"
  on "public"."friendships"
  as permissive
  for select
  to authenticated
using (((auth.uid() = user_id) OR (auth.uid() = friend_id)));



  create policy "friendships_insert_policy"
  on "public"."friendships"
  as permissive
  for insert
  to authenticated
with check ((user_id = auth.uid()));



  create policy "friendships_update_policy"
  on "public"."friendships"
  as permissive
  for update
  to authenticated
using (((auth.uid() = user_id) OR (auth.uid() = friend_id)))
with check (((auth.uid() = user_id) OR (auth.uid() = friend_id)));



  create policy "friendships_delete_policy"
  on "public"."friendships"
  as permissive
  for delete
  to authenticated
using (((auth.uid() = user_id) OR (auth.uid() = friend_id)));



  create policy "markers_select_policy"
  on "public"."markers"
  as permissive
  for select
  to authenticated
using (((user_id = auth.uid()) OR public.is_marker_shared_with_user(id, auth.uid())));



  create policy "markers_insert_policy"
  on "public"."markers"
  as permissive
  for insert
  to authenticated
with check ((user_id = auth.uid()));



  create policy "markers_update_policy"
  on "public"."markers"
  as permissive
  for update
  to authenticated
using ((user_id = auth.uid()))
with check ((user_id = auth.uid()));



  create policy "markers_delete_policy"
  on "public"."markers"
  as permissive
  for delete
  to authenticated
using ((user_id = auth.uid()));



  create policy "profiles_select_policy"
  on "public"."profiles"
  as permissive
  for select
  to authenticated
using (true);



  create policy "profiles_update_policy"
  on "public"."profiles"
  as permissive
  for update
  to authenticated
using ((id = auth.uid()));



  create policy "shared_markers_delete_policy"
  on "public"."shared_markers"
  as permissive
  for delete
  to authenticated
using ((public.is_marker_owner(marker_id, auth.uid()) OR (friend_user_id = auth.uid())));



  create policy "shared_markers_insert_policy"
  on "public"."shared_markers"
  as permissive
  for insert
  to authenticated
with check (public.is_marker_owner(marker_id, auth.uid()));



  create policy "shared_markers_select_policy"
  on "public"."shared_markers"
  as permissive
  for select
  to authenticated
using (((friend_user_id = auth.uid()) OR public.is_marker_owner(marker_id, auth.uid())));



  create policy "Nur eigenes Fog aktualisieren"
  on "public"."user_fog"
  as permissive
  for update
  to authenticated
using ((auth.uid() = user_id));



  create policy "Nur eigenes Fog lesen"
  on "public"."user_fog"
  as permissive
  for select
  to authenticated
using ((auth.uid() = user_id));


CREATE TRIGGER on_auth_user_created AFTER INSERT ON auth.users FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

CREATE TRIGGER on_marker_update_sync BEFORE INSERT OR UPDATE ON public.markers FOR EACH ROW EXECUTE FUNCTION public.sync_marker_position();


INSERT INTO storage.buckets (id, name, public)
VALUES ('marker-images', 'marker-images', true)
ON CONFLICT (id) DO UPDATE SET public = true;


  create policy "Public Read Access"
  on "storage"."objects"
  as permissive
  for select
  to public
using ((bucket_id = 'marker-images'::text));



  create policy "Authenticated Upload"
  on "storage"."objects"
  as permissive
  for insert
  to authenticated
with check ((bucket_id = 'marker-images'::text));



  create policy "Authenticated Update"
  on "storage"."objects"
  as permissive
  for update
  to authenticated
using (((bucket_id = 'marker-images'::text) AND (auth.uid() = owner)))
with check (((bucket_id = 'marker-images'::text) AND (auth.uid() = owner)));



  create policy "Owner Delete"
  on "storage"."objects"
  as permissive
  for delete
  to authenticated
using (((bucket_id = 'marker-images'::text) AND (auth.uid() = owner)));