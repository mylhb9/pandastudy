package com.as.pandaproject.controller;

import com.as.pandaproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

//HTML 단에서 바로 보여줌(가독성)
//Swagger와 연관
@Api(tags = {"로그인"})
@RestController
//@Controller는 view를 반환하기 위해서 사용, View를 반환하기 위해 ViewResolver사용
//@RestController Data를 반환하기 위해서 사용, @Controller로 같은 효과를 낼 수 있는데, @ResponseBody 사용해야함
//@RestController == @Controller + @ResponseBody
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    //@PathVariable : {noteId}를 받아줌
    //@PathVariable 사용법: @PathVaribale("noteId") Long Id / @PathVaribale(name = "noteId") Long noteId / @PathVaribale Long noteId
    //@RequestBody: http부분의 body부분을 java 객체로 받게 해준다. 주로 json을 받을 때 사용
    //@RequestParam: GET방식의 url의 queryString을 받기에 좋음.
    //@RequestParam 사용법:  http://localhost:8080/reservation/api/reservations?reservationEmail=test@naver.com
    //위에서  @RequestParam(value = "reservationEmail",required = false) 저기 위에 ?다음꺼를 value에 적으면 = 이후의 값을 받는다.
    @ApiOperation(value = "카카오 소셜 로그인")
    @GetMapping("/user/kakao/callback")
    public HeaderDto kakaoLogin(@RequestParam(value = "code") String code) {
        return userService.kakaoLogin(code);
    }

    @ApiOperation(value = "기본 회원가입")
    @PostMapping("/user/signup")
    public void registerUser(@RequestBody SignupRequestDto requestDto) {
        userService.registerUser(requestDto);
    }

    @ApiOperation(value= "기본 로그인")
    @PostMapping("/user/login")
    public String login(@RequestBody SignupRequestDto requestDto) {
        return userService.login(requestDto);
    }
}

