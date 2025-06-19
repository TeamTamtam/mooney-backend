# mooney-backend

**Mooney 프로젝트의 백엔드 서버 리포지토리입니다.**
이 서버는 사용자 정보, 소비 데이터, 예산 관리, 챌린지 생성 및 통계 조회 등 **Mooney 전체 기능의 핵심 비즈니스 로직**을 담당합니다. Spring Boot, JPA, PostgreSQL, Redis, Spring Security, JWT, OpenAI API 등의 기술을 기반으로 구현되어 있습니다.

## ✨ 프로젝트 개요

**Mooney(무니)** 는 예산 내 소비에 어려움을 겪는 **Z세대**를 위한 AI 기반 절약 가계부 서비스입니다.
사용자가 스스로 설정한 예산 안에서 **지속 가능한 소비 습관**을 형성할 수 있도록 다음과 같은 기능을 제공합니다:

* 📊 **Prophet 기반 시계열 예측 모델**을 통해 **다음 주 과소비 예상 카테고리 자동 탐지**
* 🎯 지출 습관 개선을 유도하는 **맞춤형 절약 챌린지 생성**
* 💬 GPT-4o-mini 기반 챗봇 **‘똑똑소비봇’** 으로 예산 내 소비 가능 여부 실시간 조언
* 🧩 소비 성공 시 **경험치, 캐릭터 해금, UI 변화 등 게이미피케이션 요소 제공**

무니는 단순한 기록형 가계부가 아닌, 사용자와 상호작용하며 소비 습관을 바꾸는 **AI 소비 파트너**입니다.
**Mooney Backend**는 이러한 기능을 실현하기 위해 다음과 같은 역할을 수행합니다:

* FastAPI 기반 AI 서버와의 통신 및 예측 결과 처리
* 사용자 인증 및 보안 처리 (JWT, Spring Security)
* PostgreSQL 기반 예산/소비/챌린지 데이터 저장 및 로직 관리
* Redis 기반 캐시 처리 및 실시간 처리 최적화

---

## 사전 설치/필요 항목

1. **Java 21 (JDK)**
   - **설치 방법**  
     - Windows: [Temurin 공식 사이트](https://adoptium.net/temurin/releases/?version=21)에서 JDK21-LTS Windows 버전 내려받아 설치  
     - macOS (Homebrew 권장):  
       ```bash
       brew install openjdk@21
       ```  
     - Linux (Debian/Ubuntu):  
       ```bash
       sudo apt update
       sudo apt install -y openjdk-21-jdk
       ```

2. **Gradle** (프로젝트 포함)
   - 프로젝트 내 `gradlew`(Wrapper) 사용 권장 (별도 설치 불필요)

3. **PostgreSQL**
   - **설치 방법**  
     - Windows: [PostgreSQL 공식 사이트](https://www.postgresql.org/download/)에서 Windows 버전 내려받아 설치
     - macOS (Homebrew 권장):  
       ```bash
       brew install postgresql
       ```  
     - Linux (Debian/Ubuntu):  
       ```bash
       sudo apt update
       sudo apt install -y postgresql postgresql-contrib
       ```

4. **Redis**
   - **설치 방법**
       - Windows: [Redis 공식 사이트](https://redis.io/download)에서 Windows 버전 내려받아 설치
           - macOS (Homebrew 권장):  
       ```bash
       brew install redis
       ```  
     - Linux (Debian/Ubuntu):  
       ```bash
       sudo apt update
       sudo apt install -y redis-server
       ```  
   - **서버 실행**  
     ```bash
     redis-server
     ```

5. **Docker** (선택)
   - Windows/macOS: [Docker Desktop](https://www.docker.com/products/docker-desktop/)에서 Windows 버전 내려받아 설치  
   - Linux:  
     ```bash
     sudo apt update
     sudo apt install -y docker.io
     sudo systemctl start docker
     sudo usermod -aG docker $USER
     ```

6. **OpenAI API 키**
   - [OpenAI 플랫폼](https://platform.openai.com/)에서 발급 후 `application.yml`에 입력

---

## 설치 및 실행 방법

### 1. 깃 레포지토리 다운로드 또는 클론
- git 명령어로 클론:
```bash
git clone https://github.com/TeamTamtam/mooney-backend.git
cd mooney-backend
```

### 2. 환경 변수 설정
- 예시 파일(`src/main/resources/application-example.yml`)이 제공됩니다. 이 파일의 이름을 `application.yml`로 변경해 각 항목에 실제 값을 채워넣거나, `application.yml` 파일을 새로 작성해야 합니다. datasource(PostgreSQL), Redis, OpenAI API 키 등의 환경변수를 본인의 환경에 맞게 수정하세요.
- 예: 
```bash
cat > application.yml
# 준비된 내용을 복사 붙여넣고, ctrl+c로 저장
```

### 3. 빌드
```bash
cd mooney
./gradlew build -x test # 테스트는 빌드 생략
```

### 4. 실행
```bash
# 로컬 실행
./gradlew bootRun -x test

# 또는 빌드된 JAR로 실행
java -jar build/libs/*.jar
```

### 5. Docker로 실행
```bash
docker build -t mooney-backend .
docker run -p 8080:8080 mooney-backend
```
### 6. API 명세 확인
- 서버가 정상 실행되면 `http://localhost:8080/swagger-ui.html`에 접속하여 Swagger UI로 API 명세를 확인할 수 있습니다.

---

## 데이터베이스
- PostgreSQL 사전 설치 및 유저네임, 비밀번호 설정이 필요합니다.
- Redis 설정 및 서버 실행이 필요합니다.
- `application.yml`에 접속 정보를 입력하세요.

---

## 외부 라이브러리
- Spring Boot, Spring Data JPA, Spring Security, Spring AI, Swagger 등
- 자세한 의존성 목록은 `build.gradle`을 참고 부탁드립니다.

---

## 참고/문서
- [Spring Boot 공식문서](https://spring.io/projects/spring-boot)
- [Gradle 공식문서](https://docs.gradle.org)
