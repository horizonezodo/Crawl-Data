package com.nextg.crawler.controll;

import com.nextg.crawler.exception.TokenRefreshException;
import com.nextg.crawler.jwt.JwtUntils;
import com.nextg.crawler.model.User;
import com.nextg.crawler.repo.UserRepository;
import com.nextg.crawler.response.*;
import com.nextg.crawler.service.MailService;
import com.nextg.crawler.service.RefreshTokenServiceImpl;
import com.nextg.crawler.service.UserDetailsImpl;
import com.nextg.crawler.model.RefreshToken;
import com.nextg.crawler.request.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.util.Optional;
import java.util.Random;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    @Autowired
    AuthenticationManager manager;

    @Autowired
    JwtUntils untils;

    @Autowired
    UserRepository repo;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    private RefreshTokenServiceImpl refreshService;

    @Autowired
    MailService service;


    @PostMapping("/login")
    public ResponseEntity<?> authenticationUserUsingEmail(@RequestBody LoginRequest request){
        Optional<User> otp = repo.findByEmail(request.getEmail());
        log.info("Authen access");
        if (otp.isPresent()){
            User user = otp.get();
            log.info(user.toString());
            if(encoder.matches(request.getPassword(), user.getPassword())){
                System.out.println(true);
            }else{
                System.out.println(false);
            }
        }
        if(repo.existsByEmail(request.getEmail())){
            log.info(request.getEmail() + " " + request.getPassword());
            Authentication authentication = manager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            log.info("Create authentication success");
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl accDetails = (UserDetailsImpl) authentication.getPrincipal();
            String jwt = untils.generateJwtTokenForLogin(accDetails);
            RefreshToken refreshToken = refreshService.createRefreshToken(accDetails.getId());
            LoginResponse res = new LoginResponse();
            res.setUser_role(accDetails.getUser_role());
            res.setEmail(accDetails.getEmail());
            res.setUsername(accDetails.getUsername());
            res.setToken(jwt);
            res.setRefreshToken(refreshToken.getToken());
            log.info("Login Success : " + accDetails.getEmail());
            return new ResponseEntity<>(res,HttpStatus.OK);

        }
        log.error("Account has been locked : " + request.getEmail());
        return new ResponseEntity<>(new ResponseError("801"), HttpStatus.BAD_REQUEST);
    }

    

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken( @RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        return refreshService.findByToken(requestRefreshToken)
                .map(refreshService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(acc -> {
                    String token = untils.generateTokenFromEmail(acc.getEmail());
                    RefreshToken newRefreshTOken = refreshService.updateRefreshToken(request.getRefreshToken());
                    log.info("refresh token Success : " + token);
                    return ResponseEntity.ok(new RefreshTokenResponse(token, newRefreshTOken.getToken()));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "810"));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(userDetails);
        Long userId = userDetails.getId();
        refreshService.deleteByUserId(userId);
        log.info("Logout user Success : " + userDetails.getUsername());
        logoutResponse res = new logoutResponse("Logout Success");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PutMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePassword request){
        Optional<User> otp = repo.findByEmail(request.getEmail());
        if(otp.isPresent()){
            User user = otp.get();
            if(encoder.matches(request.getOld_pass(), user.getPassword())){
                user.setPassword(encoder.encode(request.getNew_pass()));
                user.setEmailVerifired(true);
                ChangePassResponse res = new ChangePassResponse(user.getEmail(),request.getNew_pass());
                log.info("new pass" + request.getNew_pass());
                repo.save(user);
                return new ResponseEntity<>(res,HttpStatus.OK);
            }
            ResponseError error = new ResponseError("Mật khẩu không khớp");
            return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


    @PostMapping("/check-Token")
    public ResponseEntity<?> checkToken(@RequestBody checkTokenRequest request){
        if (untils.verifyToken(request.getToken())){
            CheckTokenResponse res = new CheckTokenResponse();
            res.setStatus(true);
            log.info("Token valid");
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        CheckTokenResponse res = new CheckTokenResponse();
        res.setStatus(false);
        log.info("Token invalid" + request.getToken());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/send-mail-change-pass")
    public ResponseEntity<?> sendMailChangePass(@RequestBody SendMailChangePassRequest request) throws MessagingException {
            Random random = new Random();
            int minValue = 1000;
            int maxValue = 10000;
            int randomNumber = random.nextInt(maxValue - minValue + 1) + minValue;
            service.SendMail(request.getEmail(), String.valueOf(randomNumber));
            log.info("Email change pass has been send for email " + request.getEmail());
            return new ResponseEntity<>(HttpStatus.OK);
    }


}
