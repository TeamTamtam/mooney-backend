package tamtam.mooney.domain.transaction.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tamtam.mooney.domain.enums.ExpenseCategory;
import tamtam.mooney.domain.transaction.entity.Expense;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("SELECT e.expenseCategory, COALESCE(SUM(t.amount), 0) " +
            "FROM Expense e " +
            "JOIN Transaction t ON e.transactionId = t.transactionId " +
            "WHERE t.user = :user AND t.transactionTime BETWEEN :startOfMonth AND :endOfMonth " +
            "GROUP BY e.expenseCategory")
    List<Object[]> getTotalExpenseForAllCategories(
            @Param("user") User user,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth
    );

    // 최근 지출 내역 조회 (최대 limit개)
    @Query("SELECT e FROM Expense e WHERE e.user = :user ORDER BY e.transactionTime DESC LIMIT :limit")
    List<Expense> findRecentExpenses(User user, int limit);

}
