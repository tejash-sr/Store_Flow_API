#!/bin/bash

# DuckDNS Update Script
# Updates storeflowapi.duckdns.org to point to current public IP
# Usage: DUCKDNS_TOKEN=<your-token> ./update-duckdns.sh

DOMAIN="storeflowapi"
TOKEN="${DUCKDNS_TOKEN:?Set DUCKDNS_TOKEN in the environment before running}"
UPDATE_URL="https://www.duckdns.org/update?domains=${DOMAIN}&token=${TOKEN}&ip="

# Get current public IP
PUBLIC_IP=$(curl -s https://api.ipify.org)

# Update DuckDNS
RESPONSE=$(curl -s "${UPDATE_URL}${PUBLIC_IP}")

echo "[$(date)] DuckDNS Updated: ${DOMAIN}.duckdns.org -> ${PUBLIC_IP} (Response: ${RESPONSE})"
