#!/bin/bash

# DuckDNS Update Script
# Updates storeflowapi.duckdns.org to point to current public IP

DOMAIN="storeflowapi"
TOKEN="3e45e77c-d879-4bc5-a831-300ac6a8dbfb"
UPDATE_URL="https://www.duckdns.org/update?domains=${DOMAIN}&token=${TOKEN}&ip="

# Get current public IP
PUBLIC_IP=$(curl -s https://api.ipify.org)

# Update DuckDNS
RESPONSE=$(curl -s "${UPDATE_URL}${PUBLIC_IP}")

echo "[$(date)] DuckDNS Updated: ${DOMAIN}.duckdns.org -> ${PUBLIC_IP} (Response: ${RESPONSE})"
