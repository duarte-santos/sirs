import tornado.ioloop
import tornado.web
import json
import MySQLdb as db


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

def store_in_database(number, key):
    global tableName

    mydb, mycursor = connect_to_database()

    sql = "INSERT INTO " + tableName + " (number, pkey) VALUES (%s, %s) ON DUPLICATE KEY UPDATE number=number;"
    val = (number, key)

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
        received = self.request.body.decode()

        jsonobj = json.loads(received)
        data = jsonobj["nameValuePairs"]["data"]

        print(data)

        for nk in data:
            store_in_database(nk['number'], nk['key'])         

        self.write("nothing")


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