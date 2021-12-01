package com.as.pandaproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

//swagger에서 사용된다.
@Api(tags = {"댓글"})
/*@NoArgsConstructor 파라미터가 없는 기본 생성자를 생성
 * @AllArgsConstructor 모든 필드 값을 파라미터로 받는 생성자를 만듬
 * @RequiredArgsConstructor final이나 @NonNull인 필드 값만 파라미터로 받는 생성자를 만듬*/
@RequiredArgsConstructor
//@Controller는 view를 반환하기 위해서 사용, View를 반환하기 위해 ViewResolver사용
//@RestController Data를 반환하기 위해서 사용, @Controller로 같은 효과를 낼 수 있는데, @ResponseBody 사용해야함
//@RestController == @Controller + @ResponseBody
@RestController
public class CommentController {

    private final CommentService commentService;

    //코멘트 작성
    @ApiOperation(value = "댓글 작성")
    @PostMapping("/api/comments/{noteId}")
    //@PathVariable : {noteId}를 받아줌
    //@PathVariable 사용법: @PathVaribale("noteId") Long Id / @PathVaribale(name = "noteId") Long noteId / @PathVaribale Long noteId
    //@RequestBody: http부분의 body부분을 java 객체로 받게 해준다. 주로 json을 받을 때 사용
    //@RequestParam: GET방식의 url의 queryString을 받기에 좋음.
    //@RequestParam 사용법:  http://localhost:8080/reservation/api/reservations?reservationEmail=test@naver.com
    //위에서  @RequestParam(value = "reservationEmail",required = false) 저기 위에 ?다음꺼를 value에 적으면 = 이후의 값을 받는다.
    public CommentCreateResponseDto createComment(@PathVariable Long noteId, @AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody CommentCreateRequestDto commentCreateRequestDto){
        return commentService.createComment(noteId,userDetails.getUser(),commentCreateRequestDto);
    }
    //@AuthenticationPrincipal: 로그인 한 사용자의 정보를 받고 싶을 때 쓰는 파라미터, 사용시 UserDetailsService에서 return한 객체를 사용가능
    //UserDetails: Spring Security에서 사용자의 정보를 담는 인터페이스
    //UserDetailsService: DB에서 유저정보를 직접 가져오는 인터페이스, loadUserByUsername(): UserDetailsService 인터페이스 구현시 오버라이드 되는 메소드, 유저정보를 불러오는데 사용됨.

    //코멘트 읽기
    @ApiOperation(value = "댓글 읽기")
    @GetMapping("/api/comments/{noteId}")
    public CommentReadListResponseDto readComments(@PathVariable Long noteId, @AuthenticationPrincipal UserDetailsImpl userDetails){
        return commentService.readComments(noteId,userDetails.getUser());
    }
    //코멘트 수정
    @ApiOperation(value = "댓글 수정")
    @PutMapping("/api/comments/{commentId}")
    public CommentUpdateResponseDto updateComment(@PathVariable Long commentId, @AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody CommentUpdateRequestDto commentUpdateRequestDto){
        return commentService.updateComment(commentId, userDetails.getUser(), commentUpdateRequestDto);
    }
    //코멘트 삭제
    @ApiOperation(value = "댓글 삭제")
    @DeleteMapping("/api/comments/{commentId}")
    public CommentDeleteResponseDto deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commentService.deleteComment(commentId, userDetails.getUser());
    }
}
