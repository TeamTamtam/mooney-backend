package tamtam.mooney.domain.transaction.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tamtam.mooney.domain.transaction.entity.Transaction;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 특정 날짜의 모든 트랜잭션(Expense + Income) 조회 (한 번의 쿼리 실행!)
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.transactionTime BETWEEN :startTime AND :endTime ORDER BY t.transactionTime ASC")
    List<Transaction> findByUserAndTransactionTimeBetweenOrderByTransactionTime(
            @Param("user") User user,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // 특정 날짜의 총 지출
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE TYPE(t) = Expense AND t.user = :user AND t.transactionTime BETWEEN :startTime AND :endTime")
    Long getTotalExpenseForDate(@Param("user") User user, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // 특정 날짜의 총 수입
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE TYPE(t) = Income AND t.user = :user AND t.transactionTime BETWEEN :startTime AND :endTime")
    Long getTotalIncomeForDate(@Param("user") User user, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
