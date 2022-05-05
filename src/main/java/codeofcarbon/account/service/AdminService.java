package codeofcarbon.account.service;

import com.codeofcarbon.account.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    UserService userService;
}
