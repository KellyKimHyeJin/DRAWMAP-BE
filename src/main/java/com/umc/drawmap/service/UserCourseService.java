package com.umc.drawmap.service;

import com.umc.drawmap.domain.User;
import com.umc.drawmap.domain.UserCourse;
import com.umc.drawmap.dto.UserCourseReqDto;
import com.umc.drawmap.dto.UserCourseResDto;
import com.umc.drawmap.dto.user.UserResDto;
import com.umc.drawmap.exception.NotFoundException;
import com.umc.drawmap.repository.ScrapRepository;
import com.umc.drawmap.repository.UserCourseRepository;
import com.umc.drawmap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCourseService {
    private final UserCourseRepository userCourseRepository;

    private final UserRepository userRepository;

    private final ScrapRepository scrapRepository;
    private final S3FileService s3FileService;
    @Transactional
    public UserCourse create(List<MultipartFile> files, UserCourseReqDto.CreateUserCourseDto request) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new NotFoundException("유저가 존재하지 않습니다."));

        UserCourse userCourse = UserCourse.builder()
                .userCourseTitle(request.getUserCourseTitle())
                .sido(request.getSido())
                .sgg(request.getSgg())
                .userCourseContent(request.getUserCourseContent())
                .userCourseComment(request.getUserCourseComment())
                .userCourseDifficulty(request.getUserCourseDifficulty())
                .userImage(s3FileService.upload(files))
                .user(user)
                .build();

        return userCourseRepository.save(userCourse);
    }
    @Transactional
    public UserCourse update(Long uCourseId, List<MultipartFile> files, UserCourseReqDto.UpdateUserCourseDto request) throws IOException{
        UserCourse userCourse = userCourseRepository.findById(uCourseId)
                .orElseThrow(()-> new NotFoundException("유저개발코스를 찾을 수 없습니다."));
        userCourse.update(request.getUserCourseTitle(), request.getSido(), request.getSgg(),
                request.getUserCourseDifficulty(), request.getUserCourseContent(),
                request.getUserCourseComment(), s3FileService.upload(files));
        return userCourse;
    }

    public void delete(Long ucourseId){
        userCourseRepository.deleteById(ucourseId);
    }

    // 유저 도전 조회
    public List<UserCourse> userCourseList() {
        return userCourseRepository.findAll();
    }


    // 유저코스 10개씩
    public List<UserCourseResDto.UserCourseSortDto> getPage(int page, String sort){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new NotFoundException("유저가 존재하지 않습니다."));

        List<UserCourse> list;
        if(Objects.equals(sort, "likecount")){
            PageRequest pageRequest = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "scrapCount"));
            list = userCourseRepository.findAll(pageRequest).getContent();
        }else{
            PageRequest pageRequest = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
            list = userCourseRepository.findAll(pageRequest).getContent();
        }

        List<UserCourseResDto.UserCourseSortDto> resultList = new ArrayList<>();
        for (UserCourse userCourse: list){
            Boolean isMyPost = userCourseRepository.existsByUser(user);
            Boolean isScraped= scrapRepository.existsScrapByUserAndUserCourse(user, userCourse);
            UserCourseResDto.UserCourseSortDto result = UserCourseResDto.UserCourseSortDto.builder()
                    .userCourseId(userCourse.getId())
                    .sido(userCourse.getSido())
                    .sgg(userCourse.getSgg())
                    .isMyPost(isMyPost)
                    .isScraped(isScraped)
                    .user(UserResDto.UserDto.builder().userId(userCourse.getUser().getId()).nickName(userCourse.getUser().getNickName()).profileImg(userCourse.getUser().getProfileImg()).build())
                    .title(userCourse.getUserCourseTitle())
                    .content(userCourse.getUserCourseContent())
                    .comment(userCourse.getUserCourseComment())
                    .image(userCourse.getUserImage())
                    .createdDate(userCourse.getCreatedAt())
                    .difficulty(userCourse.getUserCourseDifficulty())
                    .build();
            resultList.add(result);
        }
        return resultList;

    }

    public List<UserCourseResDto.UserCourseSortDto> getPageByArea(int page, String sido, String sgg){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new NotFoundException("유저가 존재하지 않습니다."));

        Pageable pageable = PageRequest.of(page, 10);
        List<UserCourse> list = userCourseRepository.findAllBySidoOrSgg(sido, sgg, pageable).getContent();

        List<UserCourseResDto.UserCourseSortDto> resultList = new ArrayList<>();
        for (UserCourse userCourse: list){
            Boolean isMyPost = userCourseRepository.existsByUser(user);
            Boolean isScraped= scrapRepository.existsScrapByUserAndUserCourse(user, userCourse);
            UserCourseResDto.UserCourseSortDto result = UserCourseResDto.UserCourseSortDto.builder()
                    .userCourseId(userCourse.getId())
                    .sido(userCourse.getSido())
                    .sgg(userCourse.getSgg())
                    .isMyPost(isMyPost)
                    .isScraped(isScraped)
                    .user(UserResDto.UserDto.builder().userId(userCourse.getUser().getId()).nickName(userCourse.getUser().getNickName()).profileImg(userCourse.getUser().getProfileImg()).build())
                    .title(userCourse.getUserCourseTitle())
                    .content(userCourse.getUserCourseContent())
                    .comment(userCourse.getUserCourseComment())
                    .image(userCourse.getUserImage())
                    .createdDate(userCourse.getCreatedAt())
                    .difficulty(userCourse.getUserCourseDifficulty())
                    .build();
            resultList.add(result);
        }
        return resultList;
    }

    public List<UserCourseResDto.MyUserCourseDto> findAllByUser(int page){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new NotFoundException("유저가 존재하지 않습니다."));

        Pageable pageable = PageRequest.of(page, 6);
        List<UserCourse> list = userCourseRepository.findAllByUser(user, pageable).getContent();

        List<UserCourseResDto.MyUserCourseDto> resultList = new ArrayList<>();
        for(UserCourse userCourse: list){
            UserCourseResDto.MyUserCourseDto result = UserCourseResDto.MyUserCourseDto.builder()
                    .userCourseId(userCourse.getId())
                    .sido(userCourse.getSido())
                    .sgg(userCourse.getSgg())
                    .image(userCourse.getUserImage())
                    .createdDate(userCourse.getCreatedAt())
                    .user(UserResDto.UserDto.builder().userId(userCourse.getUser().getId()).nickName(userCourse.getUser().getNickName()).profileImg(userCourse.getUser().getProfileImg()).build())
                    .build();
            resultList.add(result);
        }
        return resultList;

    }

    public UserCourseResDto.UserCourseDto findById(Long uCourseId){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new NotFoundException("유저가 존재하지 않습니다."));

        UserCourse userCourse = userCourseRepository.findById(uCourseId)
                .orElseThrow(()-> new NotFoundException("유저코스를 찾을 수 없습니다."));

        Boolean isMyPost = userCourseRepository.existsByUser(user);
        Boolean isScraped = scrapRepository.existsScrapByUserAndUserCourse(user, userCourse);

        return UserCourseResDto.UserCourseDto.builder()
                .title(userCourse.getUserCourseTitle())
                .userCourseId(userCourse.getId())
                .content(userCourse.getUserCourseContent())
                .comment(userCourse.getUserCourseComment())
                .sido(userCourse.getSido())
                .sgg(userCourse.getSgg())
                .isScraped(isScraped)
                .isMyPost(isMyPost)
                .image(userCourse.getUserImage())
                .createdDate(userCourse.getCreatedAt())
                .difficulty(userCourse.getUserCourseDifficulty())
                .user(UserResDto.UserDto.builder().userId(userCourse.getUser().getId()).nickName(userCourse.getUser().getNickName()).profileImg(userCourse.getUser().getProfileImg()).build())
                .scrapCount(userCourse.getScrapCount())
                .build();
    }

    public List<UserResDto.UserDto> getTop3User(){
        List<User> userList = userRepository.findAll();
        int scrap1 = 0;
        int scrap2 = 0;
        int scrap3 = 0;
        User user1 = null;
        User user2 = null;
        User user3 = null;

        for(User u: userList){
            List<UserCourse> userCourseList = userCourseRepository.findAllByUser(u);
            int localscrap = 0;
            for(UserCourse c: userCourseList){
                localscrap += c.getScrapCount();
            }
            if(localscrap > scrap1){
                scrap1 = localscrap;
                user1 = u;
            }else if (localscrap > scrap2){
                scrap2 = localscrap;
                user2 = u;
            }else if (localscrap > scrap3){
                scrap3 = localscrap;
                user3 = u;
            }
        }
        List<User> topUserList = new ArrayList<>();
        topUserList.add(user1);
        topUserList.add(user2);
        topUserList.add(user3);
        List<UserResDto.UserDto> list = new ArrayList<>();
        for(User u : topUserList){
            UserResDto.UserDto userDto = UserResDto.UserDto.builder()
                    .userId(u.getId())
                    .nickName(u.getNickName())
                    .profileImg(u.getProfileImg())
                    .build();
            list.add(userDto);
        }
        return list;

    }

}
