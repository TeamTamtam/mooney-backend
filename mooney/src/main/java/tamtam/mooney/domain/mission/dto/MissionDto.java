package tamtam.mooney.domain.mission.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MissionDto(
         String title,
         String advice,
         Float result,
         long numOfExpense,
         long amountOfExpense
) {
}
