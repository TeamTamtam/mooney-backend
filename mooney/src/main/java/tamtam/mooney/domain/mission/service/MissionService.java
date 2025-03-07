package tamtam.mooney.domain.mission.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import tamtam.mooney.domain.budget.entity.CategoryBudget;
import tamtam.mooney.domain.budget.repository.CategoryBudgetRepository;
import tamtam.mooney.domain.budget.repository.MonthlyBudgetRepository;
import tamtam.mooney.domain.mission.entity.Mission;
import tamtam.mooney.domain.mission.repository.MissionRepository;
import tamtam.mooney.domain.transaction.repository.TransactionRepository;
import tamtam.mooney.domain.user.dto.UserHomeWeeklyMissionDto;
import tamtam.mooney.domain.user.entity.User;
import org.springframework.core.ParameterizedTypeReference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
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
        List<Mission> missions = missionRepository.findWeeklyMissionsByUser(user.getUserId(), today);

        return missions.stream()
                .map(mission -> new UserHomeWeeklyMissionDto(
                        mission.getTitle(),
                        mission.getResult()))
                .collect(Collectors.toList());
    }


     //# FastAPI 서버에서 카테고리 및 예상 지출 금액을 가져와 현재 주별 예산과 비교하는 동기 메서드(1~3 포함)
    // => 지출 > 예상인 카테고리에 한해 카테고리, realWeeklyCategoryBudget, predictedSpending을 return
     private List<Map<String, Object>> getSelectedCategories(User user) {
         // 1️⃣ FastAPI에서 예상 지출 데이터 가져오기 (동기 방식)
         List<Map<String, Object>> categoryDataList = fetchPredictedSpending(user);

         // 2️⃣ 주별 예산과 비교하여 데이터 반환
         return compareWithWeeklyBudget(user, categoryDataList);
     }


    //1. FastAPI에서 다음주 예상 지출 데이터 가져오기
    private List<Map<String, Object>> fetchPredictedSpending(User user) {
        return webClient.get()
                .uri(FASTAPI_URL + "?userId=" + user.getUserId())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block(); // 동기 처리

    }

    /**
     * 2. 특정 카테고리에 대해 현재 주별 예산과 비교
     * Map.of(
     *     "Category", category, // String
     *     "yhat_adjusted", predictedSpending // Long
     * )
     */
    // 3️⃣ 특정 카테고리에 대해 현재 주별 예산과 비교 (모든 카테고리를 개별적으로 처리)
    private List<Map<String, Object>> compareWithWeeklyBudget(User user, List<Map<String, Object>> categoryDataList) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> categoryData : categoryDataList) {
            String category = (String) categoryData.get("Category");
            long predictedSpending = ((Number) categoryData.get("yhat_adjusted")).longValue();
            long realWeeklyCategoryBudget = calculateWeeklyBudget(user, category);

            if (realWeeklyCategoryBudget < predictedSpending) {
                Map<String, Object> categoryBudgetInfo = new HashMap<>();
                categoryBudgetInfo.put("Category", category);
                categoryBudgetInfo.put("WeeklyBudget", realWeeklyCategoryBudget);
                categoryBudgetInfo.put("PredictedSpending", predictedSpending);

                result.add(categoryBudgetInfo);
            }

        }
        return result;
    }

    //3.
    private Long calculateWeeklyBudget(User user, String category) {
        LocalDate today = LocalDate.now();
        LocalDate monthDate = today.withDayOfMonth(1); // 해당 월의 첫째 날

        // 1️⃣ 현재 월의 전체 주 수 계산
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int totalWeeks = today.with(TemporalAdjusters.lastDayOfMonth()).get(weekFields.weekOfMonth());

        // 2️⃣ 오늘이 해당 월의 몇 번째 주인지 계산
        int currentWeek = today.get(weekFields.weekOfMonth());

        // 3️⃣ 남은 주 수 계산 (현재 주 포함)
        int remainingWeeks = totalWeeks - currentWeek + 1;

        // 4️⃣ 예산과 지출을 가져와서 연산 수행
        Long categoryBudget = getUserCategoryBudget(user, category, monthDate);

        Long totalExpense = transactionRepository.getTotalCategoryExpenseAmountForMonth(user, category, monthDate, LocalDateTime.now()).orElse(0L);
        return (categoryBudget - totalExpense) / remainingWeeks;
    }



     //유저의 이번 주 미션을 생성하여 반환 (동기)
    /**
     * "Category", category,
     * "WeeklyBudget", realWeeklyCategoryBudget,
     * "PredictedSpending", predictedSpending
     **/
    public List<UserHomeWeeklyMissionDto> generateWeeklyMissions(User user) {
        List<Map<String, Object>> categoryDataList = getSelectedCategories(user);
        List<UserHomeWeeklyMissionDto> missions = new ArrayList<>();
        LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate startDate = LocalDate.now().minusMonths(2);
        LocalDate endDate = LocalDate.now();

        for (Map<String, Object> categoryData : categoryDataList) {
            String category = (String) categoryData.get("Category");
            long predictedSpending = ((Number) categoryData.get("yhat_adjusted")).longValue();
            long realWeeklyCategoryBudget = calculateWeeklyBudget(user, category);

            CategoryBudget categoryBudget = categoryBudgetRepository.findCategoryBudgetByUserIdAndCategoryAndMonth(user.getUserId(), category, currentMonthStart);

            Mission mission = generateCategoryMission(user, category, getTop3VisitDataByCategory(user, category, startDate, endDate), getTop3SpendingDataByCategory(user, category, startDate, endDate), realWeeklyCategoryBudget, predictedSpending, categoryBudget);
            missionRepository.save(mission);
            UserHomeWeeklyMissionDto userHomeWeeklyMissionDto = new UserHomeWeeklyMissionDto(mission.getTitle(), mission.getResult());
            missions.add(userHomeWeeklyMissionDto);
        }
        return missions;
    }


    /**
     * 특정 카테고리에 대한 미션 생성
     * ex.
     * title: CU 편의점 이번주에 3회 이하 가기,
     * title2: 스타벅스에서 이번주 15000원 이하 쓰기
     * advice: 평균적으로 일주일에 3.5회 / xxxxx원 마셔요. \n 2회 이하로 마시는 것은 어떨까요?
     */
    private Mission generateCategoryMission(User user, String category, List<Object[]> visitData,
                                            List<Object[]> spendingData, long realWeeklyCategoryBudget, long expectedSpending, CategoryBudget categoryBudget) {
        long requiredSaving = expectedSpending - realWeeklyCategoryBudget;
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(6);

        // 1️⃣ 예산이 충분한 경우 기본 미션 반환
        if (requiredSaving <= 0) {
            return Mission.builder()
                    .startDate(startDate) // 그날 오전 4시에 생성
                    .endDate(endDate) // 1주일 동안 유지
                    .title(category + " 소비를 현명하게! 🎯")
                    .advice("현재와 같은 지출을 유지하세요.")
                    .categoryBudget(categoryBudget)
                    .build();
        }


        float visitGap = Float.MAX_VALUE;
        float spendingGap = Float.MAX_VALUE;
        String title = null;
        String advice = null;

        // 방문 횟수 제한 미션
        if (!visitData.isEmpty()) {
//            Map.Entry<String, Float> targetVisit = sortedVisits.get(0);

            //  랜덤으로 하나 선택
            Random random = new Random();
            Map.Entry<String, Float> targetVisit = visitData.get(random.nextInt(visitData.size()));

            // 3️⃣ 선택된 장소의 정보 가져오기
            String place = targetVisit.getKey();
            float aver = targetVisit.getValue();
            float averageCost = spendingData.getOrDefault(place, 0f);

            float maxAllowedVisits = Math.max(0, realWeeklyCategoryBudget / averageCost); // 예산 내에서 최대 허용 방문 횟수

            float expectedVisitSpending = maxAllowedVisits * averageCost; // 방문 조정 후 예상 소비 금액
            float visitSpendingGap = Math.abs(realWeeklyCategoryBudget - expectedVisitSpending); // 허용 소비 금액과 차이 계산

            // 📌 ✅ 새로운 `advice`와 `title` 구성
            advice = "평균적으로 일주일에 " + String.format("%.1f", visits) + "회 / " + String.format("%,.0f", visits * averageCost) + "원 소비해요.\n"
                    + (int) maxAllowedVisits + "회 이하로 줄이는 것은 어떨까요?";

            title = place + (int) maxAllowedVisits + "회 이하 방문하기!";
            visitGap = visitSpendingGap;
        }

        // 금액 제한 미션
        if (!spendingData.isEmpty()) {
//            Map.Entry<String, Float> targetSpending = sortedSpending.get(0);
            // 1️⃣ 방문 횟수 1회 이상인 장소 필터링
            List<Map.Entry<String, Float>> eligibleSpendingList = spendingData.stream()
                    .filter(entry -> visitData.getOrDefault(entry.getKey(), 0f) >= 1.0) // 1회 이상 방문한 장소만 선택
                    .collect(Collectors.toList());

            if (!eligibleSpendingList.isEmpty()) {
                // 2️⃣ 상위 3개 중 랜덤 선택 (최대 3개까지만 가져오도록 제한)
                Random random = new Random();
                Map.Entry<String, Float> targetSpending = spendingData.get(random.nextInt(spendingData.size()));

                // 3️⃣ 선택된 장소의 정보 가져오기
                String place = targetSpending.getKey();
                float spending = targetSpending.getValue();
                Map<String, Float> visitData = getVisitDataByPayee(user, place, startDate,endDate);
                Float visits = visitData.get("avgWeeklyVisits");
                Float averageCost = visitData.get("avgSpendingPerVisit");

                float maxAllowedSpending = Math.max(realWeeklyCategoryBudget / 2, spending - (requiredSaving / 2)); // 허용된 범위 내에서 조절
                maxAllowedSpending = Math.min(spending, maxAllowedSpending); // 원래 소비 금액보다 크지 않도록 조절

                float spendingGapValue = Math.abs(realWeeklyCategoryBudget - maxAllowedSpending); // 허용 소비 금액과 차이 계산

                String spendingAdvice = "평균적으로 일주일에 " + String.format("%.1f", visits) + "회 / " + String.format("%,.0f", visits * averageCost) + "원 소비해요.\n"
                        + (int) maxAllowedSpending + "원 이하로 소비하는 것은 어떨까요?";

                String spendingTitle = place + "에서 " + (int) maxAllowedSpending + "원 이하로 소바하기!";
                spendingGap = spendingGapValue;

                // 🚀 방문 제한과 소비 제한 중 더 적절한 미션 선택
                if (spendingGap < visitGap) {
                    advice = spendingAdvice;
                    title = spendingTitle;
                }
            }
        }
        return new Mission(startDate, endDate, title, advice, categoryBudget);
    }


    /**
     * 특정 카테고리의 예산을 동기적으로 가져오기
     */
    private Long getUserCategoryBudget(User user, String category, LocalDate monthDate) {
        return categoryBudgetRepository.findCategoryBudgetByUserIdAndCategoryAndMonth(user.getUserId(), category, monthDate).getAmount(); // Returns 0 if no value is found
    }

    /**
     * 특정 카테고리의 방문 횟수 데이터를 동기적으로 가져오기
     */
    private List<Object[]> getTop3VisitDataByCategory(User user, String category, LocalDate startDate, LocalDate endDate) {
        List<Object[]> visitData = transactionRepository.findTop3VisitDataByCategory(user.getUserId(), category, startDate, endDate);
        return visitData; // null 체크 후 빈 맵 반환
    }

    /**
     * 특정 카테고리의 소비 데이터를 동기적으로 가져오기
     */
    private List<Object[]> getTop3SpendingDataByCategory(User user, String category, LocalDate startDate, LocalDate endDate) {
        List<Object[]> spendingData = transactionRepository.findTop3SpendingDataByCategory(user.getUserId(), category, startDate, endDate);
        return spendingData; // null 체크 후 빈 맵 반환
    }


    // 특정 결제처의 방문 횟수 & 금액 가져오기
    private Map<String, Float> getVisitDataByPayee(User user, String payee, LocalDate startDate, LocalDate endDate) {
        Object[] result = transactionRepository.findWeeklyVisitAndSpendingByPayee(user.getUserId(), payee, startDate, endDate);
        return convertToMap(result);
    }

    private Map<String, Float> convertToMap(Object[] result) {
        if (result == null) {
            return new HashMap<>();
        }

        Map<String, Float> map = new HashMap<>();
        map.put("avgWeeklyVisits", ((Number) result[1]).floatValue()); // 평균 주별 방문 횟수
        map.put("avgSpendingPerVisit", ((Number) result[2]).floatValue()); // 1회 방문당 평균 소비 금액
        return map;
    }



}
