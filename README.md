# ☕ CoffeeShop

spring 3기 권지원 CH 6 실전 - K사 서버 개발 과제 제출합니다.  
카페 주문 및 결제 시스템으로, 동시성 제어·이벤트 기반 아키텍처·실시간 모니터링을 학습하기 위한 프로젝트입니다.

---

## 1. 프로젝트 개요

사용자가 메뉴를 조회하고 주문·결제·취소를 수행하는 카페 서비스입니다. 주문 생성 후 10분 내 미결제 시 자동 취소되며, 결제/취소 이벤트는 Kafka를 통해 비동기로 알림·메트릭·랭킹 시스템에 전달됩니다.  
마일스톤  
<img width="752" height="303" alt="image" src="https://github.com/user-attachments/assets/3532d10e-cac5-4acc-ba0c-63db0eaa082e" />

### 😽 주요 기능

- 메뉴 조회 (카테고리·키워드 필터링, 페이징)
- 주문 생성 → 결제 → 취소 흐름
- 포인트 충전·사용·적립 (10% 적립)
- 인기 메뉴 Top 3 실시간 랭킹 (Redis Sorted Set)
- 미결제 주문 자동 취소 (Scheduler + 낙관적 락)
- 결제 메트릭 수집 및 Grafana 대시보드


### 😽 기타 
🐥 프로젝트 개발 문서 정책  
https://www.notion.so/CH-6-35555c64ceb280188642d88922a0bea5?source=copy_link  
  
🐥 트러블 슈팅  
https://kjw81024.tistory.com/88  

🐥 개발 기간  
2026.05.04(월) ~ 2026.05.11(월)


---

## 2. 기술 스택

| 분류 | 기술 | 버전 |
|------|------|------|
| Language | Java | 17 |
| Framework | Spring Boot | 3.5.14 |
| ORM | Spring Data JPA | - |
| Query | QueryDSL | 5.0.0 |
| Database | MySQL | 8.0 |
| Cache / Ranking | Redis (Lettuce) | latest |
| 분산 락 | Redisson | 3.45.0 |
| Message Broker | Apache Kafka (KRaft) | 3.7.0 |
| Monitoring | Prometheus + Grafana | - |
| Metrics | Micrometer + Actuator | - |
| Validation | Spring Boot Starter Validation | - |
| Build Tool | Gradle | - |
| Containerization | Docker Compose | - |

---

## 3. 아키텍처

```
┌─────────────┐       ┌─────────────────────────────────────────┐
│   Client    │─────▶│            Spring Boot App              │
└─────────────┘       │                                         │
                      │  ┌─────────┐  ┌──────────┐  ┌─────────┐ │
                      │  │ Order   │  │  Point   │  │  Menu   │ │
                      │  │ Service │  │  Service │  │  Service│ │
                      │  └────┬────┘  └────┬─────┘  └────┬────┘ │
                      │       │            │             │      │
                      │  ┌────▼────────────▼─────────────▼────┐ │
                      │  │       MySQL (JPA + QueryDSL)       │ │
                      │  └────────────────────────────────────┘ │
                      │                                         │
                      │  ┌──────────────┐    ┌────────────────┐ │
                      │  │ Redisson Lock│    │ Redis Cache    │ │
                      │  │ (Point/Stock)│    │ (Ranking/ZSet) │ │
                      │  └──────┬───────┘    └───────┬────────┘ │
                      │         └────────┬───────────┘          │
                      └──────────────────┼──────────────────────┘
                                         │
                      ┌──────────────────▼───────────────────────┐
                      │         Kafka (payment-events)           │
                      │                                          │
                      │  ┌──────────────┐  ┌──────────────────┐  │
                      │  │ notification │  │ metrics-group    │  │
                      │  │ -group       │  │ (Micrometer)     │  │
                      │  └──────────────┘  └───────┬──────────┘  │
                      │  ┌──────────────┐          │             │
                      │  │ menu-ranking │          ▼             │
                      │  │ -group       │  ┌──────────────────┐  │
                      │  └──────────────┘  │ Prometheus       │  │
                      └────────────────────│ → Grafana        │──┘
                                           └──────────────────┘
```

---

