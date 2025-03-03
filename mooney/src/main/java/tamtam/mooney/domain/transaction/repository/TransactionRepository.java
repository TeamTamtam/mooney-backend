package tamtam.mooney.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tamtam.mooney.domain.transaction.entity.ExpenseCategory;
import tamtam.mooney.domain.transaction.entity.Transaction;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 특정 날짜의 모든 트랜잭션(Expense + Income) 조회
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.transactionTime BETWEEN :startTime AND :endTime ORDER BY t.transactionTime ASC")
    List<Transaction> findByUserAndTransactionTimeBetweenOrderByTransactionTime(User user, LocalDateTime startTime, LocalDateTime endTime);

    // 특정 날짜의 총 지출
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE TYPE(t) = Expense AND t.user = :user AND t.transactionTime BETWEEN :startTime AND :endTime")
    Long getTotalExpenseAmountForDate(User user, LocalDateTime startTime, LocalDateTime endTime);

    // 특정 날짜의 총 수입
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE TYPE(t) = Income AND t.user = :user AND t.transactionTime BETWEEN :startTime AND :endTime")
    Long getTotalIncomeAmountForDate(User user, LocalDateTime startTime, LocalDateTime endTime);

    // 특정 월의 총 지출
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE TYPE(t) = Expense AND t.user = :user AND t.transactionTime BETWEEN :startOfMonth AND :endOfMonth")
    Long getTotalExpenseAmountForMonth(User user, LocalDateTime startOfMonth, LocalDateTime endOfMonth);

    // 특정 월의 총 수입
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE TYPE(t) = Income AND t.user = :user AND t.transactionTime BETWEEN :startOfMonth AND :endOfMonth")
    Long getTotalIncomeAmountForMonth(User user, LocalDateTime startOfMonth, LocalDateTime endOfMonth);


    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE TYPE(t) = Expense AND t.user = :user " +
            "AND t.expenseCategory = :expenseCategory " +
            "AND t.transactionTime BETWEEN :startOfMonth AND :endOfMonth")
    Long getTotalExpenseForCategory(User user, ExpenseCategory expenseCategory, LocalDateTime startOfMonth, LocalDateTime endOfMonth);
}