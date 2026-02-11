# Minecraft CLI Mod (mccli)

This NeoForge mod provides a socket-based API (CLI) for external agents (AI, scripts) to control the Minecraft player.

## Features
*   **State Observation**: Get player health, position, hunger, etc.
*   **Inventory**: Inspect inventory items.
*   **Movement**: Control movement (WASD, jump, sneak).
*   **Interaction**: Attack, use items, interact with blocks/entities.
*   **Vision**: Get screenshots of the current view.
*   **UI**: Inspect open container GUIs (chests, inventory, etc.).

## Installation
1.  Build the mod: `./gradlew build`
2.  Copy the jar from `build/libs/` to your Minecraft `mods` folder.
3.  Launch Minecraft with NeoForge 1.21.4.

## Usage
The mod starts a TCP server on port **25566** when the client loads.
You can connect to it using `netcat`, Python, or any TCP client.

### Protocol
Send a JSON string ending with a newline.
The JSON must have a `command` field.
The server responds with a JSON string ending with a newline.

### Commands

#### `ping`
Check connectivity.
*   Request: `{"command": "ping"}`
*   Response: `{"status": "success", "message": "pong"}`

#### `state`
Get player state.
*   Request: `{"command": "state"}`
*   Response: `{"status": "success", "data": { ... }}`

#### `inventory`
Get player inventory.
*   Request: `{"command": "inventory"}`
*   Response: `{"status": "success", "data": { "main": [...], "armor": [...], ... }}`

#### `move`
Control movement keys.
*   Request: `{"command": "move", "forward": true, "jump": false}`
*   Flags: `forward`, `back`, `left`, `right`, `jump`, `sneak`, `sprint`.

#### `look`
Set player rotation.
*   Request: `{"command": "look", "yaw": 0.0, "pitch": 0.0}`

#### `interact`
Perform actions.
*   Request: `{"command": "interact", "action": "attack"}`
*   Actions:
    *   `attack`: Left click (attack entity or break block).
    *   `use`: Right click (use item or interact block).
    *   `stop_use`: Release right click.
    *   `drop`: Drop selected item.
    *   `drop_stack`: Drop selected stack.

#### `view`
Get a screenshot.
*   Request: `{"command": "view"}`
*   Response: `{"status": "success", "image": "<base64_png>", "width": ..., "height": ...}`

#### `ui`
Get current UI info (open container slots).
*   Request: `{"command": "ui"}`
*   Response: `{"status": "success", "type": "...", "containerId": 1, "slots": [...]}`

#### `click_slot`
Click a slot in an open container.
*   Request: `{"command": "click_slot", "containerId": 1, "slot": 0, "button": 0, "type": "PICKUP"}`
*   `containerId`: ID from `ui` command.
*   `slot`: Slot index to click.
*   `button`: 0 (Left), 1 (Right).
*   `type`: `PICKUP`, `QUICK_MOVE` (Shift-click), `SWAP`, `CLONE`, `THROW`, `QUICK_CRAFT`, `PICKUP_ALL`.

## Mod Compatibility
The API uses registry names (e.g. `minecraft:stone`, `mekanism:osmium_ingot`) and standard NeoForge/Minecraft abstractions (`AbstractContainerMenu`, `HitResult`), ensuring compatibility with most mods.
