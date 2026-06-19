package com.souravio.linkedInProject.postsService.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class PostDto {
    private Long id;
    private String content;
    private Long userId;
    private Long likes;
    private LocalDateTime createdAt;
}
