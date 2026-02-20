package com.example.halo.repository;

import com.example.halo.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  @Query(value = """
      SELECT id, title, content, author, created_at
      FROM post
      WHERE vector IS NOT NULL
      ORDER BY vector <=> CAST(:embedding AS vector)
      LIMIT :limit
      """, nativeQuery = true)
  List<Post> findNearestNeighbors(
          @Param("embedding") String embedding,
          @Param("limit") int limit
  );

  @Modifying
  @Transactional
  @Query(value = "UPDATE post SET vector = CAST(:embedding AS vector) WHERE id = :id", nativeQuery = true)
  int updateVector(@Param("id") Long id, @Param("embedding") String embedding);

  List<Post> findTop5ByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByCreatedAtDesc(
      String titleKeyword,
      String contentKeyword
  );
}
