-- Spike iteración 0: persistencia de rutas guardadas en Supabase (PostgreSQL).
-- Ejecuta este fichero en el SQL Editor de Supabase (Dashboard → SQL Editor → New query).

create table if not exists public.ruta_guardada (
  id          uuid primary key default gen_random_uuid(),
  usuario_id  uuid not null default auth.uid() references auth.users(id) on delete cascade,
  origen      text not null,
  destino     text not null,
  modo        text not null,
  distancia_km double precision not null,
  duracion_min double precision not null,
  creada_en   timestamptz not null default now()
);

-- La seguridad por usuario se hace con Row Level Security.
alter table public.ruta_guardada enable row level security;

drop policy if exists "solo las mias" on public.ruta_guardada;
create policy "solo las mias" on public.ruta_guardada
  for all
  using (auth.uid() = usuario_id)
  with check (auth.uid() = usuario_id);
