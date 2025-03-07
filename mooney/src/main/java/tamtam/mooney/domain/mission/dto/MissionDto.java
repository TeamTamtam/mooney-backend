package tamtam.mooney.domain.mission.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MissionDto(
         LocalDateTime startTime,
         LocalDateTime endDate,
         String title,
         String advice

) {
}
