#!/bin/bash

echo "🏗️  Building Hospital Management System..."
echo ""

# Build Scheduling Service
echo "📦 Building Scheduling Service..."
cd scheduling-service
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    echo "✅ Scheduling Service built successfully"
else
    echo "❌ Failed to build Scheduling Service"
    exit 1
fi
cd ..

# Build Notification Service
echo ""
echo "📦 Building Notification Service..."
cd notification-service
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    echo "✅ Notification Service built successfully"
else
    echo "❌ Failed to build Notification Service"
    exit 1
fi
cd ..

# Build History Service
echo ""
echo "📦 Building History Service..."
cd history-service
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    echo "✅ History Service built successfully"
else
    echo "❌ Failed to build History Service"
    exit 1
fi
cd ..

echo ""
echo "🎉 All services built successfully!"
echo ""
echo "Next steps:"
echo "1. Start Docker containers: docker-compose up -d"
echo "2. Run Scheduling Service: cd scheduling-service && mvn spring-boot:run"
echo "3. Run Notification Service: cd notification-service && mvn spring-boot:run"
echo "4. Run History Service: cd history-service && mvn spring-boot:run"
