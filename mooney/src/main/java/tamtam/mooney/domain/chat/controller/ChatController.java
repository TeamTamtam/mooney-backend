package tamtam.mooney.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tamtam.mooney.domain.chat.dto.ChatRequestDto;
import tamtam.mooney.domain.chat.dto.ChatResponseDto;
import tamtam.mooney.domain.chat.service.ChatService;

@Tag(name = "Chat")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    @Operation(summary = "똑똑소비봇에게 채팅 전송", description = "똑똑소비봇에게 채팅 메시지를 전송합니다.")
    @PostMapping
    public ResponseEntity<ChatResponseDto> chat(@RequestBody @Valid ChatRequestDto requestDto) {
        return ResponseEntity.ok().body(chatService.chat(requestDto));
    }
}
