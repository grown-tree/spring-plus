package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.request.TodoSearchRequest;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class TodoCustomRepositoryImpl implements TodoCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId){

        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()//N+1 방지
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchTodos(TodoSearchRequest request, Pageable pageable) {
        QTodo todo = QTodo.todo;
        QManager manager = QManager.manager;
        QComment comment = QComment.comment;

        List<TodoSearchResponse> results = queryFactory
                .select(Projections.constructor(TodoSearchResponse.class,//Projections을 활용하여 필요한 정보만
                        todo.title,//일정에 제목만
                        manager.count(),//담당자 수만
                        comment.count()//댓글 수만 추출
                ))
                .from(todo)
                .leftJoin(manager).on(manager.todo.eq(todo))//담당자나 댓글수 0개인것도 뽑기위해 leftJoin사용
                .leftJoin(comment).on(comment.todo.eq(todo))
                .where(
                        titleContains(request.getTitle()),
                        nicknameContains(request.getNickname()),
                        createdAtBetween(request.getStartDate(), request.getEndDate())
                )
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())//최신순 정렬
                .offset(pageable.getOffset())//조회시작 지점
                .limit(pageable.getPageSize())
                .fetch();

        //페이징 처리
        Long total = queryFactory
                .select(todo.count())
                .from(todo)
                .leftJoin(manager).on(manager.todo.eq(todo))
                .where(
                        titleContains(request.getTitle()),
                        nicknameContains(request.getNickname()),
                        createdAtBetween(request.getStartDate(), request.getEndDate())
                )
                .fetchOne();

        return new PageImpl<>(results, pageable, total == null ? 0 : total);
    }

    // 동적 쿼리 조건 (null이면 조건 무시)

    //제목 부분검색
    private BooleanExpression titleContains(String title) {
        return title != null ? QTodo.todo.title.contains(title) : null; //값 있으면 LIKE '%title%' 조건 추가
    }

    //닉네임 부분검색
    private BooleanExpression nicknameContains(String nickname) {
        return nickname != null ? QManager.manager.user.nickname.contains(nickname) : null;
    }

    // 생성일 범위 검색 4가지 경우에 수()
    private BooleanExpression createdAtBetween(LocalDate start, LocalDate end) {
        if (start == null && end == null) return null;//둘다없는경우
        if (start == null) return QTodo.todo.createdAt.loe(end.atTime(23, 59, 59));//끝 날찌만있는경우
        if (end == null) return QTodo.todo.createdAt.goe(start.atStartOfDay());//시작날짜만있는경우
        return QTodo.todo.createdAt.between(start.atStartOfDay(), end.atTime(23, 59, 59));//둘다 있는경우
    }
}
