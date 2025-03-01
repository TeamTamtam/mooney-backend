package tamtam.mooney.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import tamtam.mooney.global.exception.ErrorCode;
import tamtam.mooney.global.exception.ErrorResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode, String requestUri) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // ErrorDto 생성
        ErrorResponse errorDto = new ErrorResponse(
                errorCode.getStatus(),
                errorCode.name(),
                errorCode.getMessage(),
                requestUri,
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                null
        );

        try {
            String jsonResponse = objectMapper.writeValueAsString(errorDto);
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
