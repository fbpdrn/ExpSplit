#!/bin/bash
# Eseguito automaticamente da Postgres al primo avvio.
# Crea i ruoli usati dai servizi auth e expsplit (diversi dal superuser)
# sullo stesso database "expsplit" ma senza visibilità reciproca sulle tabelle.
#
# Le password sono lette dalle variabili d'ambiente (AUTH_DB_PASSWORD eEXPSPLIT_DB_PASSWORD).

set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE ROLE expsplit_auth WITH LOGIN PASSWORD '${AUTH_DB_PASSWORD}';
    GRANT CREATE, USAGE ON SCHEMA public TO expsplit_auth;

    CREATE ROLE expsplit_app WITH LOGIN PASSWORD '${EXPSPLIT_DB_PASSWORD}';
    GRANT CREATE, USAGE ON SCHEMA public TO expsplit_app;
EOSQL
