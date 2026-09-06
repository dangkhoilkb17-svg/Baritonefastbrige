package vn.fastbridge;

public enum BridgeStopReason {
    COMPLETED("Completed"), NO_BLOCKS("No building blocks"), NO_VALID_PLACEMENT("No valid placement"),
    PLAYER_UNSAFE("Player unsafe"), PLAYER_DEAD("Player dead"), DISCONNECTED("Disconnected"),
    CANCELLED("Cancelled"), INTERNAL_ERROR("Internal error");

    private final String message;
    BridgeStopReason(String message) { this.message = message; }
    public String message() { return message; }
}
