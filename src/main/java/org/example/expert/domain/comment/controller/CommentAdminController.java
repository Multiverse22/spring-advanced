package org.example.expert.domain.comment.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.annotation.Admin;
import org.example.expert.domain.comment.service.CommentAdminService;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentAdminController {

    private final CommentAdminService commentAdminService;
    @Admin
    @DeleteMapping("/admin/comments/{commentId}")
    public void deleteComment(@Auth AuthUser authUser,
                              @PathVariable long commentId) {
        commentAdminService.deleteComment(authUser,commentId);
    }
}
