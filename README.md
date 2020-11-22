# SIRS
Segurança Informática em Redes e Sistemas - IST - 2020/2021

## Before running
**Step 0: Install the app (in base directory):**
 ```sh
 $ gradle clean install
 ```

## Server

**Step 1: Firstly, we need to setup the MySql database**

**A: Linux**

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

**B: Windows**

Open `MySQL command line client`

Run:
```sql
CREATE DATABASE contact;
CREATE USER 'server'@'localhost' IDENTIFIED BY 'server';
GRANT ALL PRIVILEGES ON `contact`.* TO 'server'@'localhost';
```

**Step 2: To run the server:**
 ```sh
 $ cd build/install/sirs/bin/
 $ ./contact-server.bat 31337
 ```
NOTE: The run commands were only tested on windows

## Client
**Step 3: To run the client:**
 ```sh
 $ cd build/install/sirs/bin/ (unnecessary)
 $ ./contact-client.bat localhost 31337
 ```

 NOTE: The run commands were only tested on windows
