#!/usr/bin/env python3
"""
Tax Client Portal - Remote Deployment Script
Handles SSH connection with password authentication
"""

import paramiko
import os
import sys
import time
from pathlib import Path

# Configuration
SERVER_IP = "191.101.0.217"
SERVER_USER = "root"
SERVER_PASSWORD = "Bilakhia@16031991"
REMOTE_DIR = "/opt/clientportal"

def create_ssh_client():
    """Create and return an SSH client connected to the server."""
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    print(f"Connecting to {SERVER_USER}@{SERVER_IP}...")
    client.connect(
        hostname=SERVER_IP,
        username=SERVER_USER,
        password=SERVER_PASSWORD,
        timeout=30
    )
    print("Connected successfully!")
    return client

def run_command(client, command, show_output=True):
    """Execute a command on the remote server."""
    print(f"Running: {command}")
    stdin, stdout, stderr = client.exec_command(command, timeout=300)

    exit_code = stdout.channel.recv_exit_status()
    output = stdout.read().decode('utf-8', errors='replace')
    error = stderr.read().decode('utf-8', errors='replace')

    if show_output and output:
        # Filter out non-ASCII for Windows console
        print(output.encode('ascii', errors='replace').decode('ascii'))
    if error:
        print(f"STDERR: {error.encode('ascii', errors='replace').decode('ascii')}")

    return exit_code, output, error

def upload_file(client, local_path, remote_path):
    """Upload a file to the remote server."""
    sftp = client.open_sftp()
    print(f"Uploading {local_path} -> {remote_path}")
    sftp.put(local_path, remote_path)
    sftp.close()

def setup_server(client):
    """Run initial server setup."""
    print("\n=== Setting up server ===\n")

    # Read and execute setup script
    setup_script = Path(__file__).parent / "setup-server.sh"
    with open(setup_script, 'r') as f:
        script_content = f.read()

    # Upload and execute setup script
    upload_file(client, str(setup_script), f"{REMOTE_DIR}/setup-server.sh")
    run_command(client, f"chmod +x {REMOTE_DIR}/setup-server.sh")
    run_command(client, f"bash {REMOTE_DIR}/setup-server.sh")

def deploy_application(client):
    """Deploy the application to the server."""
    print("\n=== Deploying application ===\n")

    deploy_dir = Path(__file__).parent
    project_dir = deploy_dir.parent

    # Check if .env exists
    env_file = deploy_dir / ".env"
    if not env_file.exists():
        print("ERROR: .env file not found!")
        print("Please copy .env.production to .env and fill in the values")
        return False

    # Build Docker image locally
    print("[1/5] Building Docker image...")
    os.system(f'cd "{project_dir}" && docker build -t clientportal:latest .')

    # Save Docker image
    print("[2/5] Saving Docker image...")
    image_tar = deploy_dir / "clientportal.tar.gz"
    os.system(f'docker save clientportal:latest | gzip > "{image_tar}"')

    # Upload files
    print("[3/5] Uploading files to server...")
    upload_file(client, str(image_tar), f"{REMOTE_DIR}/clientportal.tar.gz")
    upload_file(client, str(deploy_dir / "docker-compose.prod.yml"), f"{REMOTE_DIR}/docker-compose.yml")
    upload_file(client, str(deploy_dir / "nginx-init.conf"), f"{REMOTE_DIR}/nginx.conf")
    upload_file(client, str(deploy_dir / ".env"), f"{REMOTE_DIR}/.env")

    # Load Docker image on server
    print("[4/5] Loading Docker image on server...")
    run_command(client, f"cd {REMOTE_DIR} && gunzip -c clientportal.tar.gz | docker load")

    # Start services
    print("[5/5] Starting services...")
    run_command(client, f"cd {REMOTE_DIR} && docker compose down --remove-orphans || true")
    run_command(client, f"cd {REMOTE_DIR} && docker compose up -d db")
    print("Waiting for database to be ready...")
    time.sleep(15)
    run_command(client, f"cd {REMOTE_DIR} && docker compose up -d")

    # Cleanup
    print("Cleaning up...")
    os.remove(image_tar)
    run_command(client, f"rm -f {REMOTE_DIR}/clientportal.tar.gz")

    return True

def obtain_ssl_certificate(client):
    """Obtain SSL certificate from Let's Encrypt."""
    print("\n=== Obtaining SSL Certificate ===\n")

    # Get domain from .env
    deploy_dir = Path(__file__).parent
    env_file = deploy_dir / ".env"
    domain = "taxportal.nanobyte.ca"

    with open(env_file, 'r') as f:
        for line in f:
            if line.startswith("DOMAIN="):
                domain = line.strip().split("=", 1)[1]
                break

    print(f"Obtaining certificate for: {domain}")

    # Run certbot
    cmd = f'cd {REMOTE_DIR} && docker compose run --rm certbot certonly --webroot -w /var/www/certbot -d {domain} --email admin@nanobyte.ca --agree-tos --no-eff-email --non-interactive'
    exit_code, output, error = run_command(client, cmd)

    if exit_code == 0:
        print("SSL certificate obtained successfully!")

        # Upload full nginx config with SSL
        nginx_ssl = Path(__file__).parent / "nginx.conf"
        upload_file(client, str(nginx_ssl), f"{REMOTE_DIR}/nginx.conf")

        # Restart nginx
        run_command(client, f"cd {REMOTE_DIR} && docker compose restart nginx")
        print("Nginx restarted with SSL!")
        return True
    else:
        print("Failed to obtain SSL certificate. You may need to:")
        print("1. Ensure DNS is pointing to the server")
        print("2. Wait for DNS propagation")
        print("3. Try again later")
        return False

def check_status(client):
    """Check the status of deployed services."""
    print("\n=== Checking Status ===\n")
    run_command(client, f"cd {REMOTE_DIR} && docker compose ps")
    run_command(client, f"cd {REMOTE_DIR} && docker compose logs --tail=20 app")

def main():
    if len(sys.argv) < 2:
        print("Usage: python deploy_remote.py <command>")
        print("")
        print("Commands:")
        print("  setup    - Initial server setup (install Docker, firewall)")
        print("  deploy   - Deploy the application")
        print("  ssl      - Obtain SSL certificate")
        print("  status   - Check deployment status")
        print("  all      - Run setup + deploy + ssl")
        return

    command = sys.argv[1].lower()

    try:
        client = create_ssh_client()

        if command == "setup":
            setup_server(client)
        elif command == "deploy":
            deploy_application(client)
        elif command == "ssl":
            obtain_ssl_certificate(client)
        elif command == "status":
            check_status(client)
        elif command == "all":
            setup_server(client)
            if deploy_application(client):
                print("\nWaiting 30 seconds before obtaining SSL certificate...")
                time.sleep(30)
                obtain_ssl_certificate(client)
        else:
            print(f"Unknown command: {command}")

        client.close()

    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
