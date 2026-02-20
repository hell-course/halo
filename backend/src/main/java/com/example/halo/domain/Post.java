package com.example.halo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;
  private String content;
  private String author;
  private LocalDateTime createdAt = LocalDateTime.now();

  public Post(String title, String content, String author) {
    this.title = title;
    this.content = content;
    this.author = author;
  }
}
