import tornado.ioloop
import tornado.web
from Crypto.Hash import SHA256
from Crypto.Cipher import PKCS1_OAEP
from Crypto.PublicKey import RSA
from Crypto.Signature import PKCS1_v1_5
import base64
import json


infected = []


# ------------------------------ Class NumberKey ----------------------------- #

class NumberKey:
    def __init__(self, number, key):
        self.number = number
        self.key = key
    
    def __eq__(self, other):
        return self.number == other.number and self.key == other.key

    def dump(self):
        return {'Number': self.number, 'Key': self.key}

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
                numberkey = NumberKey(nk["number"], nk["key"])
                if numberkey not in infected:
                    infected.append(numberkey)
                    print("Added: ", numberkey)

        self.write("Success")

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
        "certfile": "server.crt",
        "keyfile": "server.key"
    })
    http_server.listen(8888)
    tornado.ioloop.IOLoop.instance().start()