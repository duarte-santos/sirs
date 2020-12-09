# SIRS
Segurança Informática em Redes e Sistemas - IST - 2020/2021

## Setting up the environment
1. Install AdoptOpenJDK 8 and add it to Path
2. Install Gradle (v6.7.1) and add it to Path
3. Install MySQL and add it to Path
4. Install MySQL python module (`pip install mysqlclient`)
5. Install Crypto python module (`pip install pycryptodome`)

## Before running
**Step 0: Build the app (in android directory):**
 ```sh
 $ cd android
 $ gradle clean build
 ```

## Server

**Step 1: To run the server:**
 ```sh
 $ cd server
 $ python ./server.py
 ```
NOTE: Alternatively use `python3`


 ## Health Authority
 **Step 2: To run the Health Authority server:**
  ```sh
 $ cd health-authority
 $ python ./health.py
 ```


## Client (on Android)
**Step 3: To run the client:**

 - Open the app (If you're on a PC, open the emulator through Android Studio - directory `android`)
 - Fill ip = `10.0.2.2` and port = `50051` and press `Start Contact Tracing`

 NOTE: The run commands were only tested on windows