================================================================================
                    CARBON CREDIT TRADING PLATFORM
                        MICROSERVICES PROJECT
================================================================================

TEAM MEMBERS
------------
1. Yash Sharma    - 22CSU295
2. Mukul Yadav    - 22CSU301


SERVICES WORKING WITHOUT ERRORS
--------------------------------
✓ Configuration Server (Port 8888) - Centralized configuration management
✓ Service Discovery/Eureka (Port 8761) - Service registration and discovery
✓ API Gateway (Port 8080) - Request routing and authentication
✓ Auth Service (Port 8081) - User signup, login, authentication
✓ User Service (Port 8082) - User profiles, credits, balance management
✓ Carbon Service (Port 8083) - Carbon credit listings management
✓ Trade Service (Port 8084) - Credit transfers and purchase transactions


SERVICES CODED BY EACH MEMBER
------------------------------

YASH SHARMA (22CSU295):
  • Auth Service (Port 8081)
  • API Gateway (Port 8080)
  • Service Discovery/Eureka (Port 8761)
  • Trade Service (Port 8084)

MUKUL YADAV (22CSU301):
  • Configuration Server (Port 8888)
  • User Service (Port 8082)
  • Carbon Service (Port 8083)


COMMON/INFRASTRUCTURE CODE
--------------------------
Configuration Server:  Yash Sharma (22CSU295)
Service Discovery:     Yash Sharma (22CSU295)
API Gateway:           MUKUL YADAV (22CSU301)


WORKING ENDPOINTS
-----------------
All endpoints accessed through: http://localhost:8080
Authentication: Basic Auth (username:password) for all except /auth/signup and /auth/login


Yash Sharma (22CSU295) - Port 8081
───────────────────────────────────────
POST /auth/signup        - Register new user
POST /auth/login         - Login and get JWT token
POST /auth/authenticate  - Validate credentials (internal)


USER SERVICE (Mukul Yadav) - Port 8082
───────────────────────────────────────
POST /users                        - Create user profile
GET  /users/{id}                   - Get user details
POST /users/{id}/addCredits        - Add credits (query param: amount)
POST /users/{id}/removeCredits     - Remove credits (query param: amount)
POST /users/{id}/addBalance        - Add balance (query param: amount)
POST /users/{id}/removeBalance     - Remove balance (query param: amount)


CARBON SERVICE (Mukul Yadav) - Port 8083
─────────────────────────────────────────
GET    /carbon      - List all carbon credit listings
GET    /carbon/{id} - Get listing details
POST   /carbon      - Create new listing
PUT    /carbon/{id} - Update listing (requires X-User-Id header)
DELETE /carbon/{id} - Delete listing (requires X-User-Id header)


TRADE SERVICE (Yash Sharma) - Port 8084
────────────────────────────────────────
POST /trades        - Simple credit transfer between users
POST /trades/carbon - Purchase carbon credits from listing
GET  /trades        - View all trade history
GET  /trades/{id}   - Get trade details


EXAMPLE CALLS
-------------

1. Signup
curl -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"alice\",\"password\":\"alice123\"}"


2. Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"alice\",\"password\":\"alice123\"}"


3. Create User Profile
curl -X POST http://localhost:8080/users \
  -u alice:alice123 \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Alice Smith\",\"credits\":100,\"balance\":5000}"


4. Get User
curl -X GET http://localhost:8080/users/1 \
  -u alice:alice123


5. Add Credits
curl -X POST "http://localhost:8080/users/1/addCredits?amount=50" \
  -u alice:alice123


6. Create Carbon Listing
curl -X POST http://localhost:8080/carbon \
  -u bob:bob123 \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Solar Credits\",\"supply\":100,\"ownerId\":2,\"price\":40.0}"


7. List All Carbon Credits
curl -X GET http://localhost:8080/carbon \
  -u alice:alice123


8. Simple Credit Transfer
curl -X POST http://localhost:8080/trades \
  -u alice:alice123 \
  -H "Content-Type: application/json" \
  -d "{\"fromUserId\":1,\"toUserId\":2,\"amount\":20}"


9. Purchase Carbon Credits
curl -X POST http://localhost:8080/trades/carbon \
  -u alice:alice123 \
  -H "Content-Type: application/json" \
  -d "{\"buyerId\":1,\"carbonId\":1,\"quantity\":25}"


10. View Trade History
curl -X GET http://localhost:8080/trades \
  -u alice:alice123


SETUP INSTRUCTIONS
------------------
1. Create MySQL databases: authdb, carboncreditdb, carbondb, tradedb
2. Start services in order:
   - Configuration Server (8888)
   - Service Discovery (8761)
   - Auth Service (8081)
   - User Service (8082)
   - Carbon Service (8083)
   - Trade Service (8084)
   - API Gateway (8080)

Access Points:
  - API Gateway: http://localhost:8080
  - Eureka Dashboard: http://localhost:8761


================================================================================
