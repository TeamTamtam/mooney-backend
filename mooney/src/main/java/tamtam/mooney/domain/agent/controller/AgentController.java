package tamtam.mooney.domain.agent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tamtam.mooney.domain.agent.dto.AgentListResponseDto;
import tamtam.mooney.domain.agent.dto.AgentResponseDto;
import tamtam.mooney.domain.agent.service.UserAgentService;

import java.util.List;

@Tag(name = "Agent")
@RestController
@RequiredArgsConstructor
@RequestMapping("/agents")
public class AgentController {

    private final UserAgentService userAgentService;

    @Operation(summary = "현재 활성화된 Agent 정보 조회", description = "현재 사용 중인 Agent 정보를 반환합니다.")
    @GetMapping("/active")
    public ResponseEntity<AgentResponseDto> getActiveUserAgent() {
        AgentResponseDto activeAgent = userAgentService.getActiveUserAgentInfo();
        return ResponseEntity.ok(activeAgent);
    }

    @Operation(summary = "전체 Agent 목록 및 보유 상태 조회", description = "사용자가 보유한 Agent는 잠금 해제 상태로, 보유하지 않은 Agent는 잠김 상태로 반환합니다.")
    @GetMapping
    public ResponseEntity<List<AgentListResponseDto>> getAllAgents() {
        List<AgentListResponseDto> agents = userAgentService.getAllAgentsWithUnlockStatus();
        return ResponseEntity.ok(agents);
    }

    @Operation(summary = "활성화 Agent 변경", description = "사용자가 보유한 Agent를 활성화합니다.")
    @PostMapping("/activate/{userAgentId}")
    public ResponseEntity<Void> activateUserAgent(@PathVariable @NotNull Long userAgentId) {
        userAgentService.activateUserAgent(userAgentId);
        return ResponseEntity.ok().build();
    }
}
