package tamtam.mooney.domain.transaction.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tamtam.mooney.domain.transaction.entity.ScheduledTransaction;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface ScheduledTransactionRepository extends JpaRepository<ScheduledTransaction, Long> {

    // 특정 사용자의 해당 월 고정 트랜잭션 조회
    @Query("SELECT s FROM ScheduledTransaction s " +
            "WHERE s.user = :user AND s.scheduledDate BETWEEN :startOfMonth AND :endOfMonth")
    List<ScheduledTransaction> getScheduledTransactionsForMonth(
            @Param("user") User user,
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("endOfMonth") LocalDate endOfMonth
    );

    // 특정 월의 아직 발생하지 않은 ScheduledTransaction 합계 금액 조회 (transaction이 null이고, scheduledDate이 해당 월 내인 경우)
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM ScheduledTransaction s WHERE s.user = :user AND s.transaction IS NULL AND s.scheduledDate BETWEEN :startOfMonth AND :endOfMonth")
    Long getTotalAmountByUserAndTransactionIsNullAndScheduledDateBetween(
            @Param("user") User user,
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("endOfMonth") LocalDate endOfMonth);

    // 특정 월의 전체 ScheduledTransaction 합계 금액 조회
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM ScheduledTransaction s WHERE s.user = :user AND s.scheduledDate BETWEEN :startOfMonth AND :endOfMonth")
    Long getTotalAmountByUserAndScheduledDateBetween(
            @Param("user") User user,
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("endOfMonth") LocalDate endOfMonth);

    List<ScheduledTransaction> findByUserAndScheduledDateBetweenAndTransactionType(User user, LocalDate startOfMonth, LocalDate endOfMonth, String transactionType);
}
