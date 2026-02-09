# gateway-service

알록달록 MSA의 `단일 API 진입점`입니다. <br>
외부 요청은 `nginx(HTTPS/TLS)`를 거쳐 `gateway-service(Spring Cloud Gateway/WebFlux)` 로 들어오고, 게이트웨이가 각 서비스로 라우팅합니다.

---

## 필요한 이유
- 클라이언트가 여러 서비스 주소 몰라도 되도록 단일 엔드포인트 제공
- JWT 검증/공통 정책을 각 서비스에 중복 구현하지 않도록 함
- 서비스 구조 변경(서비스 추가 및 이동)이 발생해도 외부 API 경로를 안정적으로 유지 가능

---

## 역할
### 1) API 라우팅
요청 경로(prefix)기준으로 내부 서비스로 라우팅
-  `/users/**` → user-service
-  `/auth/**` → auth-service
- `/stores/**` → store-service
- `/products/**` → product-service
- `/orders/**` → order-service

### 2) JWT Access Token 필터링
> 발급/갱신은 auth-service담당
- `Authorization: Bearer <access-jwt>` 파싱
- 토큰 만료 등 정책에 따라
    - 실패 : return 401
    - 성공 : 내부 서비스로 컨텍스트 전달(ex_ `X-User-Id`, `X-Request-Id`)


### 3) 공통 헤더/ 추적 ID
- `X-Request-Id` 생성/전달 (로그/트레이싱용)
- 필요 시 테넌시 헤더(`X-Store-Id` 등)는 추후 확장
- 하나의 사용자 요청이 여러 서버를 거칠 때, "이 요청이 같은 요청이다"라고 묶어주는 역할
- JWT의 userId는 누가 보냈는지 / 여러 요청에 공통적 / 권한과 주체 식별용 이라면
- `X-Request-Id`는 이 요청이 뭔지 / 요청 1회당 유니크 / 추적과 디버깅용


---

## 로직
1. client → nginx → gateway-service
2. Gateway 필터에서 Access JWT 검증(만료, 유효성)
3. 라우팅 규칙에 따라서 해당 MSA 서비스로 프록시
4. 서비스 응답을 그대로 client에 반환하기





