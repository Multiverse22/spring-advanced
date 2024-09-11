package org.example.expert.domain.manager.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.todo.service.TodoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
