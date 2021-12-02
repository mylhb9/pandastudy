package com.as.pandaproject.controller;


import com.example.teampandanback.OAuth2.UserDetailsImpl;
import com.example.teampandanback.dto.note.response.NoteSearchInBookmarkResponseDto;
import com.example.teampandanback.service.BookmarkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Api(tags = {"북마크"})
/*@NoArgsConstructor 파라미터가 없는 기본 생성자를 생성
 * @AllArgsConstructor 모든 필드 값을 파라미터로 받는 생성자를 만듬
 * @RequiredArgsConstructor final이나 @NonNull인 필드 값만 파라미터로 받는 생성자를 만듬*/
@RequiredArgsConstructor
//@Controller는 view를 반환하기 위해서 사용, View를 반환하기 위해 ViewResolver사용
//@RestController Data를 반환하기 위해서 사용, @Controller로 같은 효과를 낼 수 있는데, @ResponseBody 사용해야함
//@RestController == @Controller + @ResponseBody
@RestController
//Controller 안에 @Component 있음
// 빈으로 등록해주겠다는 의미
// 의존성 주입은 spring에만 있는 개념이 아님. 단지 스프링에서 같이 설명하는 것은 의존성 주입을 하는데 있어서 더 편리해서 사용하는 것임
public class BookmarkController {
    //필드를 선언하고 @RequiredArgsConstructor를 통해 생성자가 생성되고 주입은 Bean이 들어와서 바로 객체로 사용이 가능한 것임
    private final BookmarkService bookmarkService;

    //북마크 함
    @ApiOperation(value = "북마크 하기")
    @PostMapping("/api/notes/{noteId}/bookmark")
    //@PathVariable : {noteId}를 받아줌
    //@PathVariable 사용법: @PathVaribale("noteId") Long Id / @PathVaribale(name = "noteId") Long noteId / @PathVaribale Long noteId
    //@RequestBody: http부분의 body부분을 java 객체로 받게 해준다. 주로 json을 받을 때 사용
    //@RequestParam: GET방식의 url의 queryString을 받기에 좋음.
    //@RequestParam 사용법:  http://localhost:8080/reservation/api/reservations?reservationEmail=test@naver.com
    public void bookmarkNote(@PathVariable Long noteId, @AuthenticationPrincipal UserDetailsImpl userDetails){
        bookmarkService.bookmarkNote(noteId,userDetails.getUser());
    }

    //북마크 해제
    @ApiOperation(value = "북마크 해제")
    @PostMapping("/api/notes/{noteId}/unbookmark")
    public void unBookmarkNote(@PathVariable Long noteId, @AuthenticationPrincipal UserDetailsImpl userDetails){
        bookmarkService.unBookmarkNote(noteId,userDetails.getUser());
    }

    // 북마크한 노트들 중에서 노트 제목 검색
    @ApiOperation(value = "내가 북마크 한 노트들 중에서 노트 검색 (제목으로)", notes = "http://{hostName}/api/notes/search/bookmarks/?keyword=xxxx")
    @GetMapping("/api/notes/search/bookmarks")
    //Controller에서 형식에 맞게 쏘아줄 필요가 있으면 Dto로 쏴주는 것이 좋다.
    public NoteSearchInBookmarkResponseDto searchNoteInBookmark(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam("keyword") String rawKeyword){
        return bookmarkService.searchNoteInBookmarks(userDetails.getUser(), rawKeyword);
    }
}
