# Software Development Life Cycle (SDLC) Plan

## 1. Requirement Analysis
*   **Goal**: Create a Minecraft CLI API for AI agents.
*   **Key Features**: State observation, inventory management, movement, interaction, vision (screenshots).
*   **Constraint**: NeoForge compatibility, avoid hardcoding, secure execution.

## 2. Design
*   **Architecture**:
    *   **Mod (Client-Side)**: `net.mccli.McCliMod` initializes the system.
    *   **Server**: `net.mccli.server.ApiServer` listens on TCP port 25566 (localhost only).
    *   **Command Handler**: `net.mccli.server.CommandHandler` dispatches JSON commands.
    *   **Commands**: Granular command classes (`GetStateCommand`, `MoveCommand`, etc.) implement logic on the main game thread.
*   **Protocol**: JSON-based request/response over TCP.
*   **Security**: Bind to loopback address to prevent external access.

## 3. Implementation
*   **Project Structure**: Standard NeoForge MDK layout.
*   **Dependencies**: NeoForge 1.21.4, Gson (via Minecraft).
*   **Versioning**: Git for source control.

## 4. Testing
*   **Unit Testing**: JSON parsing logic (implicit via successful compilation and logic check).
*   **Integration Testing**: Manual verification of commands in-game (implied usage).
*   **Verification**: Ensure all commands return valid JSON and handle errors gracefully.

## 5. Deployment
*   **Build**: `./gradlew build` produces a JAR file.
*   **Installation**: Drop JAR into `mods/` folder.
*   **Update Strategy**: Users replace the JAR file.

## 6. Maintenance
*   **Mod Compatibility**: Use registry names and standard abstractions (`AbstractContainerMenu`) to support future mods.
*   **Documentation**: Keep `README.md` updated with API changes.
