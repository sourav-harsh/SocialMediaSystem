package com.souravio.linkedInProject.userService.controller;

import com.souravio.linkedInProject.userService.dto.LoginRequestDto;
import com.souravio.linkedInProject.userService.dto.LoginResponseDto;
import com.souravio.linkedInProject.userService.dto.SignupRequestDto;
import com.souravio.linkedInProject.userService.dto.UserDto;
import com.souravio.linkedInProject.userService.exception.ResourceNotFoundException;
import com.souravio.linkedInProject.userService.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signUp(@RequestBody SignupRequestDto signupRequestDto) {
        UserDto userDto = authService.signUp(signupRequestDto);
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        String[] token = authService.login(loginRequestDto);
        Cookie cookie = new Cookie("refreshToken", token[1]);
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
        return ResponseEntity.ok(new LoginResponseDto(token[0]));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(HttpServletRequest httpServletRequest) {
    String refreshToken =
        Arrays.stream(httpServletRequest.getCookies())
            .filter(cookie -> "refreshToken".equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElseThrow(
                () -> new ResourceNotFoundException("Refresh token not found inside the Cookies"));

        String token = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new LoginResponseDto(token));
    }
}
