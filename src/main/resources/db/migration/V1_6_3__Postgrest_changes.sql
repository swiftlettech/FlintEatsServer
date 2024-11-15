ALTER POLICY "Anyone can SELECT" ON public.recipe
    USING (published);

ALTER POLICY "Anyone can SELECT" ON public.recipe
    RENAME TO "Anyone can SELECT published";


ALTER POLICY "Anyone can SELECT" ON public.reaction
    USING (endtime IS NULL);

ALTER POLICY "Anyone can SELECT" ON public.reaction
    RENAME TO "Anyone can SELECT active";


ALTER POLICY "Anyone can SELECT" ON public.deal
    USING (enddate IS NULL OR enddate > CURRENT_TIMESTAMP);

ALTER POLICY "Anyone can SELECT" ON public.deal
    RENAME TO "Anyone can SELECT current";


CREATE OR REPLACE FUNCTION public.ugc_tags(
	ugc)
    RETURNS bigint[]
    LANGUAGE 'sql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
SELECT array_agg(entity_tag.tags_id)
FROM public.ugc
LEFT JOIN public.entity
	ON entity.id = ugc.id
LEFT JOIN public.entity_tag
	ON entity_tag.targets_id = entity.id
WHERE ugc.id = $1.id
$BODY$;

ALTER TABLE public.deal
    ADD COLUMN search_col tsvector
        GENERATED ALWAYS AS (to_tsvector('english', coalesce(text, '') || ' ' || coalesce(title, ''))) STORED;
CREATE INDEX deal_textsearch_idx ON public.deal USING GIN (search_col);

ALTER TABLE public.tip
    ADD COLUMN search_col tsvector
        GENERATED ALWAYS AS (to_tsvector('english', coalesce(text, ''))) STORED;
CREATE INDEX tip_textsearch_idx ON public.tip USING GIN (search_col);

ALTER TABLE public.review
    ADD COLUMN search_col tsvector
        GENERATED ALWAYS AS (to_tsvector('english', coalesce(text, ''))) STORED;
CREATE INDEX review_textsearch_idx ON public.review USING GIN (search_col);

ALTER TABLE public.recipe
    ADD COLUMN search_col tsvector
        GENERATED ALWAYS AS (to_tsvector('english', coalesce(title, '') || ' ' || coalesce(description, ''))) STORED;
CREATE INDEX recipe_textsearch_idx ON public.recipe USING GIN (search_col);

-- Tell PostgREST servers to reload their schema caches
NOTIFY pgrst, 'reload schema'