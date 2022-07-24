CREATE TABLE IF NOT EXISTS public.recipestep
(
    id bigint NOT NULL,
    recipe_id bigint NOT NULL,
    step_order integer,
    title VARCHAR(45),
    instructions TEXT,
    image bytea,
    time_minutes integer,
    CONSTRAINT recipestep_pkey PRIMARY KEY (id),
    CONSTRAINT fk_recipestep_ugc_id FOREIGN KEY (id)
        REFERENCES public.ugc (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_recipestep_recipe_id FOREIGN KEY (recipe_id)
        REFERENCES public.recipe (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
)

TABLESPACE pg_default;

CREATE TABLE IF NOT EXISTS public.recipestep_aud
(
    id bigint NOT NULL,
    rev integer NOT NULL,
    recipe_id bigint NOT NULL,
    step_order integer,
    title VARCHAR(45),
    instructions TEXT,
    image bytea,
    time_minutes integer,
    CONSTRAINT recipestep_aud_pkey PRIMARY KEY (id, rev),
    CONSTRAINT fk_recipestep_ugc_aud_id FOREIGN KEY (id, rev)
        REFERENCES public.ugc_aud (id, rev) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

CREATE TABLE IF NOT EXISTS public.recipeingredient
(
    id bigint NOT NULL,
    recipe_id bigint NOT NULL,
    name VARCHAR(255) NOT NULL,
    measurement VARCHAR(45),
    CONSTRAINT recipeingredient_pkey PRIMARY KEY (id),
    CONSTRAINT fk_recipeingredient_ugc_id FOREIGN KEY (id)
        REFERENCES public.ugc (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_recipeingredient_recipe_id FOREIGN KEY (recipe_id)
        REFERENCES public.recipe (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
)

TABLESPACE pg_default;

CREATE TABLE IF NOT EXISTS public.recipeingredient_aud
(
    id bigint NOT NULL,
    rev integer NOT NULL,
    recipe_id bigint NOT NULL,
    name VARCHAR(255) NOT NULL,
    measurement VARCHAR(45),
    CONSTRAINT recipeingredient_aud_pkey PRIMARY KEY (id, rev),
    CONSTRAINT fk_recipeingredient_ugc_aud_id FOREIGN KEY (id, rev)
        REFERENCES public.ugc_aud (id, rev) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE public.recipe 
    DROP directions,
    ADD title VARCHAR(255),
    ADD description text,
    ADD image bytea,
    ADD published BOOLEAN;


ALTER TABLE public.recipe_aud
    DROP directions,
    ADD title VARCHAR(255),
    ADD description text,
    ADD image bytea,
    ADD published BOOLEAN;
