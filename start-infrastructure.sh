#!/bin/bash

echo "🚀 Starting Hospital Management System..."
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Start Docker Compose services
echo "🐳 Starting RabbitMQ and PostgreSQL..."
docker-compose up -d

# Wait for services to be healthy
echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

echo ""
echo "✅ Infrastructure services started!"
echo ""
echo "📊 Service URLs:"
echo "   - RabbitMQ Management: http://localhost:15672 (guest/guest)"
echo "   - PostgreSQL: localhost:5432 (hospital_user/hospital_pass)"
echo ""
echo "Now you can start the application services:"
echo ""
echo "Terminal 1 - Scheduling Service:"
echo "   cd scheduling-service && mvn spring-boot:run"
echo ""
echo "Terminal 2 - Notification Service:"
echo "   cd notification-service && mvn spring-boot:run"
echo ""
echo "Terminal 3 - History Service (Optional):"
echo "   cd history-service && mvn spring-boot:run"
echo ""
echo "📝 API Documentation:"
echo "   - Scheduling API: http://localhost:8080/api/appointments"
echo "   - GraphQL Playground: http://localhost:8082/graphiql"
