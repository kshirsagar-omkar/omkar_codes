#!/bin/bash

# Keep-Alive Script for GitHub Codespaces
# This script keeps the Codespace active by running periodic commands
# Press Ctrl+C to stop

echo "=========================================="
echo "  n8n Keep-Alive Script Started"
echo "  Press Ctrl+C to stop"
echo "=========================================="
echo ""

INTERVAL=300  # 5 minutes (300 seconds)
COUNT=0

while true; do
    COUNT=$((COUNT + 1))
    TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
    
    echo "[$TIMESTAMP] Heartbeat #$COUNT"
    echo "-------------------------------------------"
    
    # Check n8n container status
    echo "📦 Container Status:"
    docker ps --filter "name=n8n" --format "   Name: {{.Names}} | Status: {{.Status}} | Ports: {{.Ports}}"
    
    # Show uptime
    echo ""
    echo "⏱️  System Uptime: $(uptime -p)"
    
    # Show memory usage
    echo ""
    echo "💾 Memory Usage:"
    free -h | awk 'NR==2{printf "   Used: %s / %s (%.1f%%)\n", $3, $2, $3/$2*100}'
    
    # Show disk usage
    echo ""
    echo "💿 Disk Usage:"
    df -h / | awk 'NR==2{printf "   Used: %s / %s (%s)\n", $3, $2, $5}'
    
    echo ""
    echo "⏳ Next check in $INTERVAL seconds..."
    echo "==========================================="
    echo ""
    
    sleep $INTERVAL
done
