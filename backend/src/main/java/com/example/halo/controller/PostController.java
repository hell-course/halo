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

    // 1️⃣ 텍스트 합치기
    String text = post.getTitle() + " " + post.getContent();

    // 2️⃣ REST 기반 임베딩 생성 (float[])
    float[] embedding = embeddingService.getEmbedding(text);

    // 3️⃣ float[] → PGvector
    PGvector vector = new PGvector(embedding);

    post.setVector(vector);

    return postRepository.save(post);
  }

  @GetMapping("/search")
  public List<Post> searchPosts(@RequestParam String query) {

    // 1️⃣ 검색어 임베딩
    float[] embedding = embeddingService.getEmbedding(query);

    // 2️⃣ float[] → PGvector
    PGvector vector = new PGvector(embedding);

    // 3️⃣ pgvector 유사도 검색
    return postRepository.findNearestNeighbors(vector, 5);
  }
}
