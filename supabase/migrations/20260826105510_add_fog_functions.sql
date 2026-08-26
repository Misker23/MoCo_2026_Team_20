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
$function$;


CREATE OR REPLACE FUNCTION public.get_user_fog()
RETURNS json
LANGUAGE sql
SECURITY DEFINER
SET search_path TO 'public'
AS $function$
    SELECT COALESCE(
        ST_AsGeoJSON(fog_polygon)::json,
        '{}'::json
    )
    FROM public.user_fog
    WHERE user_id = auth.uid();
$function$;


GRANT EXECUTE ON FUNCTION public.add_fog_point(double precision, double precision)
TO authenticated;

GRANT EXECUTE ON FUNCTION public.ensure_user_fog()
TO authenticated;

GRANT EXECUTE ON FUNCTION public.get_user_fog()
TO authenticated;