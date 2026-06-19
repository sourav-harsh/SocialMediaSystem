package com.souravio.linkedInProject.postsService.service;

import com.souravio.linkedInProject.postsService.auth.AuthContextHolder;
import com.souravio.linkedInProject.postsService.entity.Post;
import com.souravio.linkedInProject.postsService.entity.PostLike;
import com.souravio.linkedInProject.postsService.event.PostLiked;
import com.souravio.linkedInProject.postsService.exception.BadRequestException;
import com.souravio.linkedInProject.postsService.exception.ResourceNotFoundException;
import com.souravio.linkedInProject.postsService.repository.PostLikeRepository;
import com.souravio.linkedInProject.postsService.repository.PostRepository;
import java.math.BigInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private final KafkaTemplate<Long, PostLiked> postLikedKafkaTemplate;

    @Transactional
    public void likePost(Long postId) {
        Long userId = AuthContextHolder.getCurrentUserId();
        log.info("User with ID: {} liking the post with ID: {}", userId, postId);

        Post post = postRepository.findById(postId).orElseThrow(()
                -> new ResourceNotFoundException("Post not found with ID: "+postId));

        boolean hasAlreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if(hasAlreadyLiked) throw new BadRequestException("You cannot like the post again");

        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);
        postLikeRepository.save(postLike);

        // TODO: we can remove to count the value of likes on the fly
        post.setLikes(post.getLikes() + 1);
        postRepository.save(post);

//        TODO: send notification to the owner of the post
        PostLiked postLiked = PostLiked.builder()
                .postId(postId)
                .likedByUserId(userId)
                .ownerUserId(post.getUserId())
                .build();
        postLikedKafkaTemplate.send("post_liked_topic", postLiked);
    }


    @Transactional
    public void unlikePost(Long postId) {
        Long userId = AuthContextHolder.getCurrentUserId();
        log.info("User with ID: {} unliking the post with ID: {}", userId, postId);

        Post post = postRepository.findById(postId).orElseThrow(()
                -> new ResourceNotFoundException("Post not found with ID: "+postId));

        boolean hasAlreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if(!hasAlreadyLiked) throw new BadRequestException("You cannot unlike the post that you have not liked yet");

        postLikeRepository.deleteByUserIdAndPostId(userId, postId);

    // TODO: we can remove to count the value of likes on the fly
        post.setLikes(post.getLikes() - 1);
        postRepository.save(post);
    }

    public Long getAllLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }
}
