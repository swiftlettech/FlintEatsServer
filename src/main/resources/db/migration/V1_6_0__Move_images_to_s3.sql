ALTER TABLE IF EXISTS public.deal
	ADD COLUMN image_path text;
ALTER TABLE IF EXISTS public.deal_aud
	ADD COLUMN image_path text;

ALTER TABLE IF EXISTS public.market
	ADD COLUMN image_path text;
ALTER TABLE IF EXISTS public.market_aud
	ADD COLUMN image_path text;

ALTER TABLE IF EXISTS public.recipe
	ADD COLUMN image_path text;
ALTER TABLE IF EXISTS public.recipe_aud
	ADD COLUMN image_path text;

ALTER TABLE IF EXISTS public.recipestep
	ADD COLUMN image_path text;
ALTER TABLE IF EXISTS public.recipestep_aud
	ADD COLUMN image_path text;

ALTER TABLE IF EXISTS public.tip
	ADD COLUMN image_path text;
ALTER TABLE IF EXISTS public.tip_aud
	ADD COLUMN image_path text;

ALTER TABLE IF EXISTS public.usr
	ADD COLUMN image_path text;
ALTER TABLE IF EXISTS public.usr_aud
	ADD COLUMN image_path text;

-- Tell PostgREST servers to reload their schema caches
NOTIFY pgrst, 'reload schema'