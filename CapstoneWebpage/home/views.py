import re
import uuid
from django.http.request import HttpRequest
from django.shortcuts import redirect, render
from django.http import HttpResponseForbidden
import requests
import hashlib

EMAIL_PATTERN = re.compile(rf"^[A-Za-z0-9\._\+]+@[\w]+\.[\w]+$")

def home(req):
    is_logged_in = logged_in(req)
    email = req.session["email"] if is_logged_in else ""
    context={"logged_in": is_logged_in, "email": email}

    return render(req, "home.html", context)

def logout(req):
    log_out(req)

    return redirect("home")

def login(req):
    is_logged_in = logged_in(req)
    email = req.session["email"] if is_logged_in else ""
    context={"logged_in": is_logged_in, "email": email}

    this = render(req, "login.html", context)

    if (req.method == "POST"):
        info = email, simple_hash = user_info(req)

        if (info == None): return this

        res = requests.get(f"http://127.0.0.1:8080/api/user/byEmail?email={email}")
        json = res.json()

        if (res.status_code != 200): return this
        
        salt = json["salt"]
        expected_final_hash = json["hashedPwd"]
        actual_final_hash = hashlib.sha256(f"{salt}{simple_hash}".encode('utf-8')).hexdigest()

        if (expected_final_hash != actual_final_hash): return this
        
        log_in(req)

        return redirect("home")
        
    return this

def register(req):
    is_logged_in = logged_in(req)
    email = req.session["email"] if is_logged_in else ""
    context={"logged_in": is_logged_in, "email": email}
    this = render(req, "register.html", context)

    if (req.method == "POST"):
        info = email, simple_hash = user_info(req)

        if (info == None): return this
        
        res = requests.get(f"http://127.0.0.1:8080/api/user/byEmail?email={email}")

        if (res.status_code != 404):
            return this

        salt = str(uuid.uuid4())
        final_hash = hashlib.sha256(f"{salt}{simple_hash}".encode('utf-8')).hexdigest()

        res = requests.put("http://127.0.0.1:8080/api/user/put", json = {
            "email": email,
            "salt": salt,
            "hashedPwd": final_hash
        })

        if (res.status_code != 200):
            return this

        return redirect("home")

    return this

def user_info(req):
    email = req.POST["email"]
    if (not EMAIL_PATTERN.match(email) or email == ""): 
        return None

    simple_hash = req.POST["password"]
    if (simple_hash == ""):
        return None

    return email, simple_hash

def logged_in(req):
    return "email" in req.session

def log_in(req):
    req.session["email"] = req.POST["email"]
    
def log_out(req):
    del req.session["email"]