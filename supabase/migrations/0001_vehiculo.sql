-- Migración 0001: Creación de la tabla vehiculo (HU11–HU16, HU25)
-- Fuente de verdad: ARQUITECTURA.md
-- RLS obligatoria: auth.uid() = usuario_id
-- ON DELETE CASCADE con auth.users para evitar datos huérfanos (HU04)

CREATE TABLE IF NOT EXISTS vehiculo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    nombre TEXT NOT NULL,
    tipo_energia TEXT NOT NULL CHECK (tipo_energia IN ('COMBUSTIBLE', 'ELECTRICO')),
    consumo NUMERIC(6, 2) NOT NULL CHECK (consumo > 0),
    precio_carburante NUMERIC(6, 4) NULL,
    km_recorridos NUMERIC(10, 2) NOT NULL DEFAULT 0.0 CHECK (km_recorridos >= 0),
    favorito BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Índices para optimizar consultas por usuario y favoritos
CREATE INDEX IF NOT EXISTS idx_vehiculo_usuario_id ON vehiculo(usuario_id);
CREATE INDEX IF NOT EXISTS idx_vehiculo_favorito ON vehiculo(usuario_id, favorito);

-- Seguridad a nivel de fila (RLS)
ALTER TABLE vehiculo ENABLE ROW LEVEL SECURITY;

CREATE POLICY "vehiculo_select_own"
    ON vehiculo FOR SELECT
    USING (auth.uid() = usuario_id);

CREATE POLICY "vehiculo_insert_own"
    ON vehiculo FOR INSERT
    WITH CHECK (auth.uid() = usuario_id);

CREATE POLICY "vehiculo_update_own"
    ON vehiculo FOR UPDATE
    USING (auth.uid() = usuario_id)
    WITH CHECK (auth.uid() = usuario_id);

CREATE POLICY "vehiculo_delete_own"
    ON vehiculo FOR DELETE
    USING (auth.uid() = usuario_id);
