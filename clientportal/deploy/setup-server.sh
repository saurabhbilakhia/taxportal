#!/bin/bash
set -e

echo "=== Tax Client Portal - Server Setup ==="
echo "This script will install Docker, configure firewall, and prepare the server"
echo ""

# Update system
echo "[1/6] Updating system packages..."
apt-get update
apt-get upgrade -y

# Install required packages
echo "[2/6] Installing required packages..."
apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release \
    ufw

# Install Docker
echo "[3/6] Installing Docker..."
if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com -o get-docker.sh
    sh get-docker.sh
    rm get-docker.sh

    # Start and enable Docker
    systemctl start docker
    systemctl enable docker
else
    echo "Docker already installed"
fi

# Install Docker Compose plugin
echo "[4/6] Installing Docker Compose..."
if ! docker compose version &> /dev/null; then
    apt-get install -y docker-compose-plugin
else
    echo "Docker Compose already installed"
fi

# Configure firewall
echo "[5/6] Configuring firewall..."
ufw --force reset
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp    # SSH
ufw allow 80/tcp    # HTTP
ufw allow 443/tcp   # HTTPS
ufw --force enable

# Create application directory
echo "[6/6] Creating application directory..."
mkdir -p /opt/clientportal
mkdir -p /opt/clientportal/uploads
mkdir -p /opt/clientportal/certbot/conf
mkdir -p /opt/clientportal/certbot/www

# Set permissions
chmod 755 /opt/clientportal

echo ""
echo "=== Server Setup Complete ==="
echo "Docker version: $(docker --version)"
echo "Docker Compose version: $(docker compose version)"
echo ""
echo "Next steps:"
echo "1. Upload deployment files to /opt/clientportal"
echo "2. Configure .env file with production values"
echo "3. Run: docker compose up -d"
