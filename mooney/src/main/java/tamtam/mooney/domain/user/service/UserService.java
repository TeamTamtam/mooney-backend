package tamtam.mooney.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.user.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
}
