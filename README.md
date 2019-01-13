<p align="center">
    <a href="https://ci.appveyor.com/project/jonathanlermitage/manon"><img src="https://ci.appveyor.com/api/projects/status/3tfcq04yte3ff1iq?svg=true"/></a>
    <a href="https://travis-ci.org/jonathanlermitage/manon"><img src="https://travis-ci.org/jonathanlermitage/manon.svg?branch=spring5-light"/></a>
    <a href="http://cirrus-ci.com/github/jonathanlermitage/manon/spring5-light"><img src="https://api.cirrus-ci.com/github/jonathanlermitage/manon.svg?branch=spring5-light"/></a>
    <a href="https://github.com/jonathanlermitage/manon/blob/master/LICENSE.txt"><img src="https://img.shields.io/github/license/jonathanlermitage/manon.svg"/></a>
    <br/>
    <a href="https://sonarcloud.io/dashboard?id=nanon%3Amanon-light"><img src="https://sonarcloud.io/api/project_badges/measure?project=nanon%3Amanon-light&metric=alert_status"/></a>
    <a href="https://codecov.io/gh/jonathanlermitage/manon/branch/spring5-light"><img src="https://codecov.io/gh/jonathanlermitage/manon/branch/spring5-light/graph/badge.svg"/></a>
    <a href="https://lgtm.com/projects/g/jonathanlermitage/manon/alerts/"><img src="https://img.shields.io/lgtm/alerts/g/jonathanlermitage/manon.svg?logo=lgtm&logoWidth=18"/></a>
    <a href="https://lgtm.com/projects/g/jonathanlermitage/manon/context:java"><img src="https://img.shields.io/lgtm/grade/java/g/jonathanlermitage/manon.svg?logo=lgtm&logoWidth=18"/></a>
</p>

