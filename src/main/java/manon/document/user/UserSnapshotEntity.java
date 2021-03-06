package manon.document.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import manon.model.user.RegistrationState;
import manon.util.Tools;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Entity(name = "UserSnapshot")
@Table(name = "user_snapshot")
@Getter
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = {"user", "creationDate"})
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserSnapshotEntity implements Serializable {

    private static final long serialVersionUID = -4321502988403908385L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonView(WithUserView.class)
    @ManyToOne(fetch = LAZY)
    private UserEntity user;

    @Column(updatable = false)
    private String userUsername;

    @Column(updatable = false)
    private String userAuthorities;

    @JsonIgnore
    @Column(updatable = false)
    private String userPassword;

    @Column(updatable = false)
    private RegistrationState userRegistrationState;

    @Column(updatable = false)
    private String userNickname;

    @Column(updatable = false)
    private String userEmail;

    @Column(updatable = false)
    private long userVersion;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Column(nullable = false)
    private LocalDateTime creationDate;

    @PrePersist
    public void prePersist() {
        if (creationDate == null) {
            creationDate = Tools.now();
        }
    }

    /** Populate a {@link UserSnapshotEntity} from a {@link UserEntity}. */
    public static UserSnapshotEntity from(UserEntity user) {
        return UserSnapshotEntity.builder()
            .user(user)
            .userUsername(user.getUsername())
            .userAuthorities(user.getAuthorities())
            .userPassword(user.getPassword())
            .userRegistrationState(user.getRegistrationState())
            .userNickname(user.getNickname())
            .userEmail(user.getEmail())
            .userVersion(user.getVersion())
            .build();
    }

    public interface WithUserView {
    }
}
