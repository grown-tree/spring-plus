package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    //기존
    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);
    //개선
    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user WHERE " +
            "(:weather IS NULL OR t.weather = :weather) AND " +//없으면null 있으면 해당조건으로 검색
            "(:startAt IS NULL OR t.modifiedAt >= :startAt) AND " +
            "(:endAt IS NULL OR t.modifiedAt <= :endAt) "+
            "ORDER BY t.modifiedAt DESC"
    )
    Page<Todo> findByCondition(
            @Param("weather") String weather,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            Pageable pageable);




    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);

}
