# gretlx-frontend

## Beschreibung

Lorem Ipsum ...

## Komponenten

Lorem Ipsum ...

## Konfigurieren und Starten

Die Anwendung kann am einfachsten mittels Env-Variablen gesteuert werden. Es stehen aber auch die normalen Spring Boot Konfigurationsmöglichkeiten zur Verfügung (siehe "Externalized Configuration").

| Name | Beschreibung | Standard |
|-----|-----|-----|
| `PLATFORM_OWNER` | Github-Repo-Owner oder -Organisation. | `edigonzales` |
| `PLATFORM_TOKEN` | Token, um Github Action zu starten. | |
| `PLATFORM_BASE_URL` | Base-URL der Platform. Aus dieser Base-URL wird der REST-Api-Call für das Starten der Action eruiert und die URL für die Weiterleitung. | `github.com` |
| `WORK_DIRECTORY` | S3-Bucket zum Speichern der Dateien, damit sie der GRETL-Job herunterladen kann. | `ch.so.agi.gretl.naturgefahren-dev` |
| `AWS_ACCESS_KEY_ID` | AWS Access Key. |  
| `AWS_SECRET_ACCESS_KEY` | AWS Secret Key. |  
| `LOG_LEVEL_SPRING` | Loglevel für Spring-Framework. | `INFO` |
| `LOG_LEVEL_APP` | Loglevel eigene Businesslogik. | `DEBUG` |
| `TOMCAT_THREADS_MAX` |  | `50` |
| `TOMCAT_ACCEPT_COUNT` |  | `100` |
| `TOMCAT_MAX_CONNECTIONS` |  | `2000` |

### Java

```
java -jar target/gretlx-frontend-0.0.1-SNAPSHOT.jar
```

AWS-Keys müssen der Anwendung bekannt sein, sonst startet die Anwendung nicht.

### Native Image

```
./target/gretlx-frontend
```

### Docker

```
docker run -p8080:8080 -e AWS_ACCESS_KEY_ID=XXXXXX -e AWS_SECRET_ACCESS_KEY=YYYYYY sogis/gretlx-frontend-jvm:latest
```

Oder ohne `-jvm`, falls das Native Image verwendet werden soll.

## Externe Abhängigkeiten

Lorem Ipsum... Github Action...

## Konfiguration und Betrieb in der GDI

Siehe openshift-templates...

## Interne Struktur

Lorem Ipsum...

## Entwicklung

### Run 
```
./mvnw 
```

### Build

#### JVM
```
./mvnw clean package -Pproduction
```

```
docker build -t sogis/gretlx-frontend-jvm:latest -f Dockerfile.jvm .
```


#### Native

```
./mvnw -Pproduction -Pnative native:compile
```

```
docker build -t sogis/gretlx-frontend:latest -f Dockerfile.native .
```

In diesem Fall muss das Native Image auf Linux gebuildet werden.

