package pisio.backend.models.entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "FILES")
@Data
public class File
{
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "fileID", nullable = false)
    private Integer fileID;

    @Basic
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    // uploadedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", nullable = false)
    private UserEntity user;
}
