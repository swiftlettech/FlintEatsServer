CREATE OR REPLACE FUNCTION public.get_entity_type(
	entity_id bigint)
    RETURNS character varying
    LANGUAGE 'sql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
SELECT CASE
	WHEN D.id = entity_id THEN 'Deal'
	WHEN T.id = entity_id THEN 'Tip'
	WHEN Rc.id = entity_id THEN 'Recipe'
	WHEN Rv.id = entity_id THEN 'Review'
	WHEN Mk.id = entity_id THEN 'Market'
	WHEN Fp.id = entity_id THEN 'FoodPantry'
	ELSE 'Other'
END
FROM public.entity as E
LEFT JOIN public.deal as D
ON D.id = E.id
LEFT JOIN public.tip as T
ON T.id = E.id
LEFT JOIN public.recipe as Rc
ON Rc.Id = E.id
LEFT JOIN public.review as Rv
ON Rv.id = E.id
LEFT JOIN public.market as Mk
ON Mk.id = E.id
LEFT JOIN public.foodpantrysite as Fp
ON Fp.id = E.id
WHERE
E.id = entity_id
$BODY$;