package entity.enums;


public enum Type {
    RING("ring"),
    BRACELET("bracelet"),
    EARRINGS("earrings");

    private String type;

    Type(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
