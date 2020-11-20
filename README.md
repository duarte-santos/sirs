# SIRS
Segurança Informática em Redes e Sistemas - IST - 2020/2021

## Before running
Install the app (in base directory):
 ```sh
 $ mvn clean install
 ```

## Server

Firstly, we need to setup the MySql database

Run:
```sh
$ sudo mysql
```

Inside the mysql prompt, run:
```sql
CREATE DATABASE contact;
CREATE USER 'server'@'localhost' IDENTIFIED BY 'server';
GRANT ALL PRIVILEGES ON contact TO 'server'@'localhost';
```

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
