package tamtam.mooney.domain.mission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.mission.entity.AttendanceRecord;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
}
