package tamtam.mooney.domain.mission.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tamtam.mooney.global.common.entity.BaseTimeEntity;
import tamtam.mooney.domain.budget.entity.CategoryBudget;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Mission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long missionId;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDateTime startDate; // UTC 기준으로 저장

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDateTime endDate; // UTC 기준으로 저장

    @NotNull
    @Column(nullable = false)
    private String title; // 미션 제목

    @Lob
    @NotNull
    @Column(columnDefinition = "TEXT", nullable = false)
    private String advice; // 미션 조언 (긴 문자열 가능)

    private String result; // 미션 결과

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_budget_id", nullable = false)
    private CategoryBudget categoryBudget; // 연관된 월별 카테고리 예산

    @Builder
    public Mission(LocalDateTime startDate, LocalDateTime endDate, String title, String advice, CategoryBudget categoryBudget) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
        this.advice = advice;
        this.categoryBudget = categoryBudget;
    }

    public void updateResult(String result) {
        this.result = result;
    }
}
