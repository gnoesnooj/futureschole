# 필수 과제 항목
## 1. 실행 방법
프로젝트의 루트 경로에서, 다음 명령어를 통해 전체 서비스를 실행합니다. 설치 및 생성되는 이미지 및 컨테이너는 `PostgreSQL`, `Grafana`, `이벤트 생성기 앱` 입니다.
> docker-compose up -d

필요 테스트 완료 후, 다음 명령어를 통해 전체 서비스를 종료합니다.
> docker-compose down -v

Grafana (`http://localhost:3000`) 접속 후, `Connections` - `Data sources` 페이지 접속 후 우측 상단 `Add new data source`버튼을 클릭합니다.
PostgreSQL을 검색 및 클릭 한 후, 다음과 같이 설정합니다.
> Host URL : postgres-db:5432 
> Database name : event_log_db 
> Username : dev_user 
> Password : dev_password

하단 Save & test 버튼을 통해 정상적으로 동작하는지 확인합니다.

이후 Dashboard에서 작업 시 datasource에서 방금 생성한 grafana connection을 활용하여 구성합니다.

## 2. 스키마 설명
### 2-1. 이벤트 생성 설계 및 이벤트 스키마

이벤트는 강의 사이트를 이용하는 유저들을 가정하고 설계하였습니다.

강의 구매, 강의 구매 실패, 강의 검색이라는 3개의 시나리오를 가정하였고, 각 시나리오는 다음과 같이 이벤트를 생성합니다.

이벤트는 3가지의 시나리오가 확률에 따라 300번 발생하고, 평균 700개정도의 이벤트를 생성합니다.

#### 강의 구매
>강의 조회 → 강의 구매

#### 강의 구매 실패
>강의 조회 → 강의 구매 → 에러

#### 강의 검색
>강의 검색 → 강의 조회

#### 이벤트 스키마

| 컬럼명 (Column) | 데이터 타입 (Type) | 제약 조건 (Constraint) | 설명 (Description) |
| :--- | :--- | :--- | :--- |
| **log_id** | UUID | **PK**, NOT NULL | 로그 고유 식별자 (기본키) |
| **event_time** | TIMESTAMPTZ | NOT NULL | 이벤트 발생 시각 (Timezone 포함) |
| **user_id** | UUID | NOT NULL | 이벤트를 생성한 사용자 고유 ID |
| **session_id** | VARCHAR(64) | NOT NULL | 사용자 세션 식별자 (최대 64자) |
| **event_type** | VARCHAR(30) | NOT NULL | 이벤트 종류 (`EventType` Enum 매핑) |
| **ip_address** | VARCHAR(45) | NOT NULL | 클라이언트 IP 주소 (IPv4/IPv6 호환) |
| **device_type** | VARCHAR(20) | NULLABLE | 사용 기기 유형 (`DeviceType` Enum 매핑) |

- UUID를 기반으로 분산 환경을 대비한 이벤트 로그 ID의 충돌을 방지했습니다.
- `ZonedDateTime`을 활용하여 사용자의 위치에 의존되지 않도록 하였습니다.
- 이벤트 로그는 다른 데이터의 삭제와 관계없이 저장되어야 하므로, 관계를 참조하지 않습니다.
- 최근 IPv6도 사용되기 때문에 IP 변경에도 데이터가 잘리지 않도록 설정하였습니다.
- enum 값을 String으로 저장하여 손쉬운 쿼리 및 모니터링이 가능하도록 하였습니다.
- 실제 비즈니스 로직 처리를 위한 데이터는 포함하지 않고, 단순히 '어떤 이벤트가 언제, 어디서, 누구에 의해 발생했는가'라는 확인 목적에 집중해 설계했습니다.

### 2-2. 이벤트 저장소 선택 이유
이벤트 저장소는 RDB, PostgreSQL에 저장됩니다.

이벤트 데이터는 사용자 ID, 이벤트 타입, 발생 시간 등 정형 데이터로 구성되어 있으며,
이벤트 타입별 집계, 사용자별 집계와 같은 SQL 기반 분석이 필요하다고 판단했습니다.

최근 프로젝트에서 개인 활동 기반 도서 추천 시스템을 구현하며 PostgreSQL의 확장 기능인 pgvector를 활용했습니다. 이처럼 PostgreSQL은 단순한 관계형 데이터베이스를 넘어 다양한 확장 기능을 제공한다는 장점이 있습니다. 이벤트 데이터는 시간에 따라 지속적으로 증가하는 특성이 있기 때문에, 향후 데이터 규모가 커질 경우 TimescaleDB를 활용한 시계열 데이터 최적화나 pg_partman을 활용한 파티셔닝 적용이 가능하다고 생각했습니다.

