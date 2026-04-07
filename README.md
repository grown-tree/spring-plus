# Spring Plus 과제
## 구현 기능 목록
### Level 1
**1. @Transactional 이해**
- `TodoService.saveTodo()` 메서드에 `@Transactional` 누락 문제 수정

**2. JWT 이해**
- `User` 테이블에 `nickname` 컬럼 추가
- JWT 토큰 생성 시 `nickname` 클레임 포함
- `JwtFilter`, `AuthUser`, `ArgumentResolver` 에 nickname 전파

**3. JPA 이해**
- `weather` 조건 검색 (optional)
- 수정일 기준 기간 검색 `startAt`, `endAt` (optional)
- JPQL + `LEFT JOIN FETCH` 로 N+1 방지

### Level 2

**6. JPA Cascade**
- `Todo` 생성 시 생성자에서 `Manager` 자동 등록
- `CascadeType.PERSIST` 로 `Todo` 저장 시 `Manager` 함께 저장

**7. N+1 문제 해결**
- `CommentRepository.findByTodoIdWithUser()` 에 `JOIN FETCH` 추가
- 댓글 조회 시 유저 정보 한 번에 로딩

**8. QueryDSL**
- `findByIdWithUser` JPQL → QueryDSL 변경
- `fetchJoin()` 으로 N+1 방지

**9. Spring Security**
- 기존 `JwtFilter` + `FilterRegistrationBean` 방식 → Spring Security 전환
- `JwtSecurityFilter` (`OncePerRequestFilter`) 구현
    - JWT 검증 후 `SecurityContextHolder` 에 인증 정보 저장
    - `userId`, `userRole` → SecurityContext 저장
    - `email`, `nickname` → `request.setAttribute` 저장
- `SecurityConfig` 구현
    - `/auth/**` 전체 허용
    - `/admin/**` ADMIN 권한만 허용
    - 나머지 요청 인증 필요
    - 토큰 없는 요청 시 `403` → `401` 반환하도록 `authenticationEntryPoint` 설정
- `AuthUserArgumentResolver` 수정
    - 기존 `request.getAttribute()` → `SecurityContextHolder` 에서 인증 정보 조회

### Level 3
**10. QueryDSL 검색 기능**
- 검색 API: `GET /todos/search`
- 검색 조건 (모두 optional)
    - 제목 키워드 부분 일치 검색
    - 담당자 닉네임 부분 일치 검색
    - 생성일 범위 검색 (`startDate`, `endDate`), 최신순 정렬
- `Projections` 활용하여 필요한 필드만 반환
    - 일정 제목
    - 담당자 수
    - 총 댓글 개수
- 페이징 처리 적용 (기본 size: 10)
- `BooleanExpression` 으로 동적 쿼리 구현 (null 조건 자동 무시)
- `leftJoin` 으로 담당자/댓글 0개인 일정도 검색 결과에 포함

**11. Transaction 심화**
- 매니저 등록 요청 시 로그를 `log` 테이블에 저장
- `@Transactional(propagation = Propagation.REQUIRES_NEW)` 활용
  - 매니저 등록 트랜잭션과 로그 트랜잭션을 완전히 분리
  - 매니저 등록 실패(롤백) 시에도 로그는 반드시 저장됨

#### 로그 엔티티 설계
| 필드 | 타입 | 설명 | 
|---|---|---|
| `id` | Long | PK |
| `todoId` | Long | 어떤 일정에 대한 요청인지 |
| `requestUserId` | Long | 매니저 등록을 요청한 유저 |
| `managerUserId` | Long | 등록하려는 담당자 유저 |
| `status` | String | 성공(`SUCCESS`) / 실패(`FAIL`) |
| `message` | String | 실패 시 에러 메시지 |
| `createdAt` | LocalDateTime | 로그 생성 시간 |

#### 로그 엔티티 설계 이유
- `todoId`, `requestUserId`, `managerUserId` → 어떤 유저가 어떤 일정에 누구를 등록하려 했는지 추적 가능
- `status` → 성공/실패 여부를 한눈에 파악
- `message` → 실패 원인 기록으로 디버깅 용이
- `createdAt` → 언제 요청이 발생했는지 시간 추적 (과제 필수 조건)
- `REQUIRES_NEW` 사용 이유 → 매니저 등록 트랜잭션이 롤백되어도 로그 트랜잭션은 독립적으로 커밋되어야 하기 때문


---

## 기술 스택
- Java 17
- Spring Boot
- Spring Security
- JPA / QueryDSL
- JWT
- MySQL