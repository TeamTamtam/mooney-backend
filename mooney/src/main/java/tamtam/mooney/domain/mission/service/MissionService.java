package tamtam.mooney.domain.mission.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Schedulers;
import tamtam.mooney.domain.budget.entity.CategoryBudget;
import tamtam.mooney.domain.budget.repository.CategoryBudgetRepository;
import tamtam.mooney.domain.budget.repository.MonthlyBudgetRepository;
import tamtam.mooney.domain.mission.entity.Mission;
import tamtam.mooney.domain.mission.repository.MissionRepository;
import tamtam.mooney.domain.transaction.repository.TransactionRepository;
import tamtam.mooney.domain.user.dto.UserHomeWeeklyMissionDto;
import tamtam.mooney.domain.user.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.core.ParameterizedTypeReference;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MissionService {
    private final MissionRepository missionRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryBudgetRepository categoryBudgetRepository;
    private final WebClient webClient; // FastAPI 서버에서 데이터 가져오기 위한 클라이언트
    private final MonthlyBudgetRepository monthlyBudgetRepository;

    private static final String FASTAPI_URL = "https://mooney-ai.o-r.kr/predict"; // FastAPI URL


    // 저장해놓은 미션 가져오기(홈)
    public List<UserHomeWeeklyMissionDto> getWeeklyMissions(User user, LocalDate today) {
        List<Mission> missions = missionRepository.findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(user.getUserId(), today, today);

        return missions.stream()
                .map(mission -> new UserHomeWeeklyMissionDto(
                        mission.getTitle(),
                        mission.getResult()))
                .collect(Collectors.toList());
    }


     //# FastAPI 서버에서 카테고리 및 예상 지출 금액을 가져와 현재 주별 예산과 비교하는 비동기 메서드(1~3 포함)
     private Flux<Map<String, Object>> getSelectedCategories(User user) {
         return fetchPredictedSpending(user)
                 .collectList() // ✅ 전체 카테고리 데이터를 List로 변환
                 .flatMapMany(categoryDataList -> compareWithWeeklyBudget(user, categoryDataList)); // ✅ 모든 카테고리를 개별적으로 처리
     }

    //1. FastAPI에서 다음주 예상 지출 데이터 가져오기
    private Flux<Map<String, Object>> fetchPredictedSpending(User user) {
        return webClient.get()
                .uri(FASTAPI_URL + "?userId=" + user.getUserId())
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * 2. 특정 카테고리에 대해 현재 주별 예산과 비교
     * Map.of(
     *     "Category", category, // String
     *     "yhat_adjusted", predictedSpending // Long
     * )
     */
    // 3️⃣ 특정 카테고리에 대해 현재 주별 예산과 비교 (모든 카테고리를 개별적으로 처리)
    private Flux<Map<String, Object>> compareWithWeeklyBudget(User user, List<Map<String, Object>> categoryDataList) {
        return Flux.fromIterable(categoryDataList) // ✅ 여러 개의 카테고리를 개별적으로 처리
                .flatMap(categoryData -> {
                    String category = (String) categoryData.get("Category");
                    Long predictedSpending = ((Number) categoryData.get("yhat_adjusted")).longValue(); // 안전한 변환

                    LocalDate monthDate = LocalDate.now().withDayOfMonth(1);

                    return calculateWeeklyBudget(user, category) // ✅ 각 카테고리에 대해 주별 예산 계산
                            .flatMap(realWeeklyCategoryBudget ->
                                    categoryBudgetRepository.findByUserIdAndCategoryAndMonth(user.getUserId(), category, monthDate)
                                            .defaultIfEmpty(0L) // 예산이 없으면 0 처리
                                            .flatMap(weeklyBudget -> {
                                                if (realWeeklyCategoryBudget < predictedSpending) { // 비교 연산
                                                    return Mono.just(Map.of(
                                                            "Category", category,
                                                            "WeeklyBudget", realWeeklyCategoryBudget,
                                                            "PredictedSpending", predictedSpending
                                                    ));
                                                } else {
                                                    return Mono.empty(); // 초과 지출 예상이 아닌 경우 데이터 반환하지 않음
                                                }
                                            })
                            );
                });
    }



    //3.
    private Mono<Long> calculateWeeklyBudget(User user, String category) {
        LocalDate today = LocalDate.now();
        LocalDate monthDate = today.withDayOfMonth(1); // 해당 월의 첫째 날

        // 1️⃣ 현재 월의 전체 주 수 계산
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int totalWeeks = today.with(TemporalAdjusters.lastDayOfMonth()).get(weekFields.weekOfMonth());

        // 2️⃣ 오늘이 해당 월의 몇 번째 주인지 계산
        int currentWeek = today.get(weekFields.weekOfMonth());

        // 3️⃣ 남은 주 수 계산 (현재 주 포함)
        int remainingWeeks = totalWeeks - currentWeek + 1;

        // 4️⃣ 비동기적으로 예산과 지출을 가져와서 연산 수행
        return categoryBudgetRepository.findByUserIdAndCategoryAndMonth(user.getUserId(), category, monthDate)
                .defaultIfEmpty(0L) // 예산이 없을 경우 0으로 처리
                .flatMap(categoryBudget ->
                        transactionRepository.getTotalCategoryExpenseAmountForMonth(user, category, monthDate, LocalDateTime.now())
                                .defaultIfEmpty(0L) // 지출이 없을 경우 0으로 처리
                                .map(totalExpense -> categoryBudget - totalExpense) // 비동기적으로 뺄셈 연산 수행
                )
                .map(leftCategoryBudget -> leftCategoryBudget / remainingWeeks); // 남은 예산을 남은 주 수로 나눔
    }



     //유저의 이번 주 미션을 생성하여 반환 (비동기)
    /**
     * "Category", category,
     * "WeeklyBudget", realWeeklyCategoryBudget,
     * "PredictedSpending", predictedSpending
     **/
    public Mono<List<Mission>> generateWeeklyMissions(User user) {
        LocalDate thisMonthDate = LocalDate.now().withDayOfMonth(1);

        return getSelectedCategories(user) // ✅ 이미 Flux<Map<String, Object>>을 반환함
                .flatMap(categoryData -> {
                    // ✅ 각 Map<String, Object>에서 "Category"와 "PredictedSpending" 값을 추출
                    String category = (String) categoryData.get("Category");
                    long realWeeklyCategoryBudget = ((Number) categoryData.get("WeeklyBudget")).longValue();
                    long expectedSpending = ((Number) categoryData.get("PredictedSpending")).longValue();
                    CategoryBudget categoryBudget = categoryBudgetRepository.findByUserIdAndCategoryAndMonth(user.getUserId(), category, thisMonthDate);

                    // 비동기 방식으로 데이터 가져오기
                    Mono<Map<String, Float>> visitDataMono = getVisitData(user, category); // 평균 주별 방문 횟수
                    Mono<Map<String, Float>> spendingDataMono = getSpendingData(user, category); // 한 번 방문 시 평균 사용 금액

                    return Mono.zip(visitDataMono, spendingDataMono)
                            .map(tuple -> {
                                Map<String, Float> visitData = tuple.getT1();
                                Map<String, Float> spendingData = tuple.getT2();

                                // 미션 생성
                                Mission mission = generateCategoryMission(category, visitData, spendingData, realWeeklyCategoryBudget, expectedSpending);
                                return mission;
                            });
                })
                .collectList(); // 최종 결과를 리스트로 반환
    }


    /**
     * 특정 카테고리에 대한 미션 생성
     */
    private Mission generateCategoryMission(String category, Map<String, Float> visitData,
                                           Map<String, Float> spendingData, long realWeeklyCategoryBudget, long expectedSpending, CategoryBudget categoryBudget) {
        float requiredSaving = expectedSpending - realWeeklyCategoryBudget;

        // 1️⃣ 예산이 충분한 경우 기본 미션 반환
        if (requiredSaving <= 0) {
            return Mission.builder()
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusDays(7)) // 1주일 동안 유지
                    .title(category + " 소비를 현명하게! 🎯")
                    .advice("현재 예산 내에서 소비할 수 있습니다. 효율적인 지출을 유지하세요.")
                    .categoryBudget(categoryBudget)
                    .build();
        }

        // 방문 횟수 기준 정렬 (많이 방문한 곳 우선)
        List<Map.Entry<String, Float>> sortedVisits = visitData.entrySet().stream()
                .sorted((a, b) -> Float.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toList());

        // 소비 금액 기준 정렬 (가장 많이 소비한 곳 우선)
        List<Map.Entry<String, Float>> sortedSpending = spendingData.entrySet().stream()
                .sorted((a, b) -> Float.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toList());

        String visitMission = null;
        String spendingMission = null;
        float visitGap = Float.MAX_VALUE;
        float spendingGap = Float.MAX_VALUE;

        // 방문 횟수 제한 미션
        if (!sortedVisits.isEmpty()) {
            Map.Entry<String, Float> targetVisit = sortedVisits.get(0);
            String place = targetVisit.getKey();
            float visits = targetVisit.getValue();
            float averageCost = spendingData.getOrDefault(place, 0f);

            float maxAllowedVisits = Math.max(1, realWeeklyCategoryBudget / averageCost); // 예산 내에서 최대 허용 방문 횟수
            maxAllowedVisits = Math.min(visits, maxAllowedVisits); // 기존 방문 횟수보다 줄지 않도록 설정

            float expectedVisitSpending = maxAllowedVisits * averageCost; // 방문 조정 후 예상 소비 금액
            float visitSpendingGap = Math.abs(realWeeklyCategoryBudget - expectedVisitSpending); // 허용 소비 금액과 차이 계산

            visitMission = "☕ 이번 주는 " + place + " 방문을 " + (int) maxAllowedVisits + "회 이하로 유지하며 예산을 지켜봐요!";
            visitGap = visitSpendingGap;
        }

        // 금액 제한 미션
        if (!sortedSpending.isEmpty()) {
            Map.Entry<String, Float> targetSpending = sortedSpending.get(0);
            String place = targetSpending.getKey();
            float spending = targetSpending.getValue();

            float maxAllowedSpending = Math.max(realWeeklyCategoryBudget / 2, spending - (requiredSaving / 2)); // 허용된 범위 내에서 조절
            maxAllowedSpending = Math.min(spending, maxAllowedSpending); // 원래 소비 금액보다 크지 않도록 조절

            float spendingGapValue = Math.abs(realWeeklyCategoryBudget - maxAllowedSpending); // 허용 소비 금액과 차이 계산

            spendingMission = "💰 이번 주는 " + place + "에서 " + (int) maxAllowedSpending + "원 이하로 소비해봐요!";
            spendingGap = spendingGapValue;
        }

        // 허용 소비 금액에 가장 근접한 미션 선택
        return (visitGap <= spendingGap) ? visitMission : spendingMission;
    }


    /**
     * 특정 미션 결과를 가져오기
     */
    public Float getMissionResult(Long missionId) {
        return missionRepository.findMissionResultById(missionId);
    }

    /**
     * 특정 카테고리의 예산을 비동기적으로 가져오기
     */
    private Mono<Long> getUserCategoryBudget(User user, String category, LocalDate monthDate) {
        return Mono.fromCallable(() -> categoryBudgetRepository.findByUserIdAndCategoryAndMonth(user.getUserId(), category, monthDate))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 특정 카테고리의 방문 횟수 데이터를 비동기적으로 가져오기
     */
    private Mono<Map<String, Float>> getVisitData(User user, String category) {
        return Mono.fromCallable(() -> transactionRepository.findVisitDataByCategory(user.getUserId(), category))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 특정 카테고리의 소비 데이터를 비동기적으로 가져오기
     */
    private Mono<Map<String, Float>> getSpendingData(User user, String category) {
        return Mono.fromCallable(() -> transactionRepository.findSpendingDataByCategory(user.getUserId(), category))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
