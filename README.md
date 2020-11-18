# SIRS
Segurança Informática em Redes e Sistemas - IST - 2020/2021

## Server
To run the server:
 ```sh
 $ cd server
 $ mvn clean install
 $ mvn exec:java -Dexec.args="31337"
 ```

## Client
To run the client:
 ```sh
 $ cd client
 $ mvn clean install
 $ mvn exec:java -Dexec.args="localhost 31337"
 ```
