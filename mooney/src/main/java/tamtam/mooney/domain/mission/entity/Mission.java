package tamtam.mooney.domain.mission.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tamtam.mooney.domain.enums.MissionType;
import tamtam.mooney.global.common.entity.BaseTimeEntity;
import tamtam.mooney.domain.budget.entity.CategoryBudget;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Mission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long missionId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String advice;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDate startDate; // UTC 기준으로 저장

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDate endDate; // UTC 기준으로 저장

    @NotNull
    @Column(nullable = false)
    private String title; // 미션 제목

    @NotNull
    @Column(columnDefinition = "TEXT", nullable = false)
    private String place;

    @Column(nullable = false)
    private Float result = 3F;
    // 미션 결과 (0 ~ 5(잘함))

    @Column(nullable = false)
    @Enumerated()
    private MissionType missionType;

    @Column(nullable = false)
    private long max; //missionType이 VISIT이면 최대방문횟수, missionType이 EXPENSE이면 최대사용금액

    @Column(nullable = false)
    private long numOfExpense = 0;

    @Column(nullable = false)
    private long amountOfExpense = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_budget_id", nullable = false)
    private CategoryBudget categoryBudget; // 연관된 월별 카테고리 예산

    @Builder
    public Mission(MissionType missionType, LocalDate startDate, LocalDate endDate, String title, String place, String advice, CategoryBudget categoryBudget, long max) {
        this.missionType = missionType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
        this.place = place;
        this.advice = advice;
        this.categoryBudget = categoryBudget;
        this.max = max;
    }


    public void updateResult(Float result) {
        this.result = result;
    }

    public void addExpense(long amount) {
        this.numOfExpense += 1;
        this.amountOfExpense += amount;
    }
}
