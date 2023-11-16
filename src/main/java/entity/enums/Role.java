package entity.enums;


public enum Role {
    CLIENT("Client"),
    ADMIN("Admin");
    private String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
