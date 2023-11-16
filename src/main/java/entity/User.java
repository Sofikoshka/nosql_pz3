package entity;


import entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private long id;
    private String name;
    private String lastname;
    private String email;
    private String phone;
    private String password;
    private Role role;

    public User(String name, String lastname, String email, String phone, String password, Role role) {
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
    }

    public Role getRole() {
        // ensure that you are returning a non-null Role enum
        return role != null ? role : Role.CLIENT;
    }
}
