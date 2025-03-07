package tamtam.mooney.domain.user.dto;

import lombok.Builder;

@Builder
public record UserHomeWeeklyMissionDto(
        String title,
        Float status
) {}