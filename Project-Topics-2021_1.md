Instituto Superior Técnico, Universidade de Lisboa

**Network and Computer Security**



# Project Topics

_Important note: the security aspects, both functional and assurance ones, are the essential points where you should focus your work and will be the main metrics to be considered in the grading. In the designed and proposed solutions, user interface concerns are secondary._

----


## 1 Smartphone as a security token

The smartphone is an indispensable digital companion for many people. 
This work should leverage its _proximity_ to the user as part of an increased security solution.

The smartphone can be used for:
- Two-factor _authentication_ - for example, the user should have the phone with himself to answer a security challenge posed by a web application;
- _Authorization_ - for example, the user should confirm on the phone an access to an important resource in a web application;
- _Verification of presence at a specific location_ - by interacting periodically with other devices, through Wi-Fi, Bluetooth, or NFC; when the phone is present, some important resources can be made available to the user, such as a _password vault_.

In all of the examples above, the solution should be designed with a good balance between security and usability, with special consideration for the _pairing_ of devices and for the regular interaction.
The security solution to propose will have to be implemented in a working prototype, running in a mobile operating system, suitable for demonstration of the capabilities.

**Suggested references:**

- [Duo SDK](https://duo.com/product/every-application/supported-applications/apis) -- two-factor authentication API _(there are others)_
- [KeePass Plugins](https://keepass.info/help/v2/plugins.html) -- open-source password vault
- [LastPass: How it works](https://www.lastpass.com/how-lastpass-works) -- commercial password manager

----


## 2 Ransomware-resistant remote documents

Collaborative applications allow groups of users to create and edit documents remotely. 
These documents are regularly synchronized between personal devices and the remote servers.
In these applications, a user, owner of the document, can select contributors which have access to the document. 
Documents should not be accessed by unauthorized parties. 
If an attacker accesses the servers storing the documents he must not be able to view the documents and if he tries to edit any document, there must be a way to detect the illegal modifications.

A _ransomware attack_ happens when a system is breached and the attacker encrypts the user files and asks for a ransom to provide the decryption key.
If properly implemented, recovering the files without the decryption key is an intractable problem.
Also, the identification of the attackers can be difficult with the use of cryptocurrencies like BitCoin.  
Organizations must have backups, with version history, to allow recovery in the case of ransomware attacks.

In this topic, the solution should allow documents to be shared over a public network in a secure fashion. 
It should allow authenticated users to access local and remote files in a transparent way. 
Data confidentiality must be assured even in the case where an attacker gains physical access to the data storage devices. 
Illegal modification of the documents by unauthorized users must be detected. 
The document system should resist ransomware attacks.

**Suggested references:**

- [Nextcloud](https://github.com/nextcloud) -- client-server software for creating and using file hosting services
- [Syncthing](https://docs.syncthing.net/) -- file synchronization tool

----


## 3 Medical test records

Health care institutions gather and store sensitive information from patients.
The information systems should allow fine-grained and contextualized access to the records to the relevant staff, like Doctors and Nurses.
Other staff with different data access needs include: clinical assistants (take care of ward housekeeping), patient services assistants (bring meals and drinks), porters (take care of patient lifting and transport), volunteers (help with fundraising and ward visits), and ward clerks (staff the ward reception desks).

One of the relevant types of data stored are _test results_.
Some of the medical tests can be performed inside a _hospital lab_, but in many cases, tests are done in _partner labs_, that have a distinct infrastructure, remote from the hospital's infrastructure, that need to be interconnected.
Also, the medical test data has to be archived in a way such that its authenticity can be verified later, if necessary.

It is mandatory that the solution externalizes the security policy with a policy language, like XACML, with separate policy authoring and enforcement points, and different parts of the patient record can be accessed by different people and in different contexts.  
To demonstrate the advantage of this separation, the solution should have a _Normal_ mode of operation and a _Pandemic_ mode, where the rules for data access are significantly different.
The switch between modes should be done with changes to the policy documents but without changes to the application code.

**Suggested references:**

- [RFC2753](https://tools.ietf.org/html/rfc2753) -- Framework for Policy-based Admission Control
- [XACML](https://www.oasis-open.org/committees/tc_home.php?wg_abbrev=xacml#other) -- security policy language _(there are other languages)_
  - [XACML library](https://github.com/wso2/balana) -- example XACML implementation _(there are other libraries)_

----


## 4. Secure child locator

In this scenario, consider the problem of child localization in outdoor and indoor spaces.
Develop a solution for smartphone or smartwatch users that enables the tracking of children only by their authorized legal guardians and not by anyone else.
The solution should operate outdoors, using GPS (e.g. A-GPS), but should also operate indoors, where GPS does not work.
Indoor location can rely on Wi-Fi fingerprinting (e.g. Google Indoors Maps), Bluetooth beacons, Li-Fi, or other techniques.

The solution should consider the secure tracking of the children inside defined fences and there should be an alert (SOS) functionality. 
Both the children and the responsible adult should be regarded as users of the system, and all stored and communicated data should consider user consent and their privacy.
If a remote server is used, it should not be fully trusted. 
For example, the solution may assume the server to be _honest-but-curious_, i.e., it follows application protocols but tries to collect as much data as possible about the users.

**Suggested references:**

- [My Ki system](https://myki.watch/en/)
- EASYmaxx [Smartwatch](https://www.amazon.de/EASYmaxx-Smartwatch-Armbanduhr-Sprachnachrichten-Standortlokalisierung-blau/dp/B07BZ292D9) and [iOS app](https://apps.apple.com/us/app/easymaxx-smartwatch/id1375209119)
- [Google Maps Indoors](https://www.google.com/maps/about/partners/indoormaps/)

----


## 5. Contact tracing

In this topic, we consider device tracking using their hardware identifiers. 
Each mobile device has one or more hardware identifiers that are broadcast in local networks, namely the Wi-Fi adapter MAC addresses. 
The same happens with Bluetooth. 
A typical system architecture has a data aggregation server (on the cloud), a set of scan servers that send periodic reports of sighted devices, and a query client. 
Keeping record of these identifiers and associating them with users can help track the iteration between devices and their users, but can also become a severe privacy violation as we can know of a user's whereabouts and daily routines. 

The goal of the scenario is to develop a service for smartphones to assess the interaction between devices/people in order to track potential COVID-19 contamination, while maintaining the privacy of the users. 
The system should allow:  
i) the device owner to inform others that he is infected (using a code given by the health authority) or  
ii) to be informed that he was in contact with someone infected.  
In case of ii) the user should be able to get the information where this contact occurred and for how long.

**Suggested references:**

- [D3PT](https://github.com/DP-3T/documents) -- Decentralized Privacy-Preserving Proximity Tracing
- [Apple and Google Privacy-Preserving Contact Tracing](https://covid19.apple.com/contacttracing) -- joint documentation from the two leading smartphone OS providers

----


## 6. Smart car management system

Current automobiles comprise different types of systems, ranging from infotainment and climatization systems to the braking and throttle control systems.
Moreover, they support remote access to perform operations such as monitoring the levels of gas and tire pressure.
However, it is critical that systems such as the braking and throttle control systems cannot be accessed remotely.
For this topic, the integrity of operations must be ensured with adequate network segmentation and with the assistance of a simulated Trusted Execution Environment (TEE).

A TEE can be used to handle sensitive data and operations, isolating it from the main system.
TEEs can be enforced with hardware security extensions featured by some processors (Intel SGX, in case of Intel processors, and ARM TrustZone, in case of ARM application processors).

In this topic, the car system to develop must include, at least:
* In the car:
  * A Control Unit server that manages all the car's systems and external communications;
  * The TEE of the Control Unit that handles and protects critical operations;
  * Pedal peripherals, directly connected to the TEE by a dedicated bus, that may trigger the accelerate and brake operations;
  * Engine controller connected to the Control Unit that validates accelerate operations and regulates the throttle;
  * Brake controller connected to the Control Unit that validates brake operations and brakes the wheels.
* In the manufacturer:
  * A service that allows users to connect to their cars (with their phone or computer), to open doors, regulate the AC, and monitor tire pressure and fuel levels.

You may simulate each part of the system with a VM (including the TEE) or a client application and use sockets to simulate the connections.  
The TEE VM may have: a prestored certificate; a prestored private key; a service that receives, validates and executes programs; a direct secure bus to the pedals (may be simulated with sockets); and a memory region shared with the main system (may also be simulated with sockets).

**Suggested references:**

- [Survey of the Connected Vehicles](https://ieeexplore.ieee.org/abstract/document/8058008)
- [Open-TEE](https://ieeexplore.ieee.org/document/7345308) _(not for direct use, but for reference)_

----

[SIRS Faculty](mailto:meic-sirs@disciplinas.tecnico.ulisboa.pt)
