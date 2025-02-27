package tamtam.mooney.domain.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import tamtam.mooney.global.common.entity.BaseTimeEntity;
import tamtam.mooney.global.common.enums.Role;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long userId;

    @Column(unique = true)
    @NotNull
    private String email;

    @NotNull
    private String password;

    @NotNull
    private String nickname;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    @ColumnDefault("true")
    @NotNull
    private Boolean isPushAlarmEnabled;

    @NotNull
    private Long exp;

    @Builder
    public User(String email, String password, String nickname, Role role, Boolean isPushAlarmEnabled) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.isPushAlarmEnabled = isPushAlarmEnabled != null ? isPushAlarmEnabled : true;
        this.exp = 0L;
    }
}