따라서 다양한 확장성을 제공하는 PostgreSQL을 선택하였습니다.


## 3. 구현하면서 고민한 점
- 이벤트 설계 시, 실제 비즈니스 로직 처리를 위한 데이터 포함 여부에 대해 고민하였지만, 발생 여부 확인이라는 목적 생성 후 제외하였습니다.
- 이벤트마다 필요한 데이터 형식이 달라질 것을 대비하여 이벤트 테이블 분리 및 Json을 저장하는 Map 형식의 payload 필드를 고민하였지만, 범용적으로 사용될 수 있는 필드로만 구성하였습니다.

# 선택 과제 항목
## 1. kubernetes manifest 파일 작성하기
데이터베이스 PostgreSQL을 Kubernetes로 실행하기 위한 파일들을 작성했습니다.

### 리소스 역할 및 선택 이유
#### Deployment
PostgreSQL 컨테이너 실행 및 유지하기 위해서 사용했습니다. 여기선 실행 시 유지할 pod 개수, db 설정 정보 등을 담고 있습니다.

#### service
쿠버네티스의 기본이 되는 pod는 재시작 시 ip가 바뀌기 때문에, 다른 컨테이너의 안정적인 접근을 위해 service 리소스를 활용하여 고정된 이름을 부여했습니다.

#### pvc
PostgreSQL Pod가 재시작 될 때, 이전에 가지고 있던 데이터들이 사라지지 않도록 설정합니다.

#### secret
민감한 정보를 담기 위해서 secret 리소스를 사용했습니다. 여기선 DB datasource의 비밀번호를 저장합니다.

## 2. AWS 아키텍처 설계

<img width="758" height="527" alt="Image" src="https://github.com/user-attachments/assets/593c3424-7675-4fd1-a0c3-98c41ba088e9" />

### 2-1. 사용 AWS 서비스 및 선택 이유
- AWS ECR : AWS의 도커 이미지 저장소로 사용됩니다.
- AWS CloudWatch : AWS 모니터링 도구입니다. 현재 서비스에선 AWS 인프라 전체를 감시하는 도구로 활용됩니다.
- AWS IAM : AWS 서비스 간 권한 관리 도구입니다. ECR Image pull, Cloudwatch endpoint 로그 전송할 때 권한 검증에 활용됩니다.
- AWS RDS : AWS 관리형 관계형 데이터베이스 서비스 입니다. 이벤트 저장소로 활용되는 PostgreSQL을 위해 사용됩니다.
- AWS EC2 : AWS가 제공하는 컴퓨팅 인스턴스입니다. Docker 기반으로 Spring Boot 이벤트 서버가 실행됩니다.
- Internet Gateway : 외부 사용자는 인터넷 게이트웨이를 통해서 저희 서비스에 접근합니다.
- Public & Private Subnet : '외부의 트래픽을 받기 위해 노출되어도 보안 상 괜찮은가?'를 기준으로 서비스의 위치를 격리했습니다.
- ALB (Application Load Balancer) : 외부 요청을 내부로 전달해줄 때 사용합니다.
- NAT Gateway : 내부 private subnet에서 외부로 요청을 전달해야 할 때 사용됩니다. 현재 서비스에선 ECR Imaga pull, CloudWatch 메트릭 전송할 떄 활용됩니다.

### 2-2. 아키텍처 설계 시 고민한 부분
#### 아키텍쳐 표현 방식
- 아키텍처를 설계하면서 서비스 간 연관성을 기준으로 배치할지, 실제 요청 흐름을 기준으로 배치할지 고민했습니다.
- 최종적으로는 시스템 동작을 이해하기 쉽도록 요청 흐름을 우선으로 구성했으며, ECR과 CloudWatch는 AWS 공용 서비스로 분리하여 표현했습니다.

#### Public Subnet과 Private Subnet의 경계
- Event Server와 PostgreSQL은 외부에 노출될 필요가 없다고 판단하여 Private Subnet에 배치하였고, 외부 요청은 ALB를 통해서만 접근하도록 구성했습니다.
- ECR Image Pull과 CloudWatch 메트릭 전송을 위해 NAT Gateway를 추가하여 운영 편의성과 보안을 모두 확보하고자 했습니다.

#### Cloudwatch VS Grafana
- 두 스택 모두 모니터링 도구로서 활용되지만, SPring Boot 이벤트 서버 모니터링 도구로 Grafana를, AWS 인프라 전반에 대한 모니터링 도구로 AWS CloudWatch를 사용하였습니다.
