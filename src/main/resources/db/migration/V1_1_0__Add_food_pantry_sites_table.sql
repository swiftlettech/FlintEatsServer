CREATE TABLE IF NOT EXISTS public.foodpantrysite
(
    address character varying(255) COLLATE pg_catalog."default",
    schedule text COLLATE pg_catalog."default",
    notes text COLLATE pg_catalog."default",
    lat double precision,
    lng double precision,
    name character varying(255) COLLATE pg_catalog."default",
    phone character varying(255) COLLATE pg_catalog."default",
    id bigint NOT NULL,
    CONSTRAINT foodpantrysite_pkey PRIMARY KEY (id),
    CONSTRAINT fk_foodpantrysite_entity_id FOREIGN KEY (id)
        REFERENCES public.entity (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

CREATE TABLE IF NOT EXISTS public.foodpantrysite_aud
(
    id bigint NOT NULL,
    rev integer NOT NULL,
    address character varying(255) COLLATE pg_catalog."default",
    schedule text COLLATE pg_catalog."default",
    notes text COLLATE pg_catalog."default",
    lat double precision,
    lng double precision,
    name character varying(255) COLLATE pg_catalog."default",
    phone character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT foodpantrysite_aud_pkey PRIMARY KEY (id, rev),
    CONSTRAINT fk_foodpantrysite_entity_aud_id FOREIGN KEY (id, rev)
        REFERENCES public.entity_aud (id, rev) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;
