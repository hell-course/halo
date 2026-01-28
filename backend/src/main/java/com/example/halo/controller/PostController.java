package com.example.halo.controller;

import com.example.halo.domain.Post;
import com.example.halo.repository.PostRepository;
import com.example.halo.service.EmbeddingService;
import com.pgvector.PGvector;
import org.springframework.web.bind.annotation.*;

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
    PGvector vector = embeddingService.embed(post.getTitle() + " " + post.getContent());
    post.setVector(vector);
    return postRepository.save(post);
  }

  @GetMapping("/search")
  public List<Post> searchPosts(@RequestParam String query) {
    PGvector embedding = embeddingService.embed(query);
    return postRepository.findNearestNeighbors(embedding, 5);
  }
}
