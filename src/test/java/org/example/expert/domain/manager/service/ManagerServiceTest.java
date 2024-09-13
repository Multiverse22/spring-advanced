package org.example.expert.domain.manager.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;
    @InjectMocks
    private PasswordEncoder passwordEncoder;

    @Test
    public void manager_목록_조회_시_Todo가_없다면_IRE_에러를_던진다() {
        // given
        long todoId = 1L;
        //todoRepository.findById(todoId)를 했을때 아무것도 리턴하지않는다고 주어진다.
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Nested
    class saveManagerTest {
        @Test
        public void 일정_작성자와_jwt토큰을_발급받은_유저가_다른경우() {
            //given
            long todoId = 1L;
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            User user = User.fromAuthUser(authUser);
            ReflectionTestUtils.setField(user, "id", 1L);
            AuthUser anotherUSer = new AuthUser(2L, "aa@gmail.com", UserRole.USER);

            Todo todo = new Todo("title", "contents", "weather", user);
            ReflectionTestUtils.setField(todo, "id", 1L);

            long managerId = 3L;
            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerId);
            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

            //when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                    managerService.saveManager(anotherUSer, todoId, managerSaveRequest));

            //then
            assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 아닙니다.", exception.getMessage());
        }

        @Test
        public void 존재하지_않는_managerId로_manager_save하기() {
            //given
            long todoId = 1L;
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            User user = User.fromAuthUser(authUser);
            ReflectionTestUtils.setField(user, "id", 1L);

            Todo todo = new Todo("title", "contents", "weather", user);
            ReflectionTestUtils.setField(todo, "id", 1L);

            long managerId = 3L;
            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerId);
            given(userRepository.findById(managerSaveRequest.getManagerUserId())).willReturn(Optional.empty());
            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));


            //when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> managerService.saveManager(authUser, todoId, managerSaveRequest));

            //then
            assertEquals("등록하려고 하는 담당자 유저가 존재하지 않습니다.", exception.getMessage());

        }

        @Test
        void todo의_user가_null인_경우_예외가_발생한다() {
            // given
            long userId = 1L;
            AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);
            long todoId = 1L;
            long managerUserId = 2L;

            Todo todo = new Todo();
            ReflectionTestUtils.setField(todo, "user", null);

            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                    managerService.saveManager(authUser, todoId, managerSaveRequest)
            );

            assertEquals("일정에 담당자가 존재하지 않습니다.", exception.getMessage());
        }

        @Test
        public void 일정_작성자가_manager가_되려는_경우() {
            //given
            long todoId = 1L;
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            User user = User.fromAuthUser(authUser);
            ReflectionTestUtils.setField(user, "id", 1L);

            Todo todo = new Todo("title", "contents", "weather", user);
            ReflectionTestUtils.setField(todo, "id", 1L);

            long managerId = 1L;
            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerId);
            given(userRepository.findById(managerSaveRequest.getManagerUserId())).willReturn(Optional.of(user));
            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                    managerService.saveManager(authUser, todoId, managerSaveRequest));

            assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.", exception.getMessage());

        }

        @Test // 테스트코드 샘플
        public void manager_목록_조회에_성공한다() {
            // given
            long todoId = 1L;
            User user = new User("user1@example.com", "password", UserRole.USER);
            Todo todo = new Todo("Title", "Contents", "Sunny", user);
            ReflectionTestUtils.setField(todo, "id", todoId);

            Manager mockManager = new Manager(todo.getUser(), todo);
            List<Manager> managerList = List.of(mockManager);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

            // when
            List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

            // then
            assertEquals(1, managerResponses.size());
            assertEquals(mockManager.getId(), managerResponses.get(0).getId());
            assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
        }

        @Test
            // 테스트코드 샘플
        void todo가_정상적으로_등록된다() {
            // given
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

            long todoId = 1L;
            Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

            long managerUserId = 2L;
            User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
            ReflectionTestUtils.setField(managerUser, "id", managerUserId);

            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
            given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

            // then
            assertNotNull(response);
            assertEquals(managerUser.getId(), response.getUser().getId());
            assertEquals(managerUser.getEmail(), response.getUser().getEmail());
        }
    }
    @Nested
    class deleteManagerTest {
        @Test
        void todo가_없는경우() {
            long userId = 1L;
            long todoId = 1L;
            long managerUserID = 2L;
            AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);
            User user = User.fromAuthUser(authUser);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

            InvalidRequestException exception = assertThrows(InvalidRequestException.class,()->
                    managerService.deleteManager(authUser,todoId,managerUserID));

            assertEquals("Todo not found", exception.getMessage());

        }
        @Test
        void user가_없는경우() {
            long todoId = 1L;
            long managerUserId = 2L;
            long authUserId =3L;
            AuthUser authUser = new AuthUser(authUserId, "a@a.com", UserRole.USER);
            given(userRepository.findById(authUserId)).willReturn(Optional.empty());

            InvalidRequestException exception = assertThrows(InvalidRequestException.class,()->
                    managerService.deleteManager(authUser,todoId,managerUserId));
            assertEquals("User not found",exception.getMessage());
        }
        @Test
        void todo에_작성한user가_없는경우() {
            long userId = 1L;
            AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);
            long todoId = 1L;
            long managerUserId = 2L;
            User user = User.fromAuthUser(authUser);
            //todo에 user값이 null이다!!
            Todo todo = new Todo();
            ReflectionTestUtils.setField(todo, "user", null);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                    managerService.deleteManager(authUser,todoId,managerUserId));

            assertEquals("일정에 담당자가 존재하지 않습니다.", exception.getMessage());
        }
    }

}