## 4. ERD
<img width="1591" height="734" alt="스크린샷 2026-05-10 233200" src="https://github.com/user-attachments/assets/2f3fcb18-fb5d-49d8-bde4-67277fdb0e25" />



| 테이블 | 설명 |
|--------|------|
| `members` | 사용자 정보 및 보유 포인트 |
| `menus` | 메뉴 정보 (이름, 가격, 재고, 카테고리, 상태) |
| `orders` | 주문 정보 (상태: PENDING → PAID / CANCELLED) |
| `order_items` | 주문 상품 (주문 시점의 가격 보존) |
| `point_histories` | 포인트 변동 이력 (충전, 사용, 적립) |

---

## 5. 패키지 구조

```
com.example.coffeeshop
├── common
│   ├── annotation          // @DistributedLock
│   ├── aop                 // DistributedLockAspect
│   ├── config
│   │   ├── kafka           // KafkaConsumerConfig, KafkaProducerConfig, KafkaTopic
│   │   ├── JpaConfig
│   │   └── RedisConfig
│   ├── dto                 // ApiResponse, ErrorResponse
│   ├── exception           // ErrorCode, GlobalExceptionHandler, ServiceException
│   └── util                // AutoCancelScheduler, InitData
│
├── domain
│   ├── member
│   │   ├── controller / dto / entity / repository / service
│   │
│   ├── menu
│   │   ├── controller / dto / entity
│   │   ├── repository      // MenuRepository, MenuRepositoryCustom, MenuRepositoryImpl
│   │   └── service         // MenuService, MenuRankingService
│   │
│   └── order
│       ├── consumer        // NotificationConsumer, MetricsConsumer, MenuRankingConsumer
│       ├── controller / dto / entity
│       ├── producer        // PaymentEventListener
│       ├── repository
│       └── service         // OrderService, StockService, StockLockService, PointLockService
│
└── CoffeeshopApplication
```

---

## 6. 실행 방법

### (1) 사전 준비 🐣

- Java 17
- Docker & Docker Compose

### (2) 인프라 실행 🐣 

```bash
# 프로젝트 루트에서 Docker Compose 실행
docker compose up -d
```

아래 컨테이너가 실행됩니다:

| 서비스 | 포트 | 설명 |
|--------|------|------|
| MySQL | 3306 | 데이터베이스 |
| Redis | 6379 | 캐시, 랭킹, 분산 락 |
| Kafka Broker 1 | 9092 | 메시지 브로커 |
| Kafka Broker 2 | 9093 | 메시지 브로커 |
| Kafka UI | 8088 | Kafka 토픽/메시지 모니터링 |
| RedisInsight | 5540 | Redis 데이터 모니터링 |
| Prometheus | 9090 | 메트릭 수집 |
| Grafana | 3000 | 대시보드 (초기 비밀번호: admin / admin) |

### (3) 애플리케이션 실행 🐣 

```bash
./gradlew bootRun
```

애플리케이션이 `http://localhost:8080`에서 실행됩니다.

### (4) 인프라 종료 🐣 

```bash
docker compose down
```

데이터를 완전히 삭제하려면:

```bash
docker compose down -v
```

---

## 7. API 엔드포인트

| Method | URL | 설명 |
|--------|-----|------|
| `GET` | `/api/menus` | 메뉴 목록 조회 (카테고리, 키워드, 페이징) |
| `GET` | `/api/menus/{menuId}` | 메뉴 단건 조회 |
| `GET` | `/api/menus/popular` | 인기 메뉴 Top 3 |
| `POST` | `/api/orders` | 주문 생성 |
| `POST` | `/api/orders/{orderId}/pay` | 주문 결제 |
| `POST` | `/api/orders/{orderId}/cancel` | 주문 취소 |
| `POST` | `/api/points/charge/{memberId}` | 포인트 충전 |
| `GET` | `/api/points/{memberId}` | 사용자 정보 조회 |

---

## 8. 기술적 의사결정

