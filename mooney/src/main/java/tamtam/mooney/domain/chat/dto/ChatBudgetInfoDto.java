package tamtam.mooney.domain.chat.dto;

import jakarta.validation.constraints.NotNull;

public record ChatBudgetInfoDto (
        @NotNull String categoryName,
        long remaining
) {}
