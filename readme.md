## local run
- create from sample `gradle.properties`
- create from sample `app/src/main/resources/application.yml`
- create from sample `mcp/sql/src/main/resources/application.yaml`
- start compose `./gradlew compose:composeUp`
- start application `./gradlew app:bootRun`
- start application `./gradlew mcp:sql:bootRun`
- use swagger http://localhost:8081/swagger-ui/index.html