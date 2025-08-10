package tamtam.mooney.domain.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import tamtam.mooney.global.common.entity.BaseTimeEntity;
import tamtam.mooney.global.common.enums.Role;

import java.time.ZoneId;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_account")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long userId;

    @Column(unique = true, nullable = false)
    @NotNull
    private String email;

    @NotNull
    @Column(nullable = false)
    private String encryptedPassword;

    @NotNull
    @Column(nullable = false)
    private String nickname;

    @NotNull
    private ZoneId timezone;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Role role;

    @ColumnDefault("true")
    @Column(nullable = false)
    private boolean isPushAlarmEnabled;

    @NotNull
    @Column(nullable = false)
    private Long exp;

    @Builder
    public User(String email, String encryptedPassword, String nickname, ZoneId timezone) {
        this.email = email;
        this.encryptedPassword = encryptedPassword;
        this.nickname = nickname;
        this.timezone = timezone;
        this.role = Role.ROLE_USER;
        this.isPushAlarmEnabled = true;
        this.exp = 0L;
    }
}
