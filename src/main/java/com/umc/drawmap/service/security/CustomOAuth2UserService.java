package com.umc.drawmap.service.security;

import com.umc.drawmap.domain.User;
import com.umc.drawmap.dto.token.TokenReqDto;
import com.umc.drawmap.dto.token.TokenResDto;
import com.umc.drawmap.dto.user.UserReqDto;
import com.umc.drawmap.exception.user.DuplicateUserEmailException;
import com.umc.drawmap.exception.userChallenge.NoExistUserException;
import com.umc.drawmap.repository.UserRepository;
import com.umc.drawmap.security.KakaoAccount;
import com.umc.drawmap.security.KakaoUserInfo;
import com.umc.drawmap.security.KakaoUserInfoResponse;
import com.umc.drawmap.security.jwt.JwtProvider;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final KakaoUserInfo kakaoUserInfo;

    private final JwtProvider jwtProvider;
    public CustomOAuth2UserService(UserRepository userRepository, JwtProvider jwtProvider, KakaoUserInfo kakaoUserInfo) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.kakaoUserInfo = kakaoUserInfo;
    }


    @Transactional
    public User createUser(String email, UserReqDto.signUpDto dto) {
        // 이미 가입했는지 이메일을 통해 Check
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            throw new DuplicateUserEmailException();
        }
        // 가입하지 않았다면, -> 유저 생성.
        User user = User.builder()
                .nickName(dto.getNickName())
                .bike(dto.getBike())
                .role(dto.getRole())
                .gender(dto.getGender())
                .sgg(dto.getSgg())
                .sido(dto.getSido())
                .email(dto.getEmail())
                .profileImg(dto.getProfileImg())
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public TokenResDto loginUser(TokenReqDto tokenReqDto) {

        KakaoUserInfoResponse userInfo = kakaoUserInfo.getUserInfo(tokenReqDto.getAccess_token());
        KakaoAccount kakao_account = userInfo.getKakao_account();
        String email = kakao_account.getEmail();

        System.out.println("로그인 시도 하는 유저의 email : " + email);


        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            // 유저에 대해 Access Token 을 줘야해.
            User user = userOptional.get();
            List<String> stringList = new ArrayList<>();
            stringList.add("User");
            TokenResDto tokenResDto = jwtProvider.createToken(user.getId(), stringList);
            return tokenResDto;
        }
        else {
            throw new NoExistUserException("해당 유저가 존재하지 않습니다. 회원가입이 필요합니다");
        }
    }
}
