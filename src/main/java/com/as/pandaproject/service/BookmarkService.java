package com.as.pandaproject.service;


import com.example.teampandanback.OAuth2.UserDetailsImpl;
import com.example.teampandanback.domain.bookmark.Bookmark;
import com.example.teampandanback.domain.bookmark.BookmarkRepository;
import com.example.teampandanback.domain.note.Note;
import com.example.teampandanback.domain.note.NoteRepository;
import com.example.teampandanback.domain.project.Project;
import com.example.teampandanback.domain.user.User;
import com.example.teampandanback.domain.user.UserRepository;
import com.example.teampandanback.domain.user_project_mapping.UserProjectMapping;
import com.example.teampandanback.domain.user_project_mapping.UserProjectMappingRepository;
import com.example.teampandanback.dto.note.response.NoteEachSearchInBookmarkResponseDto;
import com.example.teampandanback.dto.note.response.NoteSearchInBookmarkResponseDto;
import com.example.teampandanback.exception.ApiRequestException;
import com.example.teampandanback.utils.PandanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BookmarkService {
    // 주입 받을 필드들을 선언, @RequiredArgsConstructor에 의해 생성자로 생성되고 자동주입됨
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserProjectMappingRepository userProjectMappingRepository;
    private final PandanUtils pandanUtils;

    public void bookmarkNote(Long noteId, User currentUser) {

        //북마크 누른 사람
        User user = userRepository.findById(currentUser.getUserId()).orElseThrow(
                () -> new ApiRequestException("등록되지 않은 유저의 접근입니다.")
        );
        //북마크 될 노트
        Note note = noteRepository.findById(noteId).orElseThrow(
                () -> new ApiRequestException("생성되지 않은 노트입니다.")
        );


        //Optional.ofNullable(value): Optional.empty() + Optional.of(value) 합쳐놓은 메소드, value에 해당하는 값이 있을 수도 없을 수도 있다는 뜻
        //Optional.empty(): 비어있는 Optional 객체를 생성
        //Optional.of(value): null이 아닌 객체를 담고 있는 Optional객체 생성, 이거 안에 null 들어 있으면 NPE(NUll Pointer Exception)를 넘긴다. 한마디로 큰일남
        //쿼리
        Project connectedProject = Optional.ofNullable(note.getProject()).orElseThrow(
                () -> new ApiRequestException("연결된 프로젝트가 없습니다.")
        );

        //쿼리
        UserProjectMapping userProjectMapping = userProjectMappingRepository.findByUserAndProject(user, connectedProject)
                .orElseThrow(
                        () -> new ApiRequestException("user와 project mapping을 찾지 못했습니다.")
                );

        //유저가 북마크를 했다는 레코드
        //orElseGet null일 때만 불림.
        //orElse null이던 말던 항상 불림.
        Bookmark bookmark = bookmarkRepository.findByUserAndNote(user, note)
                .orElseGet(() -> Bookmark.builder()
                        .user(user)
                        .note(note)
                        .build());

        bookmarkRepository.save(bookmark);
    }

    public void unBookmarkNote(Long noteId, User currentUser) {

        //북마크 누른 사람
        User user = userRepository.findById(currentUser.getUserId()).orElseThrow(
                () -> new ApiRequestException("등록되지 않은 유저의 접근입니다.")
        );

        //북마크 될 노트
        Note note = noteRepository.findById(noteId).orElseThrow(
                () -> new ApiRequestException("생성되지 않은 노트입니다.")
        );


        //쿼리
        Project connectedProject = Optional.ofNullable(note.getProject()).orElseThrow(
                () -> new ApiRequestException("연결된 프로젝트가 없습니다.")
        );

        //쿼리
        UserProjectMapping userProjectMapping = userProjectMappingRepository.findByUserAndProject(user, connectedProject)
                .orElseThrow(
                        () -> new ApiRequestException("user와 project mapping을 찾지 못했습니다.")
                );

        // 유저가 북마크를 했다는 레코드드
        Optional<Bookmark> bookmark = bookmarkRepository.findByUserAndNote(user, note);
        //북마크가 있다면 람다식을 :: 로 표현해줄 수 있음, 표현방법 정리 사이트, () -> bookmarkrepository.delete()랑 같은것
        bookmark.ifPresent((bookmark1 -> bookmarkRepository.delete(bookmark1)));

    }

    public NoteSearchInBookmarkResponseDto searchNoteInBookmarks(User currentUser, String rawKeyword){
        //위에 필드로 선언해주고 스프링 컨테이너에서 주입해줘서 객체로 잘 사용하는 중임
        List<String> keywordList = pandanUtils.parseKeywordToList(rawKeyword);
        // repository에서 꺼내오는 것은 어떤 방식으로도 꺼내올 수 있다. repository에 선언만 해준다면
        List<NoteEachSearchInBookmarkResponseDto> resultList = bookmarkRepository.findNotesByUserIdAndKeywordInBookmarks(currentUser.getUserId(), keywordList);

        return NoteSearchInBookmarkResponseDto.builder().noteList(resultList).build();
    }
}
