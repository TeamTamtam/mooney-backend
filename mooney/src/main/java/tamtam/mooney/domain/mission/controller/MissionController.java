package tamtam.mooney.domain.mission.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tamtam.mooney.domain.mission.service.MissionService;
import tamtam.mooney.domain.transaction.dto.MonthlyTransactionDayUnitDto;

import java.time.LocalDate;

@Tag(name = "Mission")
@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

//    @Operation(summary = "미션창 조회(배경, 미션 현황 업데이트)")
//    @GetMapping("/results/daily")
//    public ResponseEntity<MonthlyTransactionDayUnitDto> getMissionResultByDate(@RequestParam @NotNull LocalDate date) {
//        return ResponseEntity.ok(missionService.getMissionResultByDate(date));
//    }
}
