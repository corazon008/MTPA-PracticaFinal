# MTPA-PracticaFinal

A real-time messaging system with a Java server and Swing GUI client.

**Authors**: Corentin CALVEZ, Lubin TERRIEN  

---

## Requirements

- **Java**: JDK 24+
- **Maven**: 3.9+

---

## Running the Application

### 1. Start the Server

```bash
./run-server.sh
```

Or from IntelliJ: Right-click `src/main/java/org/example/server/ServerApplication.java` → Run

**Default port**: 5000

### 2. Start the Client

```bash
./run-client.sh
```

Or from IntelliJ: Right-click `src/main/java/org/example/client/ClientApplication.java` → Run


---

## Quick Start

1. **Register**: Enter a username and click "Registrarse"
2. **Login**: Use the username and the generated access key
3. **Chat in Rooms**: Select a room and send messages
4. **Private Chat**: Enter a username in "Nombre del usuario" and click "Iniciar Chat Privado"

---

## Tests

Run all tests:
```bash
mvn test
```


---

## Features

- User registration and authentication
- Public chat in predefined rooms
- Private messaging with user verification
- Message history with date search
- 190 character limit per message
- Server admin console
- Automatic heartbeat (connection keep-alive)
