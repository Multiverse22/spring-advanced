package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;

    @Test
    public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, todoId, request);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void comment를_정상적으로_등록한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(result);
    }

    @Test
    public void comment_목록_조회_성공() {
        //given
        long todoId =1;
        CommentSaveRequest request1 = new CommentSaveRequest("contents1");
        CommentSaveRequest request2 = new CommentSaveRequest("contents2");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        ReflectionTestUtils.setField(todo, "id", todoId);
        Comment comment1 = new Comment(request1.getContents(), user, todo);
        Comment comment2 = new Comment(request2.getContents(), user, todo);

        List<Comment> commentList = List.of(comment1, comment2);

        given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(commentList);
        //when
        List<CommentResponse> commentResponses = commentService.getComments(todoId);

        //then
        assertEquals(2, commentResponses.size());
        assertEquals(comment1.getContents(),commentResponses.get(0).getContents());
        assertEquals(comment2.getContents(),commentResponses.get(1).getContents());
        assertEquals(commentResponses.get(0).getUser().getEmail(),commentResponses.get(1).getUser().getEmail());
    }
    @Test
    public void comment_등록중_user값이_null이라면() {
        long todoId =1;
        CommentSaveRequest request1 = new CommentSaveRequest("contents1");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, ()->
                commentService.saveComment(null, todoId, request1));

        assertEquals("user is null", exception.getMessage());
    }
    @Test
    public void commentSaveRequest의_contents가_null인경우() {
        long todoId =1;
        CommentSaveRequest request1 = new CommentSaveRequest(null);
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, ()->
                commentService.saveComment(authUser, todoId, request1));

        assertEquals("comment Contents is null",exception.getMessage());
    }

}
