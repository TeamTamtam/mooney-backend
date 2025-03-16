package tamtam.mooney.domain.mission.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record MissionTabDto(
         List<MissionDto> missions,
         Float mooneystatus

) {
}
