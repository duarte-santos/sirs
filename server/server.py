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

# -------------------------- Get current time -------------------------- #
def get_current_time():
    # ts stores the time in seconds 
    now = time.time()
    seconds = int(now)
    nanos = int( (now - seconds) * 1000000000 )
    return (seconds, nanos)


# --------------------------- Check signature -------------------------- #

def check_signature(data):
    # ------------ Verify signatures ---------------

    # We know the health authority signed a SHA256 hash of the sum of the numbers
    # So, first we need to recriate the exact format of the data that the health authority signed

    numbers = []
    for n in data['nk_array']:
        numbers.append(n['number'])

    checkData = 0
    for n in numbers:
        checkData += n % 1000000

    checkData = str(checkData).encode("UTF-8")
    h = SHA256.new()
    h.update(checkData)
    checkData = base64.b64encode(h.digest())
    checkData = checkData.decode()
    checkData += '"'
    checkData = '"' + checkData
    checkData = checkData.encode()

    # Hash the data with SHA256
    h = SHA256.new()
    h.update(checkData)

    # Return the signature to its original format
    received_signature = data['signature']
    received_signature = received_signature.replace("-", "/")
    received_signature += "==" # add padding
    signature = base64.b64decode(received_signature)

    # Verify the signature
    with open('health.pub', 'rb') as f:
        pubkey = RSA.importKey(f.read())
    verifier = PKCS1_v1_5.new(pubkey)
    v = verifier.verify(h, signature)

    return v


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

    print("\nStoring ", number, key, seconds, nanos)
    sql = "INSERT INTO " + tableName + " (number, pkey, seconds, nanos) VALUES (%s, %s, %s, %s) ON DUPLICATE KEY UPDATE number=number;"
    val = (number, key, seconds, nanos)

    mycursor.execute(sql, val)
    mydb.commit()

    mycursor.execute("SELECT * FROM " + tableName + ";")
    myresult = mycursor.fetchall()


# -------------------------- Get from Database ------------------------- #

def get_all_from_database(lastUpdateSeconds, lastUpdateNanos):
    global tableName

    data = []

    mydb, mycursor = connect_to_database()
    mycursor.execute("SELECT * FROM " + tableName + ";")
    myresult = mycursor.fetchall()

    now = time.time()
    for row in myresult:
        # row: (number, key, seconds, nanos)
        number_ts = int(row[2]) + ( int(row[3]) / 1000000000 )
        
        if number_ts > lastUpdateSeconds: # number was added after last update
            data.append({"Number": row[0], "Key": row[1]})
            print("\nNumber: ", row[0], " Key: ", row[1])
        
        if (now - number_ts > 14*24*60*60): # 2 weeks have gone by, irrelevant number
            delete_from_database(number)

    return data

# ------------------------- Delete from Database ----------------------- #
def delete_from_database(number):
    global tableName
    mydb, mycursor = connect_to_database()

    mycursor.execute("DELETE FROM " + tableName + " WHERE number = '" + str(number) + "';")
    mydb.commit()

    print("deleted record: ", number)


# ====================================================================== #
# ====[                           MAIN                             ]==== #
# ====================================================================== #

# ---------------------------- Save Infected --------------------------- #

class saveInfected(tornado.web.RequestHandler):
    def post(self):
        seconds, nanos = get_current_time()

        received = self.request.body.decode()
        jsonobj = json.loads(received)
        data = jsonobj["nameValuePairs"]["data"]

        if not check_signature(data):
            print("\nWrong signature! ")
            self.write("Wrong signature!")
            return
            
        for nk in data['nk_array']:
            dummy = "a"
        store_in_database(nk['number'], nk['key'], seconds, nanos)
        
        print("---------------------------------\n")
        
        self.write("Success")


# ---------------------------- Get Infected ---------------------------- #

class getInfected(tornado.web.RequestHandler):
    def post(self):
        received = self.request.body.decode()

        jsonobj = json.loads(received)
        seconds = jsonobj["nameValuePairs"]["lastUpdateSeconds"]
        nanos = jsonobj["nameValuePairs"]["lastUpdateNanos"]

        #test_infected = {"Number": 12345, "Key": "key"}

        data = get_all_from_database(seconds, nanos)
        data = []
       
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