| 주제 | 선택 | 이유 |
|------|------|------|
| 엔티티 참조 | ID Reference | JPA 연관관계 대신 ID 참조로 결합도 최소화, N+1 방지 |
| 분산 락 | Redisson (AOP) | pub/sub 기반으로 Redis 부하 적음, 커스텀 어노테이션으로 선언적 적용 |
| 이벤트 발행 | @TransactionalEventListener | 트랜잭션 커밋 후 Kafka 전송으로 DB-이벤트 정합성 보장 |
| 자동 취소 | Scheduler + 낙관적 락 | 매분 실행, @Version으로 동시성 충돌 방어 |
| 인기 메뉴 | Redis Sorted Set + 캐시 | ZSet으로 실시간 점수 적산, 조회 결과 1시간 캐시 |
| Consumer Group | 역할별 분리 | 알림·메트릭·랭킹 독립 소비, 장애 격리 |

---

## 9. 적용 기술 상세

### 🙀 공통 응답 처리 (ApiResponse, GlobalExceptionHandler)
API 응답 구조를 `ApiResponse`로 통일하여 성공/실패 응답 형식을 일관되게 유지했습니다. `GlobalExceptionHandler`에서 비즈니스 예외(`ServiceException`), Validation 예외, 미처리 예외를 계층별로 분리하여 처리합니다.

### 🙀 Validation
`@Valid`, `@NotNull`, `@NotEmpty`, `@Min`, `@Range` 등을 활용해 요청 데이터의 필수값·범위를 사전 검증하여 잘못된 요청이 서비스 레이어에 도달하지 않도록 했습니다.

### 🙀 Redisson 분산 락 (@DistributedLock)
포인트 충전·사용과 재고 차감·복구 시 동시성 문제를 방지하기 위해 Redisson 기반 분산 락을 도입했습니다. 커스텀 어노테이션 `@DistributedLock`과 AOP를 활용하여 비즈니스 로직과 락 관리를 분리하고, SpEL로 리소스 단위(사용자 ID, 메뉴 ID)의 동적 키를 생성합니다.

### 🙀 Kafka 이벤트 기반 아키텍처
결제/취소 이벤트를 Kafka 단일 토픽(`payment-events`)으로 발행하고, 역할별 Consumer Group(알림·메트릭·랭킹)이 독립적으로 소비합니다. `@TransactionalEventListener(AFTER_COMMIT)`를 사용하여 트랜잭션 커밋 후에만 이벤트를 발행함으로써 DB와 이벤트 간의 정합성을 보장합니다.

### 🙀 Redis 캐시 및 랭킹
인기 메뉴 랭킹을 Redis Sorted Set으로 실시간 적산하고, 조회 결과를 1시간 TTL로 캐싱하여 반복 조회 비용을 줄였습니다. `StringRedisTemplate`을 사용한 직접 관리 방식으로 프로젝트 전체의 Redis 사용 패턴과 일관성을 유지했습니다.

### 🙀 Scheduler (미결제 자동 취소)
`@Scheduled`로 매분 실행되며, 주문 생성 후 10분 내 미결제 주문을 자동 취소합니다. JPA `@Version` 기반 낙관적 락으로 사용자 결제/취소 요청과의 동시성 충돌을 방어하며, 건별 예외 처리로 한 건의 실패가 나머지에 영향을 주지 않도록 격리했습니다.

### 🙀 Prometheus + Grafana 모니터링
Micrometer를 통해 결제 소요시간(`payment.duration`), 성공 건수(`payment.success.count`), 취소 건수(`payment.cancel.count`)를 수집하고, Prometheus → Grafana 파이프라인으로 실시간 대시보드를 구성했습니다.

### 🙀 QueryDSL
메뉴 목록 조회 시 카테고리·키워드 조건에 따라 동적 쿼리를 생성하기 위해 QueryDSL을 사용했습니다. 타입 안전한 쿼리 작성과 조건별 분기를 깔끔하게 처리할 수 있습니다.

### 🙀 Docker Compose
MySQL, Redis, Kafka(2-broker KRaft), Kafka UI, RedisInsight, Prometheus, Grafana를 하나의 `docker-compose.yml`로 관리하여, 단일 명령어로 전체 인프라를 실행할 수 있도록 구성했습니다.

### 🙀 Postman
API 개발 과정에서 Postman을 활용하여 요청/응답 구조를 검증하고, 엔드포인트별 단위 테스트를 수행했습니다.


# 🌻 🌾 🌾 🐈 🌻 🌿 🌻 🌻 🌿 🌾 🌾 🌾 🌿 🌻 🌻 🌻 🐆 