1. [Project](https://github.com/jonathanlermitage/manon#project)  
2. [Author](https://github.com/jonathanlermitage/manon#author)
3. [Compilation and test](https://github.com/jonathanlermitage/manon#compilation-and-test)
4. [License](https://github.com/jonathanlermitage/manon#license)

## Project

Some experimentation with **Spring Boot 2**, JDK8+, JUnit5, TestNG, NoSQL, Docker, ELK stack, etc. It demonstrates usage of:

* **Maven** (latest version) and **Gradle** build tools
  * migration from **Maven to Gradle** (but I'll stick to Maven). Stable and functional, but still needs some improvements. See commit [cf79b9c](https://github.com/jonathanlermitage/manon/commit/cf79b9c1f0a7eee7ffcd8a1fd0b1e05e11f1de75) ([spring5-mvn-to-gradle](https://github.com/jonathanlermitage/manon/tree/spring5-mvn-to-gradle) branch)
* **Spring Boot 2** (Spring Framework 5)
  * you can compare [spring4](https://github.com/jonathanlermitage/manon/tree/spring4) and [spring5](https://github.com/jonathanlermitage/manon/tree/spring5) branches to study migration from Spring Boot 1 (Spring Framework 4) to Spring Boot 2
* built with JDK11 on Travis CI, targets JRE8 bytecode
  * see commit [f778bf0](https://github.com/jonathanlermitage/manon/commit/f778bf072ebcd951082be934ef4b4af763beb103) ([spring5-light-jre8-with-jdk11](https://github.com/jonathanlermitage/manon/tree/spring5-light-jre8-with-jdk11) branch)
* **Spring Web** with a **REST API**
* **Spring Security**, to authenticate users via auth_basic, and fetch authentication data from MongoDB instead of default SQL database 
* **Spring Data** to serve data from a **MongoDB** database
* **Spring Batch** to schedule and manage some tasks
  * see commit [c0e3422](https://github.com/jonathanlermitage/manon/commit/c0e3422fcce5522c3320dd1a2eed65950e321621) ([spring5-light-batch](https://github.com/jonathanlermitage/manon/tree/spring5-light-batch) branch)
* **Spring Cache** via Redis for application, and prefer an embedded cache provider during tests
  * see commits [a911f6a](https://github.com/jonathanlermitage/manon/commit/a911f6a08ce67b3b302f4ea3d17a73e8a0dcd6e6), [7e26822](https://github.com/jonathanlermitage/manon/commit/7e268222a745e5bbb88129d99b91379bafac7f58) and [ae6e0e6](https://github.com/jonathanlermitage/manon/commit/ae6e0e69ac37dbe44b51f449600943e09b9b149b) ([spring5-redis](https://github.com/jonathanlermitage/manon/tree/spring5-redis) branch)
* **integration tests** and (some) **unit-test** via **[TestNG](https://testng.org)** and **[REST Assured](http://rest-assured.io)**, because I like TestNG keywords, dataproviders and maturity. REST Assured helped me to test API without Spring's magic
  * migration from **TestNG** to **[JUnit5](https://junit.org/junit5/)** (with Spring Boot, dataproviders, expected exceptions, beforeClass and afterClass annotations). See last commit of [spring5-light-testng-to-junit5](https://github.com/jonathanlermitage/manon/tree/spring5-light-testng-to-junit5) branch
  * Java **architecture tests** via [**ArchUnit**](https://github.com/TNG/ArchUnit). See last commit of [spring5-light-archunit](https://github.com/jonathanlermitage/manon/tree/spring5-light-archunit) branch
* tests work with an **embedded MongoDB** instance (for data) and HSQLDB (for Spring Batch internals only), that means you don't have to install any database to test project
  * use **embedded MongoDB** during tests. See commits [37e1be5](https://github.com/jonathanlermitage/manon/commit/37e1be5f01c3ffa6ecf4d9c3e558b4ffb297227f) and [161d321](https://github.com/jonathanlermitage/manon/commit/161d3214ab72e76a2f041bbe8914077137513fb7) ([spring5-embedmongo](https://github.com/jonathanlermitage/manon/tree/spring5-embedmongo) branch)
  * make embedded MongoDB work with version from 3.6 to 4.0: see commit [a75a917](https://github.com/jonathanlermitage/manon/commit/a75a9178211233c24a6ac7001559fdfdf3413cd2) ([spring5-light-mongo4.0.x](https://github.com/jonathanlermitage/manon/tree/spring5-light-mongo4.0.x) branch)
  * replace **HSQLDB** by **H2**. See commit [ae4701e](https://github.com/jonathanlermitage/manon/commit/ae4701e6b0ed490aed32c5b07c84c5b52711188b) ([spring5-light-hsqldb-to-h2](https://github.com/jonathanlermitage/manon/tree/spring5-light-hsqldb-to-h2) branch)
* integration with some free (for open-source) services like **[AppVeyor](https://ci.appveyor.com/project/jonathanlermitage/manon)** (Windows CI), **[Travis](https://travis-ci.org/jonathanlermitage/manon)** (Linux and MacOS CI), **[Cirrus](https://cirrus-ci.com)** (CI), **[CodeCov](https://codecov.io/gh/jonathanlermitage/manon)** (code coverage), **[SonarCloud](https://sonarcloud.io/dashboard?id=nanon:manon)** (code quality), **[LGTM](https://lgtm.com/)** (code quality) 
* [Maven](https://github.com/takari/maven-wrapper) and Gradle wrappers, and a `do` Bash script that helps you to launch some usefull commands
* **code coverage** thanks to **JaCoCo** Maven and Gradle plugin
* some **AOP** to capture performance of API endpoints
* **Spring Actuator** web endpoints configured
* **Swagger UI** to provide documentation about REST API endpoints
  * run application and check `http://localhost:8080/swagger-ui.html`, authenticate with `ROOT` / `woot`. See commit [834852c](https://github.com/jonathanlermitage/manon/commit/834852cd5ce8bbb869a189aecdd90097c9168152) ([spring5-swagger](https://github.com/jonathanlermitage/manon/tree/spring5-swagger) branch)
* **Docker** builds without Docker daemon thanks to **[Jib](https://github.com/GoogleContainerTools/jib)**. **Docker Compose** support. Linux base image is [Distroless](https://github.com/GoogleContainerTools/distroless)
  * See [DEPLOY.md](DEPLOY.md) to package and run application. See commit [de7335b](https://github.com/jonathanlermitage/manon/commit/de7335b2be850ca6a7b683bdbe2b86adc990b594) ([spring5-light-docker-jib](https://github.com/jonathanlermitage/manon/tree/spring5-light-docker-jib) branch)
* **ELK** stack (**ElasticSearch Logstash Kibana**) via Docker plus Docker Compose to parse application and Nginx logs, and Cerebro to monitor ElasticSearch node
  * see [DEPLOY.md](DEPLOY.md) to package and run application. Also, see commits [6b76e37](https://github.com/jonathanlermitage/manon/commit/6b76e376566fd34b4b3521dc6c60eaf7c30c1c22) and [966969f](https://github.com/jonathanlermitage/manon/commit/966969fc16277be3ec8605592f5ed7ae90ba7024) ([spring5-light-elk](https://github.com/jonathanlermitage/manon/tree/spring5-light-elk) branch)
* integration with **[SpotBugs](https://github.com/find-sec-bugs/find-sec-bugs/wiki/Maven-configuration)** ([FindBugs](http://findbugs.sourceforge.net))

For fun and to show some skills :cat:

## Author

Jonathan Lermitage (<jonathan.lermitage@gmail.com>)  
Linkedin profile: [jonathan-lermitage-092711142](https://www.linkedin.com/in/jonathan-lermitage-092711142/)

## Compilation and test

First, install JDK11 and Maven3+.
  
You can now use the `./do` Linux Bash script:  
```
do help         show this help message
do t            test without code coverage (with embedded MongoDB)
do tc           test with code coverage (with embedded MongoDB)
do sc           compute and upload Sonar analysis to SonarCloud
do tsc          similar to "do tc" then "do sc"
do sb           scan with SpotBugs then show GUI
do b            build without testing
do c            clean
do p            package application to manon.jar
do rd           package and run application with dev profile 
do w 3.5.2      set or upgrade Maven wrapper to 3.5.2
do cv           check plugins and dependencies versions
do uv           update plugins and dependencies versions
do dt           show dependencies tree
do rmi          stop Docker application, then remove its containers and images
do cdi          clean up dangling Docker images
do docker       build Docker image with Dockerfile to a Docker daemon
do jib          build Docker image with Jib to a Docker daemon
do jibtar       build and save Docker image with Jib to a tarball
do up           create and start containers via docker-compose
do stop         stop containers via docker-compose
do upelk        create and start ELK containers via docker-compose
do stopelk      stop ELK containers via docker-compose
do upcerebro    create and start Cerebro container via docker-compose
do stopcerebro  stop Cerebro container via docker-compose
```

Nota: the Linux Bash script can chain parameters, e.g.: `./do cdi rmi w 3.6.0 c tc docker up`.  
Nota: a Windows `do.cmd` script exists, but it's limited to some basic features (`do t` to `do dt`, no Docker support).

## License

MIT License. In other words, you can do what you want: this project is entirely OpenSource, Free and Gratis.
