## How do deploy and run application

This document will help you to run application manually or via Docker Compose.  
First, go to project's root and make the `do.sh` utility script executable: `chmod +x do.sh`.

### Manually

* Install **JDK8** and **MongoDB 3.4**+. MongoDB should listen on port 27017, accept username `root` and password `woot` on the `manondev` database (authentication and data). See `src/main/resources/application-dev.yml` for details.  
* Package and run application via `do rd`. Application will start on port 8080 with `dev` Spring profile.
  * To run with another Spring profile (e.g. `prod`), package application via `do p`, go to `target/` directory and run `java -jar -Xms128m -Xmx512m -Dspring.profiles.active=prod,metrics manon.jar`.

### Docker Compose

* Install **Docker**:
  ```bash
  # install Docker Community Edition, tested on Lubuntu 18.04 LTS
  sudo apt-get remove docker docker-engine docker.io
  sudo apt-get install apt-transport-https ca-certificates curl software-properties-common
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
  sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
  sudo apt-get update
  sudo apt-get install docker-ce
  sudo groupadd docker 
  sudo usermod -aG docker $(whoami)
  sudo systemctl enable docker
  ```
* Install **Docker Compose**:
  ```bash
  sudo curl -L "https://github.com/docker/compose/releases/download/1.22.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
  sudo chmod +x /usr/local/bin/docker-compose
  ```
* Build and install application image:
  ```
  do jib
  ```  
* Edit `docker-compose.yml` if needed (e.g. to customize ports).
* Then run application image and dependencies via Docker Compose: 
  ```bash
  docker-compose up -d
  ```
* MongoDB data is persisted in `~/manon-db/`. Application listen on port 8080 and its logs are stored in `~/manon-logs/`.
* Optional: install MongoDB command-line client and check database connectivity:
  ```bash
  sudo apt-get install mongodb-clients
  mongo localhost/manon
  ```
* Check application connectivity by visiting `http://localhost:8080/actuator/health` (default login/password is `ROOT/woot`).