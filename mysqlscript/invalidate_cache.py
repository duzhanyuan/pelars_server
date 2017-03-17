import requests
from requests import Session
import json,os,pprint,sys,getpass

def gettoken(user,password):
	session = Session()
	r = session.post('http://localhost:8080/pelars/password',params={"user":user,"pwd":password})
	if r.status_code != 200:
		return None
	else:
		return json.loads(r.text)["token"]

if(len(sys.argv) == 1):
	print("session required")
else:
	t = gettoken("lor.landolfi@gmail.com", "20aprile")
	if t:
		r = requests.post("http://localhost:8080/pelars/data/" + sys.argv[1] + "?token=" + t, data={})
		print(r.text)
	else:
		print("echo Not valid")