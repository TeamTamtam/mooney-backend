package tamtam.mooney.domain.transaction.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import reactor.core.publisher.Mono;
import tamtam.mooney.domain.transaction.entity.Transaction;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // 특정 월의 총 지출 금액
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE TYPE(t) = Expense AND t.user = :user AND t.transactionTime BETWEEN :startOfMonth AND :endOfMonth")
    Long getTotalExpenseAmountForMonth(User user, LocalDateTime startOfMonth, LocalDateTime endOfMonth);

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


    @Query("""
    SELECT e.payee, COUNT(t), AVG(t.amount)
    FROM Transaction t
    JOIN Expense e ON t.transactionId = e.transactionId
    WHERE t.user.userId = :userId
    AND e.expenseCategory = :category
    AND t.transactionTime BETWEEN :startDate AND :endDate
    GROUP BY e.payee
    ORDER BY COUNT(t) DESC
    LIMIT 3
""")
    List<Object[]> findTop3VisitDataByCategory(@Param("userId") Long userId,
                                           @Param("category") String category,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);


    @Query("""
    SELECT e.expenseCategory, SUM(t.amount)
    FROM Transaction t
    JOIN Expense e ON t.transactionId = e.transactionId
    WHERE t.user.userId = :userId
    AND e.expenseCategory = :category
    AND t.transactionTime BETWEEN :startDate AND :endDate
    GROUP BY e.expenseCategory
    ORDER BY COUNT(t) DESC
    LIMIT 3
""")
    List<Object[]> findTop3SpendingDataByCategory(@Param("userId") Long userId,
                                              @Param("category") String category,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);



    @Query("""
    SELECT e.payee, 
           COUNT(t) / COUNT(DISTINCT FUNCTION('WEEK', t.transactionTime)) AS avgWeeklyVisits,
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
                                                         @Param("category") String category,
                                                         @Param("startOfMonth") LocalDate startOfMonth,
                                                         @Param("endOfMonth") LocalDateTime endOfMonth);


}