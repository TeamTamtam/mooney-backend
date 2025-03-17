package tamtam.mooney.domain.chat.controller;

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

    @PostMapping
    public ResponseEntity<ChatResponseDto> chat(@RequestBody @Valid ChatRequestDto requestDto) {
        return ResponseEntity.ok().body(chatService.chat(requestDto));
    }
}
