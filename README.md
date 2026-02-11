# Minecraft CLI Mod (mccli)

This NeoForge mod provides a socket-based API (CLI) for external agents (AI, scripts) to control the Minecraft player.

## Features
*   **State Observation**: Get player health, position, hunger, etc.
*   **Inventory**: Inspect inventory items and containers.
*   **Movement**: Control movement (WASD, jump, sneak) or pathfind to coordinates.
*   **Interaction**: Attack, use items, mine blocks, place blocks.
*   **Vision**: Get screenshots of the current view and inspect nearby blocks/entities.
*   **Communication**: Send/Receive chat messages and execute commands.
*   **Connectivity**: Connect to servers programmatically.
*   **Events**: Receive real-time JSON events (chat, death, join).

## Installation
1.  Build the mod: `./gradlew build`
2.  Copy the jar from `build/libs/` to your Minecraft `mods` folder.
3.  Launch Minecraft with NeoForge 1.21.4.

## Usage
The mod starts a TCP server on port **25566** (localhost) when the client loads.

### Protocol
Send a JSON string ending with a newline.
The server responds with a JSON string ending with a newline.
The server also sends asynchronous JSON events.

### Commands

#### `ping`
Check connectivity.
*   Request: `{"command": "ping"}`
*   Response: `{"status": "success", "message": "pong"}`

#### `connect`
Connect to a server.
*   Request: `{"command": "connect", "address": "localhost:25565"}`

#### `chat`
Send chat message.
*   Request: `{"command": "chat", "message": "Hello world"}`

#### `execute`
Execute slash command (without slash usually, or with).
*   Request: `{"command": "execute", "cmd": "gamemode creative"}`

#### `state`
Get player state.
*   Request: `{"command": "state"}`
*   Response: `{"status": "success", "data": { "x": 100, "y": 64, "z": 100, "health": 20, ... }}`

#### `inventory`
Get player inventory.
*   Request: `{"command": "inventory"}`
*   Response: `{"status": "success", "data": { "main": [...], "armor": [...], "offhand": [...] }}`

#### `equip`
Select hotbar slot.
*   Request: `{"command": "equip", "slot": 0}`

#### `move`
Control movement keys.
*   Request: `{"command": "move", "forward": true, "jump": false}`
*   Flags: `forward`, `back`, `left`, `right`, `jump`, `sneak`, `sprint`.

#### `look`
Set player rotation.
*   Request: `{"command": "look", "yaw": 0.0, "pitch": 0.0}`

#### `goto`
Simple pathfinding to coordinates.
*   Request: `{"command": "goto", "x": 100, "z": 100}`
*   (Optional `y` to target specific height).
*   Stops when within 0.5 blocks.

#### `interact`
Perform actions.
*   Request: `{"command": "interact", "action": "attack"}`
*   Actions: `attack`, `use`, `stop_use`, `drop`, `drop_stack`.

#### `mine`
Mine block at coordinates.
*   Request: `{"command": "mine", "x": 100, "y": 64, "z": 100}`

#### `place`
Place block at coordinates.
*   Request: `{"command": "place", "x": 100, "y": 64, "z": 100, "face": "UP"}`

#### `inspect`
Inspect block at coordinates.
*   Request: `{"command": "inspect", "x": 100, "y": 64, "z": 100}`
*   Response: `{"status": "success", "data": { "id": "minecraft:stone", "properties": {...}, "nbt": "..." }}`

#### `entities`
Get nearby entities.
*   Request: `{"command": "entities", "radius": 50, "type": "minecraft:zombie"}`
*   Response: `{"status": "success", "data": [...]}`

#### `view`
Get a screenshot.
*   Request: `{"command": "view"}`
*   Response: `{"status": "success", "image": "<base64_png>"}`

#### `ui`
Get current UI info.
*   Request: `{"command": "ui"}`
*   Response: `{"status": "success", "containerId": 1, "slots": [...]}`

#### `click_slot`
Click a slot in an open container.
*   Request: `{"command": "click_slot", "containerId": 1, "slot": 0, "button": 0, "type": "PICKUP"}`

### Events
The server sends JSON objects with an "event" field.

*   **Chat**: `{"event": "chat", "message": "Player joined the game"}`
*   **Death**: `{"event": "death", "message": "Player died"}`
*   **Join**: `{"event": "join", "dimension": "minecraft:overworld"}`
