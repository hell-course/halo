package com.example.halo.repository;

import com.example.halo.domain.Post;
import com.pgvector.PGvector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
  // JpaRepository를 상속받는 것만으로 save(), findAll(), findById() 등을 바로 사용할 수 있습니다.

  @Query(value = "SELECT * FROM post ORDER BY vector <-> :embedding LIMIT :limit", nativeQuery = true)
  List<Post> findNearestNeighbors(@Param("embedding") PGvector embedding, @Param("limit") int limit);
}