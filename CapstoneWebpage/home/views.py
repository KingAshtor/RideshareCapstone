from django.http.request import HttpRequest
from django.shortcuts import redirect, render
from django.http import HttpResponseForbidden
import requests
import hashlib

def home(req):
    is_logged_in = logged_in(req)
    name = req.session["name"] if is_logged_in else ""
    context={"logged_in": is_logged_in, "name": name}

    return render(req, "home.html", context)

def logout(req):
    log_out(req)

    return redirect("home")

def login(req):
    is_logged_in = logged_in(req)
    name = req.session["name"] if is_logged_in else ""
    context={"logged_in": is_logged_in, "name": name}

    this = render(req, "login.html", context)

    if (req.method == "POST"):
        name = req.POST['name']
        res = requests.get(f"http://127.0.0.1:8080/api/salts/view?name={name}")
        if (res.status_code != 200): return this
            
        simple_hash = req.POST['password']
        salt = res.text
        final_hash = hashlib.sha256(f"{salt}{simple_hash}".encode('utf-8')).hexdigest()

        res = requests.get(f"http://127.0.0.1:8080/api/users/view?name={name}")
        if (res.status_code != 200): return this

        db_final_hash = res.json()["hashedPwd"]

        if (final_hash != db_final_hash): return this

        log_in(req)
        return redirect("home")
        
    return this

def register(req):
    is_logged_in = logged_in(req)
    name = req.session["name"] if is_logged_in else ""
    context={"logged_in": is_logged_in, "name": name}
    this = render(req, "register.html", context)

    if (req.method == "POST"):
        name = req.POST['name']
        if (name == ""): return this

        simple_hash = req.POST['password']
        if (simple_hash == ""): return this

        res = requests.get(f"http://127.0.0.1:8080/api/salts/gen?name={name}")
        if (res.status_code != 200): return this

        salt = res.text
        final_hash = hashlib.sha256(f"{salt}{simple_hash}".encode('utf-8')).hexdigest()

        res = requests.post(f"http://127.0.0.1:8080/api/users/new", json={
            "name": name,
            "hashedPwd": final_hash
        })
        if (res.status_code != 200): return this

        return redirect("home")

    return this

def logged_in(req):
    return "name" in req.session

def log_in(req):
    req.session["name"] = req.POST["name"]
    
def log_out(req):
    del req.session["name"]