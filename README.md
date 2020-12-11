# SIRS
Segurança Informática em Redes e Sistemas - IST - 2020/2021


## Required platform

The project was developed and tested in Windows 10 and Kali Linux. We therefore recommend that you run the project in Windows 10, but you can also use a stable Linux 64-bit distribution, such as Ubuntu 18.04.1 LTS.

AdoptOpenJDK 8, Gradle 6.7.1, MySQL and SDK 26 (SDK 30 would be ideal) are required.

To fully run our application, two android phones are also required - minimum Android 8.0 (Oreo). These must both suport Bluetooth and Bluetooth Advertisements. Ideally they would also need to support 2M PHY and LE Extended Advertisement.


## Setting up the environment
1. Install AdoptOpenJDK 8 and add it to Path
2. Install Gradle (v6.7.1) and add it to Path
3. Install Android SDK 30 (or Android Studio) and add it to Path
4. Install MySQL and add it to Path
5. Install the required python modules: MySQL (eg.: `pip install mysqlclient`) and Crypto (eg.: `pip install pycryptodome`)


## Before running

**Step 1: Build the app (in `android` directory):**
 ```sh
 $ cd android
 $ gradle clean build
 ```

**Step 2: Install the app:**

Connect the two smartphones to the computer through USB and run the following command in the `android` directory:

```sh
$ gradle installDebug
```

## To run the program

**Step 1: To run the android client:**

Open the `Contact Tracing` app on both smartphones and accept the request to turn on Bluetooth and the location services (required for BLE to work).

**Step 2: To run the Health Authority server:**
```sh
$ cd health-authority
$ python ./health.py
```
NOTE: Alternatively use `python3`


**Step 3: To run the server:**
```sh
$ cd server
$ python ./server.py
```
NOTE: Alternatively use `python3`


**Step4: Interact with the app:**

The smartphones will automatically start generating random numbers and storing them.

To turn on the ability for the smartphones to exchange numbers between them, click the buttons `ADVERTISE` and `SCAN`.

To simulate an infected user, click the button `RECEIVE SIGNATURE` to get a certified signature from the health authority. Then, you can click `SEND INFECTED` to send the infected numbers, along with the signature, to the server. The server will store them in its database.

To get updates on the infected numbers, click the `GET INFECTED` button. You will receive any new numbers that were stored on the server since the last update.

If you choose to send numbers on a smartphone and ask for updates with the other, as they are close to each other (and, therefore, exchanging numbers through BLE), the app will warn you that you have been in contact with an infected person.

This is the recommended steps to test our application. However, feel free to explore the capabilities of it at your own will.
