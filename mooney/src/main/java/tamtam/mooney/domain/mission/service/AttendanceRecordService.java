package tamtam.mooney.domain.mission.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.mission.repository.AttendanceRecordRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class AttendanceRecordService {
    private final AttendanceRecordRepository attendanceRecordRepository;
}
