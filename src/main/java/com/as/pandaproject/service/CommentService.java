package com.as.pandaproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentService {
    //지금까지 서비스단에서 repository가 자기 것만 있는 것이 깔끔하다고 생각했었는데, 복잡한 프로젝트에선 그리고 이번 프로젝트 상황을 보아하니 이게 맞나보다.
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final CommentRepository commentRepository;
    private final UserProjectMappingRepository userProjectMappingRepository;

    //CommentCreateResponseDto, CommentCreateRequestDto를 보아하니, 넣고 뽑을 때, Dto 형식으로 넣고 많이 뽑는 것 같다. DB저장방식이 무엇일까?
    //빌더 패턴으로 CommentCreateResponseDto, CommentCreateRequestDto를 초기화 해주는 모습을 볼 수 있었다.
    //지금까지 Domain은 많이 생성자를 다시 만들어가면서 초기화를 해주었지만, Dto를 초기화 해주는 모습을 보아하니, Dto를 자동주입 이외에도 직접적으로 값을 많이 대입하여 활용한다는 것을 생각해볼 수 있었다.
    public CommentCreateResponseDto createComment(Long noteId, User currentUser, CommentCreateRequestDto commentCreateRequestDto) {
        User user = userRepository.findById(currentUser.getUserId()).orElseThrow(
                () -> new ApiRequestException("등록되지 않은 유저의 접근입니다.")
        );

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
        //빌더 패턴으로 응답 받은 것을 Comment에 적용하여 저장하면 쉽게 저장이 된다. 이번 프로젝트에서도 적용을 시켜볼 수 있을 것 같다.
        Comment newComment = Comment.builder()
                .user(user)
                .note(note)
                .content(commentCreateRequestDto.getContent())
                .build();
        //Comment를 통해서 저장하는 과정
        Comment savedComment = commentRepository.save(newComment);

        return CommentCreateResponseDto.builder()
                .content(savedComment.getContent())
                .commentId(savedComment.getCommentId())
                .writer(savedComment.getUser().getName())
                .build();
    }
    //마찬가지로 Dto로 내보내려고 준비한다.
    public CommentReadListResponseDto readComments(Long noteId, User currentUser) {
        User user = userRepository.findById(currentUser.getUserId()).orElseThrow(
                () -> new ApiRequestException("등록되지 않은 유저의 접근입니다.")
        );

        Note note = noteRepository.findById(noteId).orElseThrow(
                () -> new ApiRequestException("생성되지 않은 노트입니다.")
        );

        //쿼리
        Project connectedProject = Optional.ofNullable(note.getProject()).orElseThrow(
                () -> new ApiRequestException("연결된 프로젝트가 없습니다.")
        );

        //쿼리
        //UserProjectMapping 들어가보니 다대다 관계인 유저와 프로젝트 사이에 넣어주어 다대일 일대다 관계로 만들어주었다.
        UserProjectMapping userProjectMapping = userProjectMappingRepository.findByUserAndProject(user, connectedProject)
                .orElseThrow(
                        () -> new ApiRequestException("user와 project mapping을 찾지 못했습니다.")
                );

        List<Comment> commentList = commentRepository.findByNoteId(noteId);
        //.stream().sorted(Comparator.comparsing(Comment::getCreatedAt)) 정렬기능: Comment를 getCreatedAt 기준으로 오름차순 정렬한다 .reversed() 해주면 역순정렬가능
        //.collect(Collectors.toList()): Stream<String> -> List<String> 으로 바꿔줌
        //비슷한 유형으로 Collectors.joining("") 으로 하면 String으로 바꿔줌
        List<Comment> commentListSortedByCreatedAt = commentList.stream().sorted(Comparator.comparing(Comment::getCreatedAt)).collect(Collectors.toList());
        List<CommentReadEachResponseDto> commentReadEachResponseDtoList =
                commentListSortedByCreatedAt
                        .stream()
                        .map(e -> CommentReadEachResponseDto.fromEntity(e))
                        .collect(Collectors.toList());
        //for문을 stream으로 대신할 수 있다./ map은 요소들을 특정조건에 해당하는 값으로 변환해줌
        //.stream().map(e -> CommentReadEachResponseDto.fromEntity(e)): 요소들을 Dto의 fromEntity메소드에 넣은 값으로 변경
        //.stream().map(s -> s.toUpperCase()) 과 같은 원리: 요소들을 대문자로 변경
        return CommentReadListResponseDto.builder()
                .commentList(commentReadEachResponseDtoList)
                .build();
    }

    // 댓글 수정
    //또 같은 방식
    @Transactional
    public CommentUpdateResponseDto updateComment(Long commentId, User currentUser, CommentUpdateRequestDto commentUpdateRequestDto) {

        Optional<Comment> maybeComment = commentRepository.findById(commentId);

        Comment updateComment = maybeComment
                .filter(c->c.getUser().getUserId().equals(currentUser.getUserId()))
                .map(c->c.update(commentUpdateRequestDto))
                .orElseThrow(() -> new ApiRequestException("댓글은 본인만 수정할 수 있습니다"));
        //.stream 으로 왜 안시작할까? 위에는 리스트를 스트림으로 변경, 아래는 리스트가 아니기 때문에? 라고 추론
        //.filter(c->c.getUser().getUserId().equals(currentUser.getUserId())): 댓글중에 유저 아이디가 썼던 댓글이 현재 유저 아이디와 쓴 댓글과 같으면 업데이트
        //일종의 데이터를 걸러주는 기능
        //.map(c->c.update(commentUpdateRequestDto)): 현재 댓글을 업데이트 해줌

        //이렇게 메소드화 해줘도 리턴으로 받을 수 있음, 왜냐하면 메소드의 변수를 Dto로 선언하였기 때문에, 같은원리로 하면 뭔가 프로젝트에 적용가능할거 같아보임
        return  CommentUpdateResponseDto.fromEntity(updateComment);
    }

    // 댓글 삭제
    @Transactional
    public CommentDeleteResponseDto deleteComment(Long commentId, User currentUser) {
        commentRepository.deleteByCommentIdAndUserId(commentId, currentUser.getUserId());
        //댓글과 유저아아디를 같이 지움 둘이 매핑되어있나?
        return CommentDeleteResponseDto.builder()
                .commentId(commentId)
                .build();

    }
}

