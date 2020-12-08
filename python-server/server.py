import tornado.ioloop
import tornado.web
import json

infected = []

class getToken(tornado.web.RequestHandler):
    def get(self):
        self.write("Hello, world")

class saveInfected(tornado.web.RequestHandler):
    global infected
    def post(self):
        infected = self.request.body
        infected = infected.decode()
        print(infected)

        if not infected:
            response = {
                'error': True,
                'msg':'Please provide an infected list'
            }
        else:
            response = {
                'error': False,
                'received': infected,
                'msg':'Thank You'
            }

        self.write(response)


application = tornado.web.Application([
    (r'/', getToken),
    (r'/saveinfected', saveInfected)
])

if __name__ == "__main__":
    http_server = tornado.httpserver.HTTPServer(application, ssl_options={
        "certfile": "health.crt",
        "keyfile": "health.key"
    })
    http_server.listen(8888)
    tornado.ioloop.IOLoop.instance().start()