-- Fog-of-War:
-- add_fog_point() speichert die bereits entdeckte Fläche.
-- get_user_fog() liefert daraus den noch nicht entdeckten Bereich
-- innerhalb eines 70 km Radius um Gummersbach.

CREATE OR REPLACE FUNCTION public.add_fog_point(
    new_lat double precision,
    new_lon double precision
)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path TO 'public'
AS $function$
DECLARE
    new_area geometry;
BEGIN

    -- 100-Meter-Kreis um die aktuelle Position erzeugen
    new_area := ST_Buffer(
        ST_SetSRID(
            ST_MakePoint(new_lon, new_lat),
            4326
        )::geography,
        50
    )::geometry;

    -- Entdeckte Fläche mit der bisherigen Fläche vereinigen
    UPDATE public.user_fog
    SET fog_polygon = ST_Multi(
        ST_Union(
            COALESCE(
                fog_polygon,
                ST_GeomFromText('MULTIPOLYGON EMPTY', 4326)
            ),
            new_area
        )
    )
    WHERE user_id = auth.uid();

END;
$function$;


CREATE OR REPLACE FUNCTION public.get_user_fog()
RETURNS json
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path TO 'public'
AS $function$
DECLARE
    result_json json;
BEGIN
    -- Stellt sicher, dass die Zeile für den User existiert
    PERFORM public.ensure_user_fog();

    SELECT COALESCE(
        ST_AsGeoJSON(
            ST_Difference(
                ST_Buffer(
                    ST_SetSRID(ST_MakePoint(7.5648, 51.0264), 4326)::geography,
                    70000
                )::geometry,
                COALESCE(
                    fog_polygon,
                    ST_GeomFromText('MULTIPOLYGON EMPTY', 4326)
                )
            )
        )::json,
        '{}'::json
    )
    INTO result_json
    FROM public.user_fog
    WHERE user_id = auth.uid();

    RETURN result_json;
END;
$function$;