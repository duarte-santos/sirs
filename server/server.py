import tornado.ioloop
import tornado.web
from Crypto.Hash import SHA256
from Crypto.Cipher import PKCS1_OAEP
from Crypto.PublicKey import RSA
from Crypto.Signature import PKCS1_v1_5
import base64
import json
import MySQLdb as db
import time

# dbHost, dbUser, dbPass, dbName, tableName
dbInfo = ("localhost", "root", "root", "contact")
tableName = "numbers"

# ====================================================================== #
# ====[                          AUXILIARY                         ]==== #
# ====================================================================== #


# ----------------------------- Hello World ---------------------------- #
class getToken(tornado.web.RequestHandler):
    def get(self):
        self.write("Hello, world")


# ====[                           Database                         ]==== #

# ------------------------- Connect to Database ------------------------ #

def connect_to_database():
    global dbInfo
    dbHost = dbInfo[0]
    dbUser = dbInfo[1]
    dbPass = dbInfo[2]
    dbName = dbInfo[3]
    
    mydb = db.connect(host = dbHost, user = dbUser, passwd = dbPass, database=dbName)
    mycursor = mydb.cursor()
    return (mydb, mycursor)


# ---------------------------- Init Database --------------------------- #

def init_database():
    global dbInfo
    global tableName
    dbHost = dbInfo[0]
    dbUser = dbInfo[1]
    dbPass = dbInfo[2]
    dbName = dbInfo[3]

    mydb = db.connect(host = dbHost, user = dbUser, passwd = dbPass)
    mycursor = mydb.cursor()

    mycursor.execute("CREATE DATABASE IF NOT EXISTS " +  dbName + ";")
    mycursor.execute("CREATE USER IF NOT EXISTS '" + dbUser + "'@" + dbHost + " IDENTIFIED BY '" + dbPass + "';")
    mycursor.execute("GRANT ALL PRIVILEGES ON `" + dbName + "`.* TO '" + dbUser + "'@" + dbHost + ";")
    
    mydb, mycursor = connect_to_database()
    mycursor.execute("CREATE TABLE IF NOT EXISTS " + tableName + \
                    "  (number          INT PRIMARY KEY," \
                    "   pkey            VARCHAR(255)," \
                    "   seconds         LONG," \
                    "   nanos           LONG);")


# -------------------------- Store in Database ------------------------- #

def store_in_database(number, key, seconds, nanos):
    global tableName

    mydb, mycursor = connect_to_database()

    sql = "INSERT INTO " + tableName + " (number, pkey, seconds, nanos) VALUES (%s, %s, %s, %s) ON DUPLICATE KEY UPDATE number=number;"
    val = (number, key, seconds, nanos)

    mycursor.execute(sql, val)
    mydb.commit()


# -------------------------- Get from Database ------------------------- #

def get_all_from_database():
    data = []

    mydb, mycursor = connect_to_database()

    mycursor.execute("SELECT * FROM " + tableName + ";")
    myresult = mycursor.fetchall()

    for row in myresult:
        print(row)
        data.append({"Number": row[0], "Key": row[1]})

    return data



# ====================================================================== #
# ====[                           MAIN                             ]==== #
# ====================================================================== #

# ---------------------------- Save Infected --------------------------- #

class saveInfected(tornado.web.RequestHandler):
    def post(self):
        # ts stores the time in seconds 
        now = time.time()
        seconds = int(now)
        nanos = int( (now - seconds) * 1000000000 )

        received = self.request.body.decode()

        jsonobj = json.loads(received)
        data = jsonobj["nameValuePairs"]["data"]

        # ------------ Verify signatures ---------------

        for batch in data:

            # We know the health authority signed an encrypted set of the numbers (encrypted with the server public key)
            # So, first we need to recriate the exact format of the data that the health authority signed
            # FIXME : The health authority still receives plaintex
        
            checkData = {"values":[]}
            for n in batch['nk_array']:
                checkData["values"].append(n['number'])

            checkData = json.dumps(checkData, indent=0).encode('utf-8')
            checkData = checkData.decode().replace("\n", "")
            checkData = checkData.replace(" ", "")
            checkData = checkData.encode('utf-8')

            # Hash the data with SHA256
            h = SHA256.new()
            h.update(checkData)

            # Return the signature to its original format
            received_signature = batch['signature']
            received_signature = received_signature.replace("-", "/")
            received_signature += "==" # add padding
            signature = base64.b64decode(received_signature)

            # Verify the signature
            with open('health.pub', 'rb') as f:
                pubkey = RSA.importKey(f.read())
            verifier = PKCS1_v1_5.new(pubkey)
            v = verifier.verify(h, signature)

            if not v:
                print("Wrong signature!")
                print(signature)
                self.write("Wrong signature!")
                return
            
            
            for nk in data[0]['nk_array']:
                store_in_database(nk['number'], nk['key'], seconds, nanos)
                print("Added: ", nk['number'], nk['key'])

        self.write("Success")


# ---------------------------- Get Infected ---------------------------- #

class getInfected(tornado.web.RequestHandler):
    def get(self):
        data = get_all_from_database()
        response = {'data':data}

        self.write(response)


# ----------------------------- Run server ----------------------------- #

init_database()

application = tornado.web.Application([
    (r'/', getToken),
    (r'/sendinfected', saveInfected),
    (r'/getinfected', getInfected),
])

if __name__ == "__main__":

    http_server = tornado.httpserver.HTTPServer(application, ssl_options={
        "certfile": "server.crt",
        "keyfile": "server.key"
    })
    http_server.listen(8888)
    tornado.ioloop.IOLoop.instance().start()