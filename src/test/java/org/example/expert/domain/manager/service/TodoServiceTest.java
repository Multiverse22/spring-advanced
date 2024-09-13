package org.example.expert.domain.manager.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;
    @InjectMocks
    private TodoService todoService;

    @Test
    public void 일정_조회_정상동작() {
        //given
        long todoId = 1L;

        given(todoRepository.findByIdWithUser(anyLong())).
                willReturn(Optional.of(new Todo()));
        //when
        TodoResponse todoResponse = todoService.getTodo(todoId);
        //then

        assertNotNull(todoResponse);
    }

    @Test
    public void 일정을_저장할_때_contents가_null인_경우() {
        //given
        long userId = 1L;
        AuthUser authUser = new AuthUser(userId,"asd@gmail.com", UserRole.USER);
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title",null);

        given(weatherClient.getTodayWeather()).willReturn("맑음");

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            todoService.saveTodo(authUser, todoSaveRequest);
        });

        assertEquals("Contents is required",exception.getMessage());
    }
    @Test
    public void 일정을_저장할_때_weather가_null인_경우() {
        //given
        long userId = 1L;
        AuthUser authUser = new AuthUser(userId,"asd@gmail.com", UserRole.USER);
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title","contents");

        given(weatherClient.getTodayWeather()).willReturn(null);

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            todoService.saveTodo(authUser, todoSaveRequest);
        });

        assertEquals("Weather is required",exception.getMessage());
    }

    @Test
    public void 일정_저장_정상작동() {
        long userId = 1L;
        AuthUser authUser = new AuthUser(userId,"asd@gmail.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title","contents");
        Todo todo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                "바람",
                user
        );
        given(weatherClient.getTodayWeather()).willReturn("바람");
        //Todo savedTodo = todoRepository.save(newTodo) 를 실행하면 객체의 동등성
        //문제가 생길수 있으니 any(Todo.class) 와같이 주고 리턴을 todo로 받는식으로 코드를 작성하자.
        given(todoRepository.save(any(Todo.class))).willReturn(todo);

        TodoSaveResponse todoSaveResponse = todoService.saveTodo(authUser, todoSaveRequest);

        assertNotNull(todoSaveResponse);
        assertEquals(todoSaveResponse.getContents(),"contents");
        assertEquals(todoSaveResponse.getTitle(),"title");
        assertEquals(todoSaveResponse.getUser().getId(),user.getId());
        assertEquals(todoSaveResponse.getUser().getEmail(),user.getEmail());
    }
}
