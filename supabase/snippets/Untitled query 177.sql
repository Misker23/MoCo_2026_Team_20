CREATE OR REPLACE FUNCTION public.add_fog_point(new_lat double precision, new_lon double precision)
 RETURNS void
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    UPDATE user_fog
    SET fog_polygon = ST_Multi(ST_Union(
        fog_polygon,
        -- Zieht einen 100 Meter Radius um die neue Koordinate
        ST_Buffer(ST_SetSRID(ST_MakePoint(new_lon, new_lat), 4326)::geography, 100)::geometry
    ))
    WHERE user_id = auth.uid();
END;
$function$;