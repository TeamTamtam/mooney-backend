package tamtam.mooney.domain.transaction.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tamtam.mooney.domain.transaction.entity.ScheduledTransaction;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface ScheduledTransactionRepository extends JpaRepository<ScheduledTransaction, Long> {

    // 특정 사용자의 해당 월 스케줄된 트랜잭션 조회 (최적화: WHERE 절 필터링)
    @Query("SELECT s FROM ScheduledTransaction s " +
            "WHERE s.user = :user " +
            "AND s.scheduledDate BETWEEN :startOfMonth AND :endOfMonth")
    List<ScheduledTransaction> getScheduledTransactionsForMonth(
            @Param("user") User user,
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("endOfMonth") LocalDate endOfMonth
    );

    // 아직 발생하지 않은 스케줄된 트랜잭션 합계 금액 조회 (최적화: IS NULL을 WHERE에 포함)
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM ScheduledTransaction s " +
            "WHERE s.user = :user " +
            "AND s.transaction IS NULL " +
            "AND s.scheduledDate BETWEEN :startOfMonth AND :endOfMonth")
    Long getPendingScheduledTransactionAmountForMonth(
            @Param("user") User user,
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("endOfMonth") LocalDate endOfMonth
    );

    // 전체 스케줄된 트랜잭션 합계 조회 (WHERE 절 필터링 적용)
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM ScheduledTransaction s " +
            "WHERE s.user = :user " +
            "AND s.scheduledDate BETWEEN :startOfMonth AND :endOfMonth")
    Long getTotalScheduledTransactionAmountForMonth(
            @Param("user") User user,
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("endOfMonth") LocalDate endOfMonth
    );

    // 특정 월의 트랜잭션 유형에 따른 스케줄된 트랜잭션 조회
    @Query("SELECT s FROM ScheduledTransaction s " +
            "WHERE s.user = :user " +
            "AND s.scheduledDate BETWEEN :startOfMonth AND :endOfMonth " +
            "AND s.transactionType = :transactionType")
    List<ScheduledTransaction> getScheduledTransactionsByTypeForMonth(
            @Param("user") User user,
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("endOfMonth") LocalDate endOfMonth,
            @Param("transactionType") String transactionType
    );
}