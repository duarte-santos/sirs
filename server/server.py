import tornado.ioloop
import tornado.web
import json

# ------------------------------ Class NumberKey ----------------------------- #

class NumberKey:
    def __init__(self, number, key):
        self.number = number
        self.key = key

    def dump(self):
        return {'Number': self.number, 'Key': self.key}

testpair = NumberKey(12345, "imakeyyaaaaay12345")
testpair2 = NumberKey(66666, "whf83hgf837bf38fb3fo3f4")
infected = [testpair, testpair2]

# -------------------------------- Hello World ------------------------------- #

class getToken(tornado.web.RequestHandler):
    def get(self):
        self.write("Hello, world")

# ------------------------------- Save Infected ------------------------------ #

class saveInfected(tornado.web.RequestHandler):
    global infected
    def post(self):
        received = self.request.body.decode()

        jsonobj = json.loads(received)
        data = jsonobj["nameValuePairs"]["data"]

        for nk in data:
            numberkey = NumberKey(nk["number"], nk["key"])
            infected.append(numberkey)

        for i in infected:
            print(i.number, i.key)

        self.write("nothing")

# ------------------------------- Get Infected ------------------------------- #

class getInfected(tornado.web.RequestHandler):
    global infected
    def get(self):
        global infected
        
        response = {'data':[]}
        for pair in infected:
            response['data'].append(pair.dump())

        self.write(response)



# ----------------------------------- Stuff ---------------------------------- #

application = tornado.web.Application([
    (r'/', getToken),
    (r'/sendinfected', saveInfected),
    (r'/getinfected', getInfected),
])

if __name__ == "__main__":
    http_server = tornado.httpserver.HTTPServer(application, ssl_options={
        "certfile": "health.crt",
        "keyfile": "health.key"
    })
    http_server.listen(8888)
    tornado.ioloop.IOLoop.instance().start()