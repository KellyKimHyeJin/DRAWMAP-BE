package com.umc.drawmap.controller;

import com.umc.drawmap.domain.Challenge;
import com.umc.drawmap.domain.User;
import com.umc.drawmap.dto.challenge.ChallengeConverter;
import com.umc.drawmap.dto.challenge.ChallengeReqDto;
import com.umc.drawmap.dto.challenge.ChallengeResDto;
import com.umc.drawmap.exception.BaseResponse;
import com.umc.drawmap.repository.UserRepository;
import com.umc.drawmap.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Member;
import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/challenge")
public class ChallengeController {

    private final ChallengeService challengeService;
    private final UserRepository userRepository;

    // 도전코스 상세페이지 조회
    @GetMapping("/{courseId}")
    public BaseResponse<ChallengeResDto.ChallengeDto> getChallenge(@PathVariable(name = "courseId")Long courseId){
        return new BaseResponse<>(challengeService.findById(courseId));
    }

    // 도전코스 전체 리스트 조회 3개씩
    @GetMapping("/courses")
    public BaseResponse<List<ChallengeResDto.ChallengeDto>> getChallengeList(@RequestParam(name = "page")int page){
        return new BaseResponse<>(challengeService.findAll(page));
    }

    // 도전코스 본인 리스트 조회 (참여한 이달의 도전코스) 6개씩
    @GetMapping("/courses/mylist")
    public BaseResponse<List<ChallengeResDto.MyChallengeDto>> getChallengeMyList(@RequestParam(name = "page")int page){
        return new BaseResponse<>(challengeService.findAllByUser(page));
    }

    // 도전코스 정렬 (최신순) 도전코스 6개씩
    // 페이지 0부터 시작 -> ex) 6개씩 있는 2페이지 조회 PageRequest.of((page)1,(size)5)
    @GetMapping("/list")
    public BaseResponse<Page<ChallengeResDto.ChallengeSortDto>> getChallengeListByCreatedAt(@RequestParam(name = "page")int page){
        Page<Challenge> challengeList = challengeService.getPage(page-1);
        return new BaseResponse<>(ChallengeConverter.toChallengeSortList(challengeList));
    }

    // 도전코스 정렬 (인기순)
    @GetMapping("/likelist")
    public BaseResponse<Page<ChallengeResDto.ChallengeSortDto>> getChallengeListByScrap(@RequestParam(name = "page")int page){
        Page<Challenge> challengeList = challengeService.getPageByScrap(page-1);
        return new BaseResponse<>(ChallengeConverter.toChallengeSortList(challengeList));
    }

    // 도전코스 정렬 (지역별)
    @GetMapping("/arealist")
    public BaseResponse<Page<ChallengeResDto.ChallengeSortDto>> getChallengeListByArea(@RequestParam(name = "page")int page, @RequestParam(name = "sido") String sido, @RequestParam(name = "sgg")String sgg){
        Page<Challenge> challengeList = challengeService.getPageByArea(page-1, sido, sgg);
        return new BaseResponse<>(ChallengeConverter.toChallengeSortList(challengeList));
    }

    // 관광지 추천 (코스사진이랑 관광지사진만)
    @GetMapping("/spot/image/{courseId}")
    public BaseResponse<ChallengeResDto.ChallengeDetailDto> getSpotRecommend(@PathVariable(name = "courseId")Long courseId){
        return new BaseResponse<>(challengeService.getSpotRecommend(courseId));
    }


    @PostMapping("/course")
    public BaseResponse<String> createChallenge(@RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                @ModelAttribute(value= "request") ChallengeReqDto.CreateChallengeDto request
                                                ) throws IOException{
        Challenge challenge = challengeService.create(files, request);
        return new BaseResponse<>("새로운 도전코스 등록 완료");
    }

    @PatchMapping("/course/{courseId}")
    public BaseResponse<String> updateChallenge(@PathVariable(name = "courseId")Long courseId,
                                                @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                @ModelAttribute ChallengeReqDto.UpdateChallengeDto request) throws IOException{
        Challenge challenge = challengeService.update(courseId,files, request);
        return new BaseResponse<>("도전코스 수정 완료");
    }

    @DeleteMapping("/course/{courseId}")
    public BaseResponse<String> deleteChallenge(@PathVariable(name = "courseId")Long courseId){
        challengeService.delete(courseId);
        return new BaseResponse<>("도전코스 삭제 완료");
    }


}
