#!/bin/bash

echo "🛑 Stopping Hospital Management System..."
echo ""

# Stop Docker Compose services
docker-compose down

echo ""
echo "✅ All services stopped!"
