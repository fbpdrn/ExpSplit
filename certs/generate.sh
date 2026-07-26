#!/bin/bash
# Genera una CA interna auto-firmata e un certificato per ciascun servizio
# (auth, gateway, expsplit), usati per il mTLS tra i servizi
#
# Uso: ./generate.sh

set -euo pipefail

# Directory in cui salvare la CA e i certificati dei servizi
CA_DIR="ca"

# Numero di giorni di validità dei certificati generati
DAYS=3650

# Servizi per i quali generare i certificati
SERVICES=(auth gateway expsplit)

# Carica le variabili d'ambiente dal file .env se esiste
ENV_FILE="../.env"
if [ -f "$ENV_FILE" ]; then
    set -a
    source "$ENV_FILE"
    set +a
fi

# Password per i keystore e truststore
PASSWORD="${CERT_PASSWORD:-expsplit_cert}"

# Si elimina e ricrea la cartella della CA
rm -rf "$CA_DIR"
mkdir -p "$CA_DIR"

# Si genera la CA interna auto-firmata
echo "== Generazione CA interna =="
openssl req -x509 -newkey rsa:4096 -sha256 -days "$DAYS" -nodes -keyout "$CA_DIR/ca-key.pem" -out "$CA_DIR/ca-cert.pem" -subj "/CN=ExpSplit Internal CA"

# Si genera un truststore condiviso contenente solo il certificato della CA
echo "== Truststore condiviso (contiene solo il certificato della CA) =="
TRUSTSTORE="truststore.p12"
rm -f "$TRUSTSTORE"

# Si importa il certificato della CA nel truststore PKCS#12
keytool -importcert -noprompt -alias expsplit-ca -file "$CA_DIR/ca-cert.pem" -keystore "$TRUSTSTORE" -storetype PKCS12 -storepass "$PASSWORD"

# Si genera un certificato per ciascun servizio
for SERVICE in "${SERVICES[@]}"; do
    echo "== Certificato per $SERVICE =="
    SERVICE_DIR="$SERVICE"

    # Si elimina e ricrea la cartella del servizio
    rm -rf "$SERVICE_DIR"
    mkdir -p "$SERVICE_DIR"

    # Si genera una chiave privata e una richiesta di firma del certificato (CSR) per il servizio
    openssl genrsa -out "$SERVICE_DIR/key.pem" 2048
    openssl req -new -key "$SERVICE_DIR/key.pem" -out "$SERVICE_DIR/csr.pem" -subj "/CN=$SERVICE"

    # Si genera un file di estensione per il certificato con i SAN (Subject Alternative Names)
    cat > "$SERVICE_DIR/ext.cnf" <<EOF
subjectAltName = DNS:$SERVICE, DNS:localhost
extendedKeyUsage = serverAuth, clientAuth
EOF

    # Si firma la CSR con la CA interna per generare il certificato del servizio
    openssl x509 -req -in "$SERVICE_DIR/csr.pem" -CA "$CA_DIR/ca-cert.pem" -CAkey "$CA_DIR/ca-key.pem" -CAcreateserial -out "$SERVICE_DIR/cert.pem" -days "$DAYS" -sha256 -extfile "$SERVICE_DIR/ext.cnf"
    # Si crea un keystore PKCS#12 per il servizio contenente la chiave privata e il certificato firmato
    openssl pkcs12 -export -in "$SERVICE_DIR/cert.pem" -inkey "$SERVICE_DIR/key.pem" -certfile "$CA_DIR/ca-cert.pem" -name "$SERVICE" -out "$SERVICE_DIR/keystore.p12" -password "pass:$PASSWORD"

    # Si copia il truststore condiviso nella cartella del servizio
    cp "$TRUSTSTORE" "$SERVICE_DIR/truststore.p12"
    rm -f "$SERVICE_DIR/csr.pem" "$SERVICE_DIR/ext.cnf"
done

echo
echo "Certificati generati"
