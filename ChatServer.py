import socket
import threading
import time

msgs = {}

class Messages(threading.Thread):
    def __init__(self, nickname):
        super().__init__(name=("TH"+str(len(users))))
        self.nickname = nickname
    def run(self):
        nickname = self.nickname
        con = users[nickname]
        if nickname in msgs:
            for mess in msgs[nickname]:
                con.send(mess)
                time.sleep(1)
            msgs.pop(nickname)
        while(True):
            try:
                data = con.recv(1024).decode()
                #print(data)
                if data[0:2] == "to":
                    i = data.find("msg")
                    to_nickname = data[3:i-1]
                    msg = data[i+4:]
                    mess = ("from:"+nickname+",msg:"+msg).encode()
                    try:
                        users[to_nickname].send(mess)
                    except:
                        if to_nickname not in msgs:
                            l = []
                            l.append(mess)
                            msgs[to_nickname]=l
                        else:
                            msgs[to_nickname].append(mess)
                else:
                    delUser(nickname, con)
                    return
            except:
                delUser(nickname, con)
                return

soc = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
host = "192.168.43.39"
port = 1234
soc.bind((host, port))
soc.listen(10)
users = {}
print("Connected")

def delUser(nickname, con):
    con.close()
    updateList()
    del users[nickname]
    print("Пользователь отключился: ", nickname)

def updateList():
    ls = list(users.keys())
    msg = ','.join(ls)
    for nickname in ls:
        try:
            users[nickname].send(msg.encode())
        except:
            pass

while(True):
    conn, addr = soc.accept()
    addr = addr[0]+":"+str(addr[1])
    nickname = conn.recv(1024).decode()
    print ("Пользователь подключился: "+nickname+", "+addr)
    users[nickname]=conn
    updateList()
    TH = Messages(nickname)
    TH.start()