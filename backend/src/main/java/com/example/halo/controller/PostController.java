package com.example.halo.controller;

import com.example.halo.domain.Post;
import com.example.halo.repository.PostRepository;
import com.example.halo.service.EmbeddingService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

  private final PostRepository postRepository;
  private final EmbeddingService embeddingService;

  public PostController(PostRepository postRepository, EmbeddingService embeddingService) {
    this.postRepository = postRepository;
    this.embeddingService = embeddingService;
  }

  @GetMapping
  public List<Post> getPosts() {
    return postRepository.findAll();
  }

  @PostMapping
  public Post createPost(@RequestBody Post post) {
    // Save the base post first so vector failures do not block creation.
    Post savedPost = postRepository.save(post);
    String text = post.getTitle() + " " + post.getContent();
    float[] embedding = embeddingService.getEmbedding(text);

    if (embedding.length > 0) {
      postRepository.updateVector(savedPost.getId(), toVectorLiteral(embedding));
    }

    return savedPost;
  }

  @GetMapping("/search")
  public List<Post> searchPosts(@RequestParam String query) {
    float[] embedding = embeddingService.getEmbedding(query);
    if (embedding.length == 0) {
      return postRepository.findTop5ByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByCreatedAtDesc(query, query);
    }
    try {
      return postRepository.findNearestNeighbors(toVectorLiteral(embedding), 5);
    } catch (Exception e) {
      return postRepository.findTop5ByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByCreatedAtDesc(query, query);
    }
  }

  private String toVectorLiteral(float[] embedding) {
    return Arrays.toString(embedding);
  }
}
