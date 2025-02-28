package tamtam.mooney.domain.transaction.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public enum IncomeCategory {
    // TODO: 수정하기
    SALARY("급여", "💰"),                // 월급, 고정 급여
    PART_TIME("알바", "🛠️"),            // 아르바이트 수입
    BUSINESS("사업 수익", "🏢"),         // 사업 운영 수익
    INVESTMENT("투자 수익", "📈"),       // 주식, 코인, 펀드 등 투자 수익
    INTEREST("이자 소득", "🏦"),        // 은행 예금/적금 이자
    DIVIDEND("배당금", "💸"),           // 주식 배당금
    RENTAL("임대 수입", "🏠"),          // 부동산 임대료 수입
    PENSION("연금", "👴"),              // 국민연금, 개인연금 등
    INSURANCE("보험금", "🩺"),          // 보험금 지급
    GIFT("용돈/선물", "🎁"),           // 부모님 용돈, 친구 선물 등
    PRIZE("당첨금", "🎯"),             // 복권, 이벤트 당첨금
    SUBSIDY("보조금", "🏛️"),           // 정부 지원금, 학자금 지원
    SECOND_JOB("부업", "🌙"),          // 부업, 프리랜서 수익
    ETC("이체", "↔️");                 // 그 외 기타 수입

    @NotNull
    private final String categoryName;

    @NotNull
    private final String icon;

    IncomeCategory(String categoryName, String icon) {
        this.categoryName = categoryName;
        this.icon = icon;
    }
}