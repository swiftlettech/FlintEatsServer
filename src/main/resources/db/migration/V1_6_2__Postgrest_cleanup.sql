DROP POLICY IF EXISTS "Anyone can SELECT" ON public.authenticationrecord_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.badge_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.comment_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.deal_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.entity_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.entity_tag_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.food_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.foodpantrysite_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.foodproperty_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.market_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.policy_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.preference_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.reaction_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.recipe_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.recipeingredient_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.recipestep_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.review_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.reviewproperty_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.role_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.tag_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.tip_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.ugc_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.usr_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.usr_badge_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.usr_role_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.usr_usr_aud;
DROP POLICY IF EXISTS "Anyone can SELECT" ON public.viewing_aud;


REVOKE ALL ON TABLE public.usr FROM web_anon, api_user;
GRANT ALL (
    avatar,
    background,
    firstname,
    gison,
    lastname,
    phone,
    username,
    id,
    image_path
) ON TABLE public.usr TO web_anon, api_user;

-- Tell PostgREST servers to reload their schema caches
NOTIFY pgrst, 'reload schema'