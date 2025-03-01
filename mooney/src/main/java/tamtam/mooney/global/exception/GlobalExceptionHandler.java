package tamtam.mooney.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
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

    // 커스텀 예외 처리
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e, HttpServletRequest request) {
        return buildErrorResponse(e.getErrorCode(), request);
    }

    // 요청 파라미터 누락 예외 처리
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handleMissingRequestParamException(MissingServletRequestParameterException e, HttpServletRequest request) {
        return buildErrorResponse(ErrorCode.MISSING_PARAMETER, request);
    }

    // @Valid 유효성 검사 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<String> errorDetails = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + Optional.ofNullable(fieldError.getDefaultMessage()).orElse("Invalid value"))
                .collect(Collectors.toList());
        return buildErrorResponse(ErrorCode.INVALID_INPUT_VALUE, request, errorDetails);
    }

    // @NotNull, @Size 등 단일 필드 검증 실패 처리
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        List<String> errorDetails = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath().toString() + ": " + violation.getMessage())
                .collect(Collectors.toList());
        return buildErrorResponse(ErrorCode.INVALID_INPUT_VALUE, request, errorDetails);
    }

    // 예상하지 못한 서버 내부 오류 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleGenericException(Exception e, HttpServletRequest request) {
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, request, List.of(e.getMessage()));
    }

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
