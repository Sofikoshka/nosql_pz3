package entity.enums;

public enum OrderStatus {
    PROCESSING("processing"),
    ACCEPTED("accepted"),
    PACKED("packed"),
    DELIVERED("delivered"),
    CANCELLED("cancelled");

    private final String status;

    OrderStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}

