package tamtam.mooney.domain.mission.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import tamtam.mooney.domain.budget.entity.CategoryBudget;
import tamtam.mooney.domain.budget.repository.CategoryBudgetRepository;
import tamtam.mooney.domain.budget.repository.MonthlyBudgetRepository;
import tamtam.mooney.domain.enums.ExpenseCategory;
import tamtam.mooney.domain.enums.MissionType;
import tamtam.mooney.domain.mission.dto.MissionDto;
import tamtam.mooney.domain.mission.entity.Mission;
import tamtam.mooney.domain.mission.repository.MissionRepository;
import tamtam.mooney.domain.transaction.repository.TransactionRepository;
import tamtam.mooney.domain.user.dto.UserHomeWeeklyMissionDto;
import tamtam.mooney.domain.user.entity.User;
import org.springframework.core.ParameterizedTypeReference;
import tamtam.mooney.domain.user.service.UserService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    private static final String FASTAPI_URL = "https://mooney-ai.o-r.kr/predict"; // FastAPI URL
    private final UserService userService;


    // 저장해놓은 미션 가져오기(홈)
    public List<UserHomeWeeklyMissionDto> getWeeklyMissions(LocalDate today) {
        User user = userService.getCurrentUser();
        List<Mission> missions = missionRepository.findWeeklyMissionsByUser(user.getUserId(), today);
        updateMissionResult(today);

        return missions.stream()
                .map(mission -> new UserHomeWeeklyMissionDto(
                        mission.getTitle(),
                        mission.getResult()))
                .collect(Collectors.toList());
    }

    // 저장해놓은 미션 가져오기(미션탭)
    public List<MissionDto> getWeeklyMissionsDetail(LocalDate today) {
        User user = userService.getCurrentUser();
        List<Mission> missions = missionRepository.findWeeklyMissionsByUser(user.getUserId(), today);
        updateMissionResult(today);

        return missions.stream()
                .map(mission -> new MissionDto(
                        mission.getTitle(),
                        mission.getAdvice(),
                        mission.getResult(),
                        mission.getNumOfExpense(),
                        mission.getAmountOfExpense()))
                .collect(Collectors.toList());
    }

    // 미션 데이터(방문 횟수, 사용한 금액) 업데이트 - expense 발생할 때마다 미션에 해당하는지 보고 미션에 해당되면 추적해서 저장
    public void updateMission(User user, String payee, long amount){
        LocalDate today = LocalDate.now();
        List<String> missionPlaces = missionRepository.findWeeklyMissionPlacesByUser(user.getUserId(), today);
        if (missionPlaces.contains(payee)) {
            // missionPlaces에 포함된 경우의 처리
            Mission mission = missionRepository.findMissionByPlace(payee);
            //해당 미션의 numOfExpense와 amountOfExpense에 더하기
            mission.addExpense(amount);
        }
    }

    //미션 상태 업데이트
    public float updateMissionResult(LocalDate today){
        User user = userService.getCurrentUser();
        List<Mission> missions = missionRepository.findWeeklyMissionsByUser(user.getUserId(), today);
        int currentDayOfWeek = today.getDayOfWeek().getValue();
        float sum = 0;
        System.out.println("오늘 요일: " + currentDayOfWeek);
        for(Mission mission : missions){
            System.out.println("미션 종류: " + mission.getMissionType());
            System.out.println("소비액: " + mission.getAmountOfExpense() + ", 방문횟수: " + mission.getNumOfExpense());

            float result = 0;
            if(mission.getMissionType().equals("VISIT")){ //방문 기반 미션
                result = calculateVisitMissionScore(mission.getMax(), mission.getNumOfExpense(), currentDayOfWeek);
            }
            else{
                result = calculateSpendingMissionScore(mission.getMax(), mission.getAmountOfExpense(), currentDayOfWeek);
            }

            mission.updateResult(result);
            sum += result;
        }
        float totalAvg = sum / missions.size();


        return totalAvg;
    }

    /**
     * 방문 기반 미션 점수 계산 (예: "카페 앨리스 이번 주 쉬어가기")
     * 기본 점수는 3.0점이며, 현재 방문 횟수와 예상 방문 횟수, 그리고 최종 예측 방문 횟수를 기반으로 점수를 조정합니다.
     *
     * @param maxVisitsAllowed 미션에서 정한 최대 방문 횟수
     * @param currentVisits 현재까지의 방문 횟수
     * @param currentDayOfWeek 현재 요일 (1: 월요일 ~ 7: 일요일)
     * @return 1 ~ 5 사이의 미션 점수
     */
    public float calculateVisitMissionScore(long maxVisitsAllowed, long currentVisits, int currentDayOfWeek) {
        float baseScore = 3.0f;
        float weekProgress = currentDayOfWeek / 7.0f;
        float expectedVisits = maxVisitsAllowed * weekProgress;
        float predictedVisits = (weekProgress > 0) ? (currentVisits / weekProgress) : currentVisits;

        // ✅ 첫날 & 아무 행동 없을 때만 3.0 고정
        if (currentDayOfWeek == 1 && currentVisits == 0) {
            return baseScore;
        }

        float adjustment = 0.0f;

        if (currentVisits < expectedVisits && expectedVisits > 0) {
            float diffRatio = (expectedVisits - currentVisits) / expectedVisits;
            adjustment = 2.0f * diffRatio;
        } else if (currentVisits > expectedVisits) {
            float diff = currentVisits - expectedVisits;
            adjustment = -1.5f; // 감점은 고정으로
        }

        if (predictedVisits > maxVisitsAllowed) {
            float overageRatio = (predictedVisits - maxVisitsAllowed) / maxVisitsAllowed;
            adjustment -= 1.5f * overageRatio;
        }

        return clamp(baseScore + adjustment, 1.0f, 5.0f);
    }


    /**
     * 소비 기반 미션 점수 계산 (예: "이번 주 올리브영에서 10,000원 이하 소비")
     * 기본 점수는 3.0점이며, 현재 소비액과 예상 소비액, 그리고 최종 예측 소비액을 기반으로 점수를 조정합니다.
     *
     * @param maxSpendingAllowed 미션에서 허용한 최대 소비액
     * @param currentSpending 현재까지의 소비액
     * @param currentDayOfWeek 현재 요일 (1: 월요일 ~ 7: 일요일)
     * @return 1 ~ 5 사이의 미션 점수
     */
    public float calculateSpendingMissionScore(float maxSpendingAllowed, float currentSpending, int currentDayOfWeek) {
        float baseScore = 3.0f;
        float weekProgress = currentDayOfWeek / 7.0f;
        float expectedSpending = maxSpendingAllowed * weekProgress;
        float predictedSpending = (weekProgress > 0) ? (currentSpending / weekProgress) : currentSpending;

        // ✅ 첫날 & 소비가 0일 때만 3.0 고정
        if (currentDayOfWeek == 1 && currentSpending == 0) {
            return baseScore;
        }

        float adjustment = 0.0f;

        if (currentSpending < expectedSpending && expectedSpending > 0) {
            float diffRatio = (expectedSpending - currentSpending) / expectedSpending;
            adjustment = 2.0f * diffRatio;
        } else if (currentSpending > expectedSpending) {
            float diff = currentSpending - expectedSpending;
            adjustment = -1.5f; // 고정 감점
        }

        if (predictedSpending > maxSpendingAllowed) {
            float overageRatio = (predictedSpending - maxSpendingAllowed) / maxSpendingAllowed;
            adjustment -= 1.5f * overageRatio;
        }

        return clamp(baseScore + adjustment, 1.0f, 5.0f);
    }


    /**
     * 점수를 주어진 범위 내로 클램핑하는 유틸리티 메서드
     *
     * @param value 입력 값
     * @param min 최소값
     * @param max 최대값
     * @return min과 max 사이로 조절된 값
     */
    private float clamp(float value, float min, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }




    //# FastAPI 서버에서 카테고리 및 예상 지출 금액을 가져와 현재 주별 예산과 비교하는 동기 메서드(1~3 포함)
    // => 지출 > 예상인 카테고리에 한해 카테고리, realWeeklyCategoryBudget, predictedSpending을 return
     private List<Map<String, Object>> getSelectedCategories(User user) {
         // FastAPI에서 예상 지출 데이터 가져오기 (동기 방식)
         List<Map<String, Object>> categoryDataList = fetchPredictedSpending(user);

         // 2주별 예산과 비교하여 데이터 반환
         return compareWithWeeklyBudget(user, categoryDataList);
     }


    //1. FastAPI에서 다음주 예상 지출 데이터 가져오기
    private List<Map<String, Object>> fetchPredictedSpending(User user) {
        // 백엔드에서 12주 집계 데이터를 가져옴
        List<Map<String, Object>> aggregatedData = fetchAggregatedWeeklyData(user);

        Map<String, Object> requestBody = Map.of("data", aggregatedData);

        Map<String, Object> response = webClient.post()
                .uri(FASTAPI_URL)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        System.out.println("Received Response from FastAPI: " + response);

        // "predict_results" 키에서 실제 데이터를 추출하여 리스트로 변환
        List<Map<String, Object>> predictResults = (List<Map<String, Object>>) response.get("predict_results");

        // String을 ExpenseCategory Enum으로 변환
        List<Map<String, Object>> convertedResults = predictResults.stream().map(entry -> {
            Map<String, Object> newEntry = new HashMap<>(entry);
            String categoryStr = (String) entry.get("Category"); // 🔥 FastAPI에서 온 문자열

            try {
                ExpenseCategory categoryEnum = ExpenseCategory.valueOf(categoryStr); // 🔥 Enum 변환
                newEntry.put("Category", categoryEnum); // 🔄 변환된 Enum 저장
            } catch (IllegalArgumentException e) {
                System.out.println("🚨 Warning: Invalid category received - " + categoryStr);
            }

            return newEntry;
        }).collect(Collectors.toList());

        System.out.println("Converted Prediction Results: " + convertedResults);
        return convertedResults;
    }

    private List<Map<String, Object>> fetchAggregatedWeeklyData(User user) {
        // 유효 카테고리 문자열 목록
        List<String> validCategories = Arrays.asList(
                "FOOD", "CAFE_SNACKS", "TRANSPORTATION", "ONLINE_SHOPPING",
                "ALCOHOL_ENTERTAINMENT", "LIVING", "FASHION_SHOPPING", "BEAUTY_CARE",
                "CULTURE_LEISURE", "TRAVEL_ACCOMMODATION"
        );

        // 문자열 목록을 enum의 ordinal 값(List<Integer>)로 변환
        List<Integer> validCategoryOrdinals = validCategories.stream()
                .map(cat -> ExpenseCategory.valueOf(cat).ordinal())
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        // 현재 주의 시작일(월요일 기준)
        LocalDate currentWeekStart = today.with(DayOfWeek.MONDAY);
        // 최근 12주: 현재 주 포함하여 11주 전부터
        LocalDate startWeek = currentWeekStart.minusWeeks(11);
        LocalDateTime startDateTime = startWeek.atStartOfDay();

        // 쿼리를 통해 최근 12주 내 실제 거래가 있는 주에 대한 집계 데이터 조회
        List<Map<String, Object>> rawData = transactionRepository.findWeeklyAggregatedTransactions(
                user.getUserId(), validCategoryOrdinals, startDateTime
        );

        List<Map<String, Object>> formattedResults = new ArrayList<>();
        // 결과로 반환되는 week_start는 "yyyy-MM-dd HH:mm:ss" 형식의 문자열일 수 있으므로 포맷터 준비
        DateTimeFormatter outputFormatter  = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Map<String, Object> entry : rawData) {
            // item_id는 정수(ordinal)로 반환됨 → 이를 다시 enum 이름으로 변환
            int ordinal = ((Number) entry.get("item_id")).intValue();
            String categoryName = ExpenseCategory.values()[ordinal].name();

            // week_start 값을 문자열로 받아서 LocalDate로 변환
            String tsStr = entry.get("week_start").toString();
            LocalDate weekStartDate;
            try {
                // 포맷에 맞게 파싱
                weekStartDate = LocalDate.parse(tsStr.substring(0, 10)); // 앞 10자리 (yyyy-MM-dd)
            } catch (DateTimeParseException e) {
                continue; // 파싱 오류가 발생하면 해당 데이터는 무시
            }
            // FastAPI가 기대하는 JSON 구조로 변환
            Map<String, Object> formattedEntry = new HashMap<>();
            formattedEntry.put("timestamp", weekStartDate.format(outputFormatter)); // "yyyy-MM-dd" 형식
            formattedEntry.put("amount", entry.get("target")); // 총 소비 금액
            formattedEntry.put("expense_category", categoryName); // 카테고리 이름

            formattedResults.add(formattedEntry);
        }
        return formattedResults;
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
        List<Map<String, Object>> notSelectedResult = new ArrayList<>(); //카테고리가 3개 미만으로 뽑히는 상황 위해
        System.out.println("compareWithWeeklyBudget\n\n");
        System.out.println("categoryDataList: " + categoryDataList);
        for (Map<String, Object> categoryData : categoryDataList) {
            ExpenseCategory category = (ExpenseCategory) categoryData.get("Category");
            Object predictedSpendingObj = categoryData.get("yhat_adjusted");
            Long predictedSpending;
            if (predictedSpendingObj instanceof Number) {
                predictedSpending = ((Number) predictedSpendingObj).longValue();
            } else {
                System.out.println("⚠️ 예상 소비값이 누락되었거나 올바르지 않습니다: " + predictedSpendingObj + " (카테고리: " + category + ")");
                predictedSpending = 100000L;
            }

            long realWeeklyCategoryBudget = calculateWeeklyBudget(user, category);

            Map<String, Object> categoryBudgetInfo = new HashMap<>();
            categoryBudgetInfo.put("Category", category);
            categoryBudgetInfo.put("WeeklyBudget", realWeeklyCategoryBudget);
            categoryBudgetInfo.put("PredictedSpending", predictedSpending);

            if (realWeeklyCategoryBudget < predictedSpending) {
                result.add(categoryBudgetInfo);
            }
            else{
                notSelectedResult.add(categoryBudgetInfo);
            }

        }
        if (result.size() < 3) {
            notSelectedResult.sort(Comparator.comparingLong((Map<String, Object> cat) -> {
                Object predictedSpendingObj = cat.get("PredictedSpending");
                Object weeklyBudgetObj = cat.get("WeeklyBudget");

                // 🔹 null 체크 및 기본값 설정
                long predictedSpending = (predictedSpendingObj instanceof Number) ? ((Number) predictedSpendingObj).longValue() : 0L;
                long weeklyBudget = (weeklyBudgetObj instanceof Number) ? ((Number) weeklyBudgetObj).longValue() : 0L;

                return predictedSpending - weeklyBudget; // 예산 초과 정도가 큰 순으로 정렬 (내림차순)
            }).reversed()); //  내림차순 정렬 적용

            int i = 0;
            while (result.size() < 3 && i < notSelectedResult.size()) {
                result.add(notSelectedResult.get(i));
                i++;
            }
        }

        return result;

    }

    //3.
    private Long calculateWeeklyBudget(User user, ExpenseCategory category) {
        LocalDate today = LocalDate.now();
        LocalDateTime monthDate = today.withDayOfMonth(1).atStartOfDay(); // 해당 월의 첫째 날

        // 1️⃣ 현재 월의 전체 주 수 계산
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int totalWeeks = today.with(TemporalAdjusters.lastDayOfMonth()).get(weekFields.weekOfMonth());

        // 2️⃣ 오늘이 해당 월의 몇 번째 주인지 계산
        int currentWeek = today.get(weekFields.weekOfMonth());

        // 3️⃣ 남은 주 수 계산 (현재 주 포함)
        int remainingWeeks = totalWeeks - currentWeek + 1;

        // 4️⃣ 예산과 지출을 가져와서 연산 수행
        //예산
        Long categoryBudgetAmount = getUserCategoryBudgetAmount(user, category, monthDate.toLocalDate());

        //지출
        Long totalExpense = transactionRepository.getTotalCategoryExpenseAmountForMonth(user, category, monthDate, LocalDateTime.now()).orElse(0L);
        return (categoryBudgetAmount - totalExpense) / remainingWeeks;
    }



     //유저의 이번 주 미션을 생성하여 반환 (동기)
    /**
     * "Category", category,
     * "WeeklyBudget", realWeeklyCategoryBudget,
     * "PredictedSpending", predictedSpending
     **/
    @Transactional(readOnly = false)
    public List<String> generateWeeklyMissions(User user, LocalDate missionStartDate) {
        //User user = userService.getCurrentUser();
        long userId = user.getUserId();

        // 현재 날짜 관련 변수 미리 선언
        LocalDate now = LocalDate.now();
        LocalDate currentMonthStart = now.withDayOfMonth(1);
        LocalDateTime startDate = now.minusMonths(2).atStartOfDay(); // 두 달 전부터 조회
        LocalDateTime endDate = LocalDateTime.now();

        // 디버깅 로그 (실제 서비스에서는 logger 사용 권장)
        System.out.println("Current Month 시작 : " + currentMonthStart.format(DateTimeFormatter.ISO_LOCAL_DATE));

        List<Map<String, Object>> categoryDataList = getSelectedCategories(user);

        List<String> missionTitles = new ArrayList<>();

        System.out.println("categoryDataList 길이" + categoryDataList.size());
        for (Map<String, Object> categoryData : categoryDataList) {
            System.out.println(categoryData);

            // ExpenseCategory 추출 (null이면 예외)
            ExpenseCategory category = extractCategory(categoryData);

            // PredictedSpending 추출 (없으면 기본값 0L)
            Long predictedSpending = extractPredictedSpending(categoryData);

            // ✅ 하한선 적용 (예: 최소 1,000원)
            long minPredictedSpending = 1000L;
            predictedSpending = Math.max(predictedSpending, minPredictedSpending);


            // realWeeklyCategoryBudget 계산
            Long realWeeklyCategoryBudget = calculateWeeklyBudget(user, category);
            System.out.println("realWeeklyCategoryBudget: " + realWeeklyCategoryBudget);

            System.out.println(userId + " " + category + " " + currentMonthStart);

            // 사용자 카테고리 예산 조회
            CategoryBudget categoryBudget = getUserCategoryBudget(userId, category, currentMonthStart);

            //다음주 미션이 이미 있으면 생성 안되게
            checkUserHasNextWeekMission(userId, now);

            // 미션 생성
            Mission mission = generateCategoryMission( //여기서 missionReposiotry에 save
                    getTop3VisitDataByCategory(user, category, startDate, endDate),
                    getTop3SpendingDataByCategory(user, category, startDate, endDate),
                    realWeeklyCategoryBudget,
                    predictedSpending,
                    categoryBudget,
                    missionStartDate
            );

            if(mission == null) {
                throw new IllegalStateException("미션 생성 실패: category=" + category);
            }
            missionTitles.add(mission.getTitle());

        }
        return missionTitles;
    }

    private void checkUserHasNextWeekMission(long userId, LocalDate now) {
        LocalDate nextWeekStart = now.plusWeeks(1).with(java.time.DayOfWeek.MONDAY); // 다음 주 월요일
        LocalDate nextWeekEnd = nextWeekStart.plusDays(6);
        long nextWeekMissionCount = missionRepository.countNextWeekMissionsByUser(userId, nextWeekStart, nextWeekEnd);

        if (nextWeekMissionCount > 0) {
            throw new IllegalStateException("다음 주 미션이 이미 존재합니다. userId=" + userId);
        }
    }

    // 헬퍼 메서드: ExpenseCategory 추출
    private ExpenseCategory extractCategory(Map<String, Object> categoryData) {
        Object categoryObj = categoryData.get("Category");
        if (categoryObj == null) {
            throw new IllegalArgumentException("Category 값이 null입니다.");
        }
        return (ExpenseCategory) categoryObj;
    }

    // 헬퍼 메서드: PredictedSpending 추출
    private Long extractPredictedSpending(Map<String, Object> categoryData) {
        Object predictedSpendingObj = categoryData.get("PredictedSpending");
        if (predictedSpendingObj != null) {
            return ((Number) predictedSpendingObj).longValue();
        } else {
            System.out.println("Warning: PredictedSpending 값이 null입니다. 기본값 0L 사용");
            return 0L;
        }
    }



    /**
     * 특정 카테고리에 대한 미션 생성
     * ex.
     * title: CU 편의점 이번주에 3회 이하 가기,
     * title2: 스타벅스에서 이번주 15000원 이하 쓰기
     * advice: 평균적으로 일주일에 3.5회 / xxxxx원 마셔요. \n 2회 이하로 마시는 것은 어떨까요?
     */
    private Mission generateCategoryMission(List<Object[]> visitData,
                                            List<Object[]> spendingData,
                                            long realWeeklyCategoryBudget,
                                            long expectedSpending,
                                            CategoryBudget categoryBudget, LocalDate startDate) {
        long requiredSaving = expectedSpending - realWeeklyCategoryBudget; // 줄여야 하는 금액
        //LocalDate startDate = LocalDate.now().plusDays(1); // 미션 시작일 (예: 내일)
        LocalDate endDate = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));   // 미션 종료일 (startDate의 주의 일요일)

        Mission visitMission = null;
        Mission spendingMission = null;
        float visitGap = Float.MAX_VALUE;
        float spendingGap = Float.MAX_VALUE;

        // 방문 제한 미션 생성 (visitData가 있을 경우)
        if (!visitData.isEmpty()) {
            Random random = new Random();
            Object[] targetVisit = visitData.get(random.nextInt(visitData.size()));

            // 데이터 추출
            String place = (String) targetVisit[0];                  // 예: 상점 이름
            float averageVisit = ((Number) targetVisit[1]).floatValue();  // 평균 방문 횟수
            float averageCost = ((Number) targetVisit[2]).floatValue();   // 평균 소비 금액

            long maxAllowedVisits = 0;
            if (averageCost != 0) {
                maxAllowedVisits = (long) Math.max(1, (realWeeklyCategoryBudget * 0.8) / averageCost);
            }

            float expectedVisitSpending = maxAllowedVisits * averageCost; // 방문 제한 후 예상 소비 금액
            visitGap = Math.abs(realWeeklyCategoryBudget - expectedVisitSpending); // 예산과의 차이

            String advice = "평균적으로 일주일에 " + String.format("%.1f", averageVisit) + "회 / " +
                    String.format("%,.0f", averageCost) + "원 소비해요.\n 이번 주는 방문횟수를 " +
                    (int) maxAllowedVisits + "회 이하로 줄여볼까요?";
            String title = place +"를 "+ (int) maxAllowedVisits + "회 이하 소비하기!";

            visitMission = new Mission(MissionType.VISIT, startDate, endDate, title, place, advice, categoryBudget, maxAllowedVisits);
            System.out.println("Mission advice before saving: " + visitMission.getAdvice());
        }

        // 소비 제한 미션 생성 (spendingData가 있을 경우)
        if (!spendingData.isEmpty()) {
            Random random = new Random();
            Object[] targetSpending = spendingData.get(random.nextInt(spendingData.size()));

            // 데이터 추출
            String spendingPlace = (String) targetSpending[0];              // 소비 장소
            float averageVisit = ((Number) targetSpending[1]).floatValue();   // 평균 방문 횟수
            float averageCost = ((Number) targetSpending[2]).floatValue();    // 평균 소비 금액
            float spending = ((Number) targetSpending[3]).floatValue();       // 총 소비 금액

            // 소비 제한 계산
            long maxAllowedSpending = (long) Math.max(realWeeklyCategoryBudget * 0.8, spending - (requiredSaving * 0.9));
            maxAllowedSpending = (long) Math.min(spending, maxAllowedSpending);
            spendingGap = Math.abs(realWeeklyCategoryBudget - maxAllowedSpending);

            String spendingAdvice = "평균적으로 일주일에 " + String.format("%.1f", averageVisit) + "회 지출하고, 지출할 때마다 " +
                    String.format("%,.0f", averageCost) + "원 소비해요.\n 이번 주는 " +
                    (int) maxAllowedSpending + "원 이하로 소비하는 것은 어떨까요?";
            String spendingTitle = spendingPlace + "에서 " + (int) maxAllowedSpending + "원 이하로 소비하기!";

            spendingMission = new Mission(MissionType.EXPENSE, startDate, endDate, spendingTitle, spendingPlace, spendingAdvice, categoryBudget, maxAllowedSpending);
            System.out.println("Mission advice before saving: " + spendingMission.getAdvice());
        }

        // 두 미션 중 더 적합한 것을 선택하거나, 하나만 존재하는 경우 해당 미션 반환
        Mission missionToSave = null;
        if (visitMission != null && spendingMission != null) {
            missionToSave = (spendingGap < visitGap) ? spendingMission : visitMission;
        } else if (spendingMission != null) {
            missionToSave = spendingMission;
        } else if (visitMission != null) {
            missionToSave = visitMission;
        } else {
            // 두 데이터 모두 없는 경우 예외 발생 (또는 원하는 기본 동작 적용)
            throw new IllegalStateException("카테고리 미션 생성에 필요한 데이터가 없습니다.");
        }

        Mission savedMission = missionRepository.save(missionToSave);
        return savedMission;
    }


    /**
     * 특정 카테고리의 예산을 가져오기
     */
    private Long getUserCategoryBudgetAmount(User user, ExpenseCategory category, LocalDate monthDate) {
        return categoryBudgetRepository.findCategoryBudgetByUserIdAndExpenseCategoryAndMonth(user.getUserId(), category, monthDate).getAmount(); // Returns 0 if no value is found
    }
    private CategoryBudget getUserCategoryBudget(Long userId, ExpenseCategory category, LocalDate monthDate) {
        return categoryBudgetRepository.findCategoryBudgetByUserIdAndExpenseCategoryAndMonth(userId, category, monthDate); // Returns 0 if no value is found
    }

    /**
     * 특정 카테고리의 방문 횟수 데이터를 동기적으로 가져오기
     */
    private List<Object[]> getTop3VisitDataByCategory(User user, ExpenseCategory category, LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> top3Visits  = transactionRepository.findVisitDataByCategory(user.getUserId(), category, startDate, endDate, (Pageable) PageRequest.of(0, 3));

        return top3Visits; // null 체크 후 빈 맵 반환
    }

    /**
     * 특정 카테고리의 소비 데이터를 동기적으로 가져오기
     */
    private List<Object[]> getTop3SpendingDataByCategory(User user, ExpenseCategory category, LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> top3SpendingData = transactionRepository.findSpendingDataByCategory(user.getUserId(), category, startDate, endDate, (org.springframework.data.domain.Pageable) PageRequest.of(0, 3));

        return top3SpendingData; // null 체크 후 빈 맵 반환
    }


}
