#!/bin/bash
set -e

# Configuration
SERVER_IP="191.101.0.217"
SERVER_USER="root"
REMOTE_DIR="/opt/clientportal"
IMAGE_NAME="clientportal"
IMAGE_TAG="latest"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Tax Client Portal - Deployment Script ===${NC}"
echo ""

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo -e "${RED}Error: .env file not found!${NC}"
    echo "Please copy .env.production to .env and fill in the values"
    exit 1
fi

# Load environment variables
source .env

# Verify required variables
if [ "$POSTGRES_PASSWORD" = "CHANGE_THIS_TO_A_SECURE_PASSWORD" ]; then
    echo -e "${RED}Error: Please set a secure POSTGRES_PASSWORD in .env${NC}"
    exit 1
fi

if [ "$JWT_SECRET" = "CHANGE_THIS_TO_A_256_BIT_SECRET_KEY" ]; then
    echo -e "${RED}Error: Please set a secure JWT_SECRET in .env${NC}"
    exit 1
fi

echo -e "${YELLOW}[1/6] Building Docker image...${NC}"
cd ..
docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
cd deploy

echo -e "${YELLOW}[2/6] Saving Docker image...${NC}"
docker save ${IMAGE_NAME}:${IMAGE_TAG} | gzip > ${IMAGE_NAME}.tar.gz

echo -e "${YELLOW}[3/6] Transferring files to server...${NC}"
scp ${IMAGE_NAME}.tar.gz ${SERVER_USER}@${SERVER_IP}:${REMOTE_DIR}/
scp docker-compose.prod.yml ${SERVER_USER}@${SERVER_IP}:${REMOTE_DIR}/docker-compose.yml
scp nginx.conf ${SERVER_USER}@${SERVER_IP}:${REMOTE_DIR}/
scp .env ${SERVER_USER}@${SERVER_IP}:${REMOTE_DIR}/

echo -e "${YELLOW}[4/6] Loading Docker image on server...${NC}"
ssh ${SERVER_USER}@${SERVER_IP} "cd ${REMOTE_DIR} && gunzip -c ${IMAGE_NAME}.tar.gz | docker load"

echo -e "${YELLOW}[5/6] Starting services...${NC}"
ssh ${SERVER_USER}@${SERVER_IP} "cd ${REMOTE_DIR} && docker compose down --remove-orphans || true"
ssh ${SERVER_USER}@${SERVER_IP} "cd ${REMOTE_DIR} && docker compose up -d db"
echo "Waiting for database to be ready..."
sleep 10
ssh ${SERVER_USER}@${SERVER_IP} "cd ${REMOTE_DIR} && docker compose up -d"

echo -e "${YELLOW}[6/6] Cleaning up...${NC}"
rm -f ${IMAGE_NAME}.tar.gz
ssh ${SERVER_USER}@${SERVER_IP} "rm -f ${REMOTE_DIR}/${IMAGE_NAME}.tar.gz"

echo ""
echo -e "${GREEN}=== Deployment Complete ===${NC}"
echo ""
echo "Application URL: https://${DOMAIN}"
echo ""
echo -e "${YELLOW}Note: If this is the first deployment, you need to obtain an SSL certificate:${NC}"
echo ""
echo "1. First, start nginx without SSL to allow Let's Encrypt verification:"
echo "   ssh ${SERVER_USER}@${SERVER_IP} \"cd ${REMOTE_DIR} && docker compose stop nginx\""
echo ""
echo "2. Create a temporary nginx config for HTTP-only, then run:"
echo "   ssh ${SERVER_USER}@${SERVER_IP} \"cd ${REMOTE_DIR} && docker compose run --rm certbot certonly --webroot -w /var/www/certbot -d ${DOMAIN} --email admin@nanobyte.ca --agree-tos --no-eff-email\""
echo ""
echo "3. Restart nginx with SSL:"
echo "   ssh ${SERVER_USER}@${SERVER_IP} \"cd ${REMOTE_DIR} && docker compose up -d nginx\""
echo ""
echo "To check logs:"
echo "   ssh ${SERVER_USER}@${SERVER_IP} \"cd ${REMOTE_DIR} && docker compose logs -f\""
