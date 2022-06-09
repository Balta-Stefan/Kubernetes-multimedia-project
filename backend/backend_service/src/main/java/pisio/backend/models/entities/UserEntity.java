package pisio.backend.models.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "USERS")
@Data
public class UserEntity
{
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "userID", nullable = false)
    private Integer userID;

    @Basic
    @Column(name = "username", nullable = false, unique = true, length = 45)
    private String username;

    @Basic
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Basic
    @Column(name = "email", nullable = false, unique = true, length = 65)
    private String email;

    @Basic
    @Column(name = "active", nullable = false)
    private Boolean active;
}
