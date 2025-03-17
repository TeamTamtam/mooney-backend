package tamtam.mooney.domain.chat.dto;

import jakarta.validation.constraints.NotNull;

public record ChatRequestDto (
        @NotNull
        String message
) {}
