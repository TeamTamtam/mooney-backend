# Mooney Backend

## 소개
본 프로젝트는 Spring Boot 기반의 백엔드 서버로, JPA, Spring Security, JWT, PostgreSQL, Redis, Spring AI(OpenAI API) 등 다양한 기술을 활용합니다.

---

## 사전 설치/필요 항목

1. **Java 21 (JDK)**
   - **설치 방법**  
     - Windows: [Temurin/Oracle 사이트](https://adoptium.net/temurin/releases/?version=21)에서 `.msi` 내려받아 설치  
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
     - Windows: [PostgreSQL 공식 사이트](https://www.postgresql.org/download/)에서 설치 마법사 실행  
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
     - macOS (Homebrew 권장):  
       ```bash
       brew install redis
       ```  
     - Linux (Debian/Ubuntu):  
       ```bash
       sudo apt update
       sudo apt install -y redis-server
       ```  
     - Windows: WSL 또는 Redis for Windows 활용  
   - **서버 실행**  
     ```bash
     redis-server
     ```

5. **Docker** (선택)
   - Windows/macOS: [Docker Desktop](https://www.docker.com/products/docker-desktop/) 설치  
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
- 예시 파일(`src/main/resources/application-example.yml`)이 제공됩니다. 이 파일의 이름을 `application.yml`로 변경한 뒤, 각 항목에 실제 값을 채워넣어야 합니다. datasource(PostgreSQL), Redis, OpenAI API 키 등의 환경변수를 본인의 환경에 맞게 수정하세요.

### 3. 빌드
```bash
./gradlew build
```

### 4. 실행
```bash
# 로컬 실행
./gradlew bootRun

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