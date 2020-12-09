import tornado.ioloop
import tornado.web
import json
from hashlib import sha256
from Crypto.Cipher import PKCS1_OAEP
from Crypto.PublicKey import RSA
import base64

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

# ------------------------------- Get Signature ------------------------------- #

class getSignature(tornado.web.RequestHandler):
    def post(self):
        request = self.request.body.decode()
        print(request)

        msg = "aaaaa"

        privkey = False
        with open('health.priv', 'rb') as f:
            privkey = RSA.importKey(f.read())
        with open('health.pub', 'rb') as f:
            pubkey = RSA.importKey(f.read())

        digest = sha256(request.encode('utf-8'))

        '''
        # Encrypt digest
        cipher = PKCS1_OAEP.new(privkey)
        signature = base64.b64encode(cipher.encrypt(msg.encode()))

        print(signature)

        # Verify with public key
        cipher = PKCS1_OAEP.new(privkey.publickey())
        decrypted_digest = cipher.decrypt(base64.b64decode(signature))
        
        print(base64.b64encode(decrypted_digest))'''

        signature = digest.hexdigest()
        print("Signed!: ", signature)
        self.write(signature)

# ----------------------------------- Stuff ---------------------------------- #

application = tornado.web.Application([
    (r'/', getSignature),
    (r'/getsignature', getSignature),
])

if __name__ == "__main__":
    http_server = tornado.httpserver.HTTPServer(application, ssl_options={
        "certfile": "health.crt",
        "keyfile": "health.priv"
    })
    http_server.listen(9999)
    tornado.ioloop.IOLoop.instance().start()