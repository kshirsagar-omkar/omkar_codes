# n8n Workflow Automation Setup

## Overview
n8n is a free and open-source workflow automation tool that allows you to automate tasks across different services.

## Quick Start

### Start n8n
```bash
cd /workspaces/omkar_codes/ai/n8n
docker compose up -d
```

### Access n8n
Open your browser and go to: **http://localhost:5678**

### Stop n8n
```bash
docker compose down
```

### View Logs
```bash
docker compose logs -f n8n
```

## Configuration

### Environment Variables
The following environment variables are configured in `docker-compose.yml`:

| Variable | Value | Description |
|----------|-------|-------------|
| `GENERIC_TIMEZONE` | Asia/Kolkata | Timezone for scheduled tasks |
| `TZ` | Asia/Kolkata | System timezone |
| `N8N_HOST` | localhost | Host address |
| `N8N_PORT` | 5678 | Port number |
| `N8N_PROTOCOL` | http | Protocol (http/https) |
| `WEBHOOK_URL` | http://localhost:5678/ | Webhook base URL |
| `N8N_SECURE_COOKIE` | false | Disable secure cookies for local dev |

### Persistent Storage
All data is stored in the `n8n_data` Docker volume:
- Workflows
- Credentials (encrypted)
- Execution history
- Settings

## Updating n8n

```bash
# Pull latest image
docker compose pull

# Restart with new image
docker compose down
docker compose up -d
```

## Useful Commands

```bash
# Check container status
docker ps -a | grep n8n

# View container logs
docker logs n8n

# Enter container shell
docker exec -it n8n /bin/sh

# Backup data volume
docker run --rm -v n8n_data:/data -v $(pwd):/backup alpine tar cvf /backup/n8n_backup.tar /data

# Restore data volume
docker run --rm -v n8n_data:/data -v $(pwd):/backup alpine tar xvf /backup/n8n_backup.tar -C /
```

## Troubleshooting

### Container won't start
```bash
# Check logs
docker compose logs n8n

# Check if port is in use
lsof -i :5678
```

### Reset n8n (Warning: Deletes all data)
```bash
docker compose down
docker volume rm n8n_data
docker volume create n8n_data
docker compose up -d
```
