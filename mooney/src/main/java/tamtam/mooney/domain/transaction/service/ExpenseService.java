package tamtam.mooney.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.mission.service.MissionService;
import tamtam.mooney.domain.transaction.dto.BaseExpenseRequest;
import tamtam.mooney.domain.transaction.dto.ExpenseAddRequestDto;
import tamtam.mooney.domain.transaction.dto.ExpenseWithCategoryAddRequestDto;
import tamtam.mooney.domain.transaction.entity.Expense;
import tamtam.mooney.domain.enums.ExpenseCategory;
import tamtam.mooney.domain.transaction.repository.ExpenseRepository;
import tamtam.mooney.domain.transaction.repository.TransactionRepository;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;
import tamtam.mooney.global.openai.OpenAIOptionEnum;
import tamtam.mooney.global.openai.OpenAIService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ExpenseService {

    private final TransactionRepository transactionRepository;
    private final ExpenseRepository expenseRepository;
    private final UserService userService;
    private final MissionService missionService;
    private final OpenAIService openAIService;

    // 정규식 패턴 → 카테고리 매핑 리스트
    private final List<PatternCategory> patternCategoryList = List.of(
            // 카페 / 간식
            new PatternCategory(Pattern.compile("(?i).*(쏘머럽|투썸플레이스|스타벅스|컴포즈|커피|페이브).*"),
                    ExpenseCategory.CAFE_SNACKS),
            // 식비
            new PatternCategory(Pattern.compile("(?i).*(떡볶이|김밥|돈까스|식당|파스타|국수|써브웨이|밥).*"),
                    ExpenseCategory.FOOD),
            // 교통
            new PatternCategory(Pattern.compile("(?i).*(티머니|택시|버스|지하철).*"),
                    ExpenseCategory.TRANSPORTATION),
            // 온라인쇼핑
            new PatternCategory(Pattern.compile("(?i).*(쿠팡|G마켓|11번가|티몬|위메프|배송|주문|네이버쇼핑).*"),
                    ExpenseCategory.ONLINE_SHOPPING),

            // 생활 (편의점·마트)
            new PatternCategory(Pattern.compile("(?i).*(이마트|홈플러스|롯데마트|GS25|CU|세븐일레븐).*"),
                    ExpenseCategory.LIVING),

            // 뷰티 / 미용
            new PatternCategory(Pattern.compile("(?i).*(올리브영|뷰티|화장품|헤어|네일).*"),
                    ExpenseCategory.BEAUTY_CARE),

            // 패션 / 쇼핑
            new PatternCategory(Pattern.compile("(?i).*(현대백화점|롯데백화점|무신사|패션|의류).*"),
                    ExpenseCategory.FASHION_SHOPPING),

            // 교육 / 학습
            new PatternCategory(Pattern.compile("(?i).*(교보문고|아카데미|학원|교육).*"),
                    ExpenseCategory.EDUCATION),

            // 문화 / 여가
            new PatternCategory(Pattern.compile("(?i).*(CGV|메가박스|영화|전시|공연|문화).*"),
                    ExpenseCategory.CULTURE_LEISURE),

            // 여행 / 숙박
            new PatternCategory(Pattern.compile("(?i).*(아고다|호텔|모텔|게스트하우스|여행|숙박).*"),
                    ExpenseCategory.TRAVEL_ACCOMMODATION),

            // 의료 / 건강
            new PatternCategory(Pattern.compile("(?i).*(병원|약국|의원|치과|외과|내과).*"),
                    ExpenseCategory.HEALTHCARE),

            // 금융
            new PatternCategory(Pattern.compile("(?i).*(카카오페이|토스|네이버페이|페이코|이체|입금|출금).*"),
                    ExpenseCategory.FINANCE)
    );


    // 지출 추가 (정규식 기반 카테고리 매핑)
    public String createExpense(ExpenseAddRequestDto request) {
        User user = userService.getCurrentUser();
        String payee = request.payee();
        String prompt = String.format("%s 가 어떤 지출처인지 파악해주세요.", payee);

        ExpenseCategory category = patternCategoryList.stream()
                .filter(pc -> pc.pattern.matcher(payee).matches())
                .map(pc -> pc.category)
                .findFirst()
                .orElseGet(() -> {
                    String code = openAIService.generateGPTResponse(prompt, OpenAIOptionEnum.BALANCED);
                    return ExpenseCategory.fromCode(Integer.parseInt(code));
                });
        return saveAndReturnCategory(request, user, category);
    }

    private static class PatternCategory {
        final Pattern pattern;
        final ExpenseCategory category;
        PatternCategory(Pattern pattern, ExpenseCategory category) {
            this.pattern = pattern;
            this.category = category;
        }
    }

    // 지출 추가 (카테고리 직접 지정)
    public String createExpenseWithCategory(ExpenseWithCategoryAddRequestDto request) {
        User user = userService.getCurrentUser();
        return saveAndReturnCategory(request, user, request.expenseCategory());
    }

    // ——— private 헬퍼 메서드 ———
    private <T extends BaseExpenseRequest> String saveAndReturnCategory(
            T request,
            User user,
            ExpenseCategory category
    ) {
        Expense expense = Expense.builder()
                .payee(request.payee())
                .expenseCategory(category)
                .amount(request.amount())
                .transactionTime(request.transactionTime())
                .transactionSource(request.transactionSource())
                .sourceApp(request.sourceApp())
                .user(user)
                .build();

        transactionRepository.save(expense);
        missionService.updateMission(user, request.payee(), request.amount());
        return category.name();
    }

    @Transactional(readOnly = true)
    public Long getTotalExpenseAmountForPeriod(User user, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        return transactionRepository.getTotalExpenseAmountForPeriod(user, startDateTime, endDateTime);
    }

    @Transactional(readOnly = true)
    public Map<ExpenseCategory, Long> mapTotalExpenseForAllCategories(User user, LocalDate startDate) {
        LocalDateTime startOfMonth = startDate.atStartOfDay();
        LocalDateTime endOfMonth = startDate.withDayOfMonth(startDate.lengthOfMonth()).atTime(23, 59, 59);
        log.info("startOfMonth: " + startOfMonth + " / endOfMonth: " + endOfMonth);

        // List<Object[]>로 쿼리 결과 받기
        List<Object[]> rawResults = expenseRepository.getTotalExpenseForAllCategories(user, startOfMonth, endOfMonth);

        // 수동 변환: Map<ExpenseCategory, Long>
        Map<ExpenseCategory, Long> result = new HashMap<>();
        for (Object[] row : rawResults) {
            ExpenseCategory category = (ExpenseCategory) row[0];
            Long amount = (Long) row[1];
            result.put(category, amount);
        }

        // 전체 결과 출력
        result.forEach((key, value) ->
                log.info("카테고리: " + key.name() + " / 총 지출: " + value)
        );

        return result;
    }


}
