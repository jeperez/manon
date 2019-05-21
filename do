#!/bin/bash

let "nextParam = 1"
for ((cmd = 1; cmd <= $#; cmd++)) do

    (( nextParam++ ))

    case "${!cmd}" in

    "help")
      echo ""
      echo  "Helper: (tip: you can chain parameters, e.g.: \"./do cdi rmi docker\" or \"./do w 3.6.0 c t\")"
      echo  ""
      echo  "t            test"
      echo  "ut           test: run unit tests only, no integration tests"
      echo  "tc           test and generate coverage data"
      echo  "sc           compute and upload Sonar analysis to SonarCloud (set TK1_MANON_SONAR_ORGA and TK1_MANON_SONAR_LOGIN environment variables first)"
      echo  "tsc          similar to \"do tc\" then \"do sc\""
      echo  "sb           scan with SpotBugs then show GUI"
      echo  "b            compile"
      echo  "c            clean"
      echo  "p            package"
      echo  "rd           package and run application with dev profile"
      echo  "w \$V         set or upgrade Maven wrapper to version \$V"
      echo  "cv           check plugins and dependencies versions"
      echo  "uv           update plugins and dependencies versions"
      echo  "dt           show dependencies tree"
      echo  "rmi          stop Docker application, then remove its containers and images"
      echo  "cdi          clean up dangling Docker images"
      echo  "docker       build Docker image with Dockerfile to a Docker daemon as lermitage-manon:1.0.0-SNAPSHOT"
      echo  "jib          build Docker image with Jib to a Docker daemon as lermitage-manon:1.0.0-SNAPSHOT"
      echo  "jibtar       build and save Docker image with Jib to a tarball"
      echo  "up           create and start containers via docker-compose"
      echo  "stop         stop containers via docker-compose"
      echo  "upelk        create and start ELK containers via docker-compose"
      echo  "stopelk      stop ELK containers via docker-compose"
      echo  "upcerebro    create and start Cerebro container via docker-compose"
      echo  "stopcerebro  stop Cerebro container via docker-compose"
      echo  "maria        connect to dockerized MariaDB by calling MySQL Client provided by container"
      echo  "mariah       connect to dockerized MariaDB by calling host MySQL Client (mysql-client package must be installed)"
      ;;

    "t")
      sh ./mvnw verify
      ;;

    "ut")
      sh ./mvnw test
      ;;

    "tc")
      sh ./mvnw verify -P coverage
      ;;

    "b")
      sh ./mvnw compile -DskipTests -T1
      ;;

    "c")
      sh ./mvnw clean
      ;;

    "p")
      sh ./mvnw package -DskipTests -T1
      ;;

    "rd")
      sh ./mvnw package -DskipTests -T1
      cd target/
      java -jar -Xms128m -Xmx512m -Dspring.profiles.active=dev -Dfile.encoding=UTF-8 -Djava.awt.headless=true -XX:CompileThreshold=1500 manon.jar
      cd ..
      ;;

    "w")
      mvn -N io.takari:maven:wrapper -Dmaven=${!nextParam}
      ;;

    "cv")
      sh ./mvnw versions:display-property-updates -U -P coverage,jib,mig,spotbugs
      ;;

    "uv")
      sh ./mvnw versions:update-properties -U -P coverage,jib,mig,spotbugs
      ;;

    "dt")
      sh ./mvnw dependency:tree
      ;;

    "sc")
      sh ./mvnw sonar:sonar -Dsonar.organization=$TK1_MANON_SONAR_ORGA -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$TK1_MANON_SONAR_LOGIN
      ;;

    "tsc")
      sh ./mvnw verify sonar:sonar -P coverage -Dsonar.organization=$TK1_MANON_SONAR_ORGA -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$TK1_MANON_SONAR_LOGIN
      ;;

    "sb")
      sh ./mvnw clean compile spotbugs:spotbugs spotbugs:gui -P spotbugs
      ;;

    "rmi")
      docker-compose -f ./docker/docker-compose.yml stop
      docker rm $(docker ps -a | grep "lermitage-manon" | awk '{print $1}')
      docker rmi $(docker images | grep -E "^lermitage-manon|gcr.io/distroless/java|<none>" | awk '{print $3}')
      ;;

    "cdi")
      docker rmi $(docker images -f "dangling=true" -q)
      ;;

    "docker")
      sh ./mvnw package -DskipTests -T1
      docker build -f ./docker/Dockerfile -t lermitage-manon:1.0.0-SNAPSHOT .
      ;;

    "jib")
      sh ./mvnw compile jib:dockerBuild -DskipTests -P jib
      ;;

    "jibtar")
      sh ./mvnw compile jib:buildTar -DskipTests -P jib
      ;;

    "up")
      docker-compose -f ./docker/docker-compose.yml up -d
      ;;

    "stop")
      docker-compose -f ./docker/docker-compose.yml down
      ;;

    "upelk")
      docker-compose -f ./docker/docker-compose-elk.yml up -d
      ;;

    "stopelk")
      docker-compose -f ./docker/docker-compose-elk.yml down
      ;;

    "upcerebro")
      docker-compose -f ./docker/docker-compose-cerebro.yml up -d
      ;;

    "stopcerebro")
      docker-compose -f ./docker/docker-compose-cerebro.yml down
      ;;

    "maria")
      docker exec -it maria mysql --user=root --password=woot manon
      ;;

    "mariah")
      mysql -h $(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' maria) --port 3306 --protocol=TCP --user=root --password=woot manon
      ;;

    esac

done
