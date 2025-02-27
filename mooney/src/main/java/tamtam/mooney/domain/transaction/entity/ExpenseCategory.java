package tamtam.mooney.domain.transaction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tamtam.mooney.global.common.entity.BaseTimeEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ExpenseCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long expenseCategoryId;

    @NotNull
    private String expenseCategoryName;

    @NotNull
    private String icon;

    @Builder
    public ExpenseCategory(String expenseCategoryName, String icon) {
        this.expenseCategoryName = expenseCategoryName;
        this.icon = icon;
    }
}
