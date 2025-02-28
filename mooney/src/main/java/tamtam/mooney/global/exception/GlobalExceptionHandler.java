package tamtam.mooney.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e, HttpServletRequest request) {
        return buildErrorResponse(e.getErrorCode(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handleMissingRequestParamException(MissingServletRequestParameterException e, HttpServletRequest request) {
        return buildErrorResponse(ErrorCode.MISSING_PARAMETER, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<String> errorDetails = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + Optional.ofNullable(fieldError.getDefaultMessage()).orElse("Invalid value"))
                .collect(Collectors.toList());
        return buildErrorResponse(ErrorCode.INVALID_INPUT_VALUE, request, errorDetails);
    }

    // 공통적인 ErrorResponse 빌드 메서드 (KST 기준)
    private ResponseEntity<ErrorResponse> buildErrorResponse(ErrorCode errorCode, HttpServletRequest request) {
        return buildErrorResponse(errorCode, request, null);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(ErrorCode errorCode, HttpServletRequest request, List<String> details) {
        ZonedDateTime zonedDateTime = Instant.now().atZone(KST_ZONE);
        ErrorResponse errorResponse = new ErrorResponse(
                errorCode.getStatus(),
                errorCode.name(),
                errorCode.getMessage(),
                request.getRequestURI(),
                zonedDateTime.toString(),
                details
        );
        return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
    }
}
