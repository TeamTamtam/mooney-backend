package tamtam.mooney.domain.transaction.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tamtam.mooney.domain.transaction.entity.Expense;
import tamtam.mooney.domain.transaction.entity.ExpenseCategory;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user = :user " +
            "AND e.expenseCategory = :expenseCategory " +
            "AND e.transactionTime BETWEEN :startOfMonth AND :endOfMonth")
    Long getTotalExpenseForCategory(
            @Param("user") User user,
            @Param("expenseCategory") ExpenseCategory expenseCategory,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth);

    // 최근 지출 내역 조회 (최대 limit개)
    @Query("SELECT e FROM Expense e WHERE e.user = :user ORDER BY e.transactionTime DESC LIMIT :limit")
    List<Expense> findRecentExpenses(User user, int limit);
}
