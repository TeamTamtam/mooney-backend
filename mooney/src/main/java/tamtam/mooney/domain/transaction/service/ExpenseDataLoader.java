package tamtam.mooney.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tamtam.mooney.domain.enums.ExpenseCategory;
import tamtam.mooney.domain.transaction.entity.Expense;
import tamtam.mooney.domain.transaction.repository.TransactionRepository;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
public class ExpenseDataLoader {

    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private static final Logger logger = LoggerFactory.getLogger(ExpenseDataLoader.class);

    public void loadExpensesFromExcel(String filePath) throws IOException {
        User currentUser = userService.getCurrentUser();
        FileInputStream file = new FileInputStream(new File(filePath));
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        Iterator<Row> rowIterator = sheet.iterator();
        if (rowIterator.hasNext()) {
            rowIterator.next(); // 헤더 건너뛰기
        }

        // 날짜, 시간, 그리고 결합한 날짜+시간 포맷터 설정
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            try {
                // (A) 타입 컬럼 (인덱스 2): "지출"인지 확인
                String type = getCellAsString(row.getCell(2));
                if (!"지출".equals(type)) continue;

                // (B) 날짜 컬럼 (인덱스 0)
                String dateStr;
                Cell dateCell = row.getCell(0);
                if (dateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dateCell)) {
                    LocalDateTime ldt = dateCell.getDateCellValue().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDateTime();
                    dateStr = ldt.format(dateFormatter);
                } else {
                    dateStr = getCellAsString(dateCell);
                }

                // (C) 시간 컬럼 (인덱스 1)
                String timeStr;
                Cell timeCell = row.getCell(1);
                if (timeCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(timeCell)) {
                    LocalDateTime ldt = timeCell.getDateCellValue().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDateTime();
                    timeStr = ldt.format(timeFormatter);
                } else {
                    timeStr = getCellAsString(timeCell);
                }

                LocalDateTime transactionTime = LocalDateTime.parse(dateStr + " " + timeStr, dateTimeFormatter);

                // (D) 금액 (인덱스 6)
                double rawAmount = row.getCell(6).getNumericCellValue();
                long amount = (long) Math.abs(rawAmount);

                // (E) 대분류 -> ExpenseCategory (인덱스 3)
                String rawCategory = getCellAsString(row.getCell(3));
                ExpenseCategory expenseCategory = ExpenseCategory.fromCategoryName(rawCategory);

                // (F) 결제처 (payee) (인덱스 5)
                String payee = getCellAsString(row.getCell(5));

                // (G) 결제수단 (sourceApp) (인덱스 8)
                String sourceApp = getCellAsString(row.getCell(8));

                // Expense 엔티티 생성
                Expense expense = Expense.builder()
                        .payee(payee)
                        .expenseCategory(expenseCategory)
                        .amount(amount)
                        .transactionTime(transactionTime)
                        .transactionSource(sourceApp)  // 여기서는 결제수단을 거래 출처로 사용
                        .user(currentUser)
                        .build();

                transactionRepository.save(expense);
                logger.info("Row {} processed successfully: {}", row.getRowNum(), expense);
            } catch (Exception e) {
                logger.error("Error processing row {}: {}", row.getRowNum(), e.getMessage(), e);
            }
        }

        workbook.close();
        file.close();

        logger.info("✅ 모든 지출 데이터가 저장되었습니다!");
    }

    private String getCellAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 날짜형 셀는 별도로 처리하므로 여기서는 단순 숫자값으로 변환
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
