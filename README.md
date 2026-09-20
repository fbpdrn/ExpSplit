# ExpSplit

Progetto di PAC a.a 2025/2026 per la gestione di spese condivise.

## Avvio con Docker

```bash
# Copia il file di esempio e genera i certificati
cp .env.example .env
cd certs && bash generate.sh

# Compila e avvia il backend e il database
docker compose up -d --build
```

Il backend è raggiungibile su `https://localhost:8080`. Lo script dei certificati legge la password da `.env` e, se rieseguito, sostituisce i certificati esistenti.

## Comandi utili

```bash
# Avvia i container senza ricompilare
docker compose up -d

# Arresta e rimuove i container
docker compose down
```

## Documentazione

La documentazione del progetto è disponibile nel formato PDF nella cartella `docs/out`.