package tamtam.mooney.domain.transaction.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tamtam.mooney.domain.enums.ExpenseCategory;
import tamtam.mooney.domain.transaction.entity.Transaction;
import tamtam.mooney.domain.user.entity.User;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // 특정 월의 총 지출 금액
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE TYPE(t) = Expense AND t.user = :user AND t.transactionTime BETWEEN :startDateTime AND :endDateTime")
    Long getTotalExpenseAmountForPeriod(User user, LocalDateTime startDateTime, LocalDateTime endDateTime);

    // 특정 월의 총 수입, 지출 금액
    @Query("""
    SELECT SUM(CASE WHEN TYPE(t) = Income THEN t.amount ELSE 0 END) AS totalIncome,
           SUM(CASE WHEN TYPE(t) = Expense THEN t.amount ELSE 0 END) AS totalExpense
    FROM Transaction t
    WHERE t.user = :user
    AND t.transactionTime BETWEEN :startOfMonth AND :endOfMonth
    GROUP BY t.transactionTime
    ORDER BY t.transactionTime
    """)
    List<Object[]> getTotalIncomeAndExpenseByMonth(@Param("user") User user,
                                                   @Param("startOfMonth") LocalDateTime startOfMonth,
                                                   @Param("endOfMonth") LocalDateTime endOfMonth);

    // 특정 날짜의 모든 트랜잭션(Expense + Income) 정보 조회
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.transactionTime BETWEEN :startTime AND :endTime ORDER BY t.transactionTime ASC")
    List<Transaction> findByUserAndTransactionTimeBetweenOrderByTransactionTime(User user, LocalDateTime startTime, LocalDateTime endTime);

    // 특정 날짜의 총 지출 금액
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE TYPE(t) = Expense AND t.user = :user AND t.transactionTime BETWEEN :startTime AND :endTime")
    Long getDailyTotalExpenseAmount(User user, LocalDateTime startTime, LocalDateTime endTime);

    // 특정 날짜의 총 수입, 지출 금액
    @Query("""
    SELECT CAST(t.transactionTime AS LocalDate),
           COALESCE(SUM(CASE WHEN TYPE(t) = Income THEN t.amount ELSE 0 END), 0),
           COALESCE(SUM(CASE WHEN TYPE(t) = Expense THEN t.amount ELSE 0 END), 0)
    FROM Transaction t
    WHERE t.user = :user
    AND t.transactionTime BETWEEN :startOfMonth AND :endOfMonth
    GROUP BY CAST(t.transactionTime AS LocalDate)
    ORDER BY CAST(t.transactionTime AS LocalDate)
""")
    List<Object[]> getDailyTotalIncomeAndExpenseByMonth(@Param("user") User user,
                                                        @Param("startOfMonth") LocalDateTime startOfMonth,
                                                        @Param("endOfMonth") LocalDateTime endOfMonth);


    /*
    * [
    {
        "payee": "스타벅스",
        "visitCount": 5,
        "avgSpending": 4500.0
    },
    {
        "payee": "맥도날드",
        "visitCount": 3,
        "avgSpending": 5200.0
    },
    {
        "payee": "버거킹",
        "visitCount": 2,
        "avgSpending": 6000.0
    }
]*/
    @Query("""
    SELECT e.payee, COUNT(t), AVG(t.amount)
    FROM Transaction t
    JOIN Expense e ON t.transactionId = e.transactionId
    WHERE t.user.userId = :userId
    AND e.expenseCategory = :category
    AND t.transactionTime BETWEEN :startDate AND :endDate
    GROUP BY e.payee
    ORDER BY COUNT(t) DESC
""")
    List<Object[]> findVisitDataByCategory(@Param("userId") Long userId,
                                           @Param("category") ExpenseCategory category,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate, Pageable pageable);


    /*
    * [
    {
        "payee": "스타벅스",
        "visitCount": 5,
        "avgSpending": 4500.0,
        "spending": 50000
    },
    {
        "payee": "맥도날드",
        "visitCount": 3,
        "avgSpending": 5200.0,
        * "spending": 50000
    },
    {
        "payee": "버거킹",
        "visitCount": 2,
        "avgSpending": 6000.0,
        "spending": 50000
    }
]
*/
    //소비 금액이 높은 곳이 최상위로 정렬
    @Query("""
    SELECT e.payee, COUNT(t), AVG(t.amount), SUM(t.amount)
    FROM Transaction t
    JOIN Expense e ON t.transactionId = e.transactionId
    WHERE t.user.userId = :userId
    AND e.expenseCategory = :category
    AND t.transactionTime BETWEEN :startDate AND :endDate
    GROUP BY e.payee
    ORDER BY SUM(t.amount) DESC
""")
    List<Object[]> findSpendingDataByCategory(@Param("userId") Long userId,
                                              @Param("category") ExpenseCategory category,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate, Pageable pageable);



    @Query("""
    SELECT e.payee, 
           COUNT(t) / COUNT(DISTINCTFUNCTION('WEEK', t.transactionTime)) AS avgWeeklyVisits,
           AVG(t.amount) AS avgSpendingPerVisit
    FROM Transaction t
    JOIN Expense e ON t.transactionId = e.transactionId
    WHERE t.user.userId = :userId
    AND e.payee = :payee
    AND t.transactionTime BETWEEN :startDate AND :endDate
    GROUP BY e.payee
""")
    Object[] findWeeklyVisitAndSpendingByPayee(@Param("userId") Long userId,
                                               @Param("payee") String payee,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);



    // 특정 월의 특정 카테고리 총 지출 금액
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user = :user " +
            "AND e.transactionTime BETWEEN :startOfMonth AND :endOfMonth " +
            "AND e.expenseCategory = :category")
    Optional<Long> getTotalCategoryExpenseAmountForMonth(@Param("user") User user,
                                                         @Param("category") ExpenseCategory category,
                                                         @Param("startOfMonth") LocalDateTime startOfMonth,
                                                         @Param("endOfMonth") LocalDateTime endOfMonth);




    @Query(value = """
    SELECT e.expense_category AS item_id, 
           date_trunc('week', t.transaction_time) AS week_start, 
           SUM(t.amount) AS target 
    FROM transaction t 
    JOIN expense e ON t.transaction_id = e.transaction_id
    WHERE t.user_id = :userId 
      AND e.expense_category IN :validCategories 
      AND t.transaction_time >= :startDate 
    GROUP BY e.expense_category, date_trunc('week', t.transaction_time) 
    ORDER BY date_trunc('week', t.transaction_time) ASC
    """, nativeQuery = true)
    List<Map<String, Object>> findWeeklyAggregatedTransactions(@Param("userId") Long userId,
                                                               @Param("validCategories") List<Integer> validCategories,
                                                               @Param("startDate") LocalDateTime startDate);

}

