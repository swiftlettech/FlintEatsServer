ALTER TABLE IF EXISTS public.usr_usr DROP CONSTRAINT IF EXISTS fk55pm40kwwsakhbfdgvybtvrfi;

ALTER TABLE IF EXISTS public.usr_usr
    ADD CONSTRAINT fk55pm40kwwsakhbfdgvybtvrfi FOREIGN KEY (followers_id)
    REFERENCES public.usr (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.usr_usr DROP CONSTRAINT IF EXISTS fkhxq9m05ggrklvste7h777hh3h;

ALTER TABLE IF EXISTS public.usr_usr
    ADD CONSTRAINT fkhxq9m05ggrklvste7h777hh3h FOREIGN KEY (followees_id)
    REFERENCES public.usr (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;