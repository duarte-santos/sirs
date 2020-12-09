import tornado.ioloop
import tornado.web
import json
from Crypto.Hash import SHA256
from Crypto.Cipher import PKCS1_OAEP
from Crypto.PublicKey import RSA
from Crypto.Signature import PKCS1_v1_5
import base64

# ------------------------------- Get Signature ------------------------------- #

class getSignature(tornado.web.RequestHandler):
    def post(self):
        request = self.request.body

        with open('health.priv', 'rb') as f:
            privkey = RSA.importKey(f.read())

        # Create message digest
        digest = SHA256.new()
        digest.update(request)

        # Encrypt digest
        signer = PKCS1_v1_5.new(privkey)
        signature = signer.sign(digest)
        signature_b64 = base64.b64encode(signature)

        # Replace slashes or it all goes to hell 
        escaped_signature = signature_b64.decode().replace("/", "-")

        print("Signed!")
        self.write(escaped_signature)

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