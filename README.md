# SIRS
Segurança Informática em Redes e Sistemas - IST - 2020/2021

## Before running
Install the app (in base directory):
 ```sh
 $ mvn clean install
 ```

## Server
To run the server:
 ```sh
 $ cd server
 $ mvn exec:java -Dexec.args="31337"
 ```

## Client
To run the client:
 ```sh
 $ cd client
 $ mvn exec:java -Dexec.args="localhost 31337"
 ```
