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
    email = req.session.get("email")
    context={"logged_in": is_logged_in}

    if (email != None):
        res = requests.get(f"http://127.0.0.1:8080/api/user/byEmail?email={email}")

        if (res.status_code == 200):
            json = res.json()
            context["email"] = email
            context["fName"] = json["fName"]
            context["lName"] = json["lName"]
            context["roles"] = json["roles"]
        else:
            log_out(req)
            is_logged_in = False
            context={"logged_in": is_logged_in}


    return render(req, "home.html", context)

def users(req):
    is_logged_in = logged_in(req)

    if (not is_logged_in): return redirect("home")

    email = req.session.get("email")
    context={"logged_in": is_logged_in}
    res = requests.get(f"http://127.0.0.1:8080/api/user/byEmail?email={email}")

    if (res.status_code != 200): return redirect("home")

    user = res.json()

    if (not is_logged_in or "admin" not in user["roles"]): return redirect("home")

    res = requests.get(f"http://127.0.0.1:8080/api/user/list")

    if (res.status_code != 200): return redirect("home")

    context={"users": res.json()}
    this = render(req, "users.html", context)

    if (req.method == "POST"):
        if (req.POST["type"] == "search"):
            search = req.POST["search"]
            context["users"] = filter(lambda user: search in user["fName"] or search in user["lName"] or search in user["email"], context["users"])
            this = render(req, "users.html", context)
        elif (req.POST["type"] == "delete"):
            email = req.POST["email"]
            res = requests.delete(f"http://127.0.0.1:8080/api/user/del/byEmail?email={email}")
            
            if (res.status_code == 200):
                res = requests.get(f"http://127.0.0.1:8080/api/user/list")

                if (res.status_code != 200): return redirect("home")

                context={"users": res.json()}
                this = render(req, "users.html", context)
        elif (req.POST["type"] == "promote"):
            email = req.POST["email"]
            res = requests.post(f"http://127.0.0.1:8080/api/roles/add/byEmail?email={email}&role=admin")

            if (res.status_code == 200):
                res = requests.get(f"http://127.0.0.1:8080/api/user/list")

                if (res.status_code != 200): return redirect("home")

                context={"users": res.json()}
                this = render(req, "users.html", context)
        elif (req.POST["type"] == "demote"):
            email = req.POST["email"]
            res = requests.delete(f"http://127.0.0.1:8080/api/roles/del/byEmail?email={email}&role=admin")
            
            if (res.status_code == 200):
                res = requests.get(f"http://127.0.0.1:8080/api/user/list")

                if (res.status_code != 200): return redirect("home")

                context={"users": res.json()}
                this = render(req, "users.html", context)

    if (not is_logged_in or "admin" not in user["roles"]): return redirect("home")
    
    return this

def edit(req):
    is_logged_in = logged_in(req)

    if (not is_logged_in): return redirect("home")

    email = req.session["email"]
    res = requests.get(f"http://127.0.0.1:8080/api/user/byEmail?email={email}")

    if (res.status_code == 200):
        json = res.json()
        context={
            "email": email,
            "fName": json["fName"],
            "lName": json["lName"],
            "roles": json["roles"]
        }

    this = render(req, "edit.html", context)

    # if (req.method == "POST"):
    # TODO ADD EDITING

    return this

def logout(req):
    log_out(req)

    return redirect("home")

def login(req):
    is_logged_in = logged_in(req)

    if (is_logged_in): return redirect("home")

    email = req.session["email"] if is_logged_in else ""
    this = render(req, "login.html")

    if (req.method == "POST"):
        info = email, simple_hash = user_info(req)

        if (info == None): return this

        res = requests.get(f"http://127.0.0.1:8080/api/user/byEmail?email={email}")

        if (res.status_code != 200): return this

        json = res.json()
        
        salt = json["salt"]
        expected_final_hash = json["hashedPwd"]
        actual_final_hash = hashlib.sha256(f"{salt}{simple_hash}".encode('utf-8')).hexdigest()

        if (expected_final_hash != actual_final_hash): return this
        
        log_in(req)

        return redirect("home")
        
    return this

def register(req):
    is_logged_in = logged_in(req)

    if (is_logged_in): return redirect("home")

    this = render(req, "register.html")

    if (req.method == "POST"):
        info = email, simple_hash = user_info(req)
        eat = req.POST.get("eat")
        smoke = req.POST.get("smoke")

        if (eat == None or smoke == None): return this
        if (info == None): return this
        
        res = requests.get(f"http://127.0.0.1:8080/api/user/byEmail?email={email}")

        if (res.status_code != 404):
            return this

        salt = str(uuid.uuid4())
        final_hash = hashlib.sha256(f"{salt}{simple_hash}".encode('utf-8')).hexdigest()

        res = requests.put("http://127.0.0.1:8080/api/user/put", json = {
            "email": email,
            "fName": req.POST["first"],
            "lName": req.POST["last"],
            "salt": salt,
            "hashedPwd": final_hash
        })

        if (res.status_code != 200):
            return this
        
        rider = True if (req.POST.get("rider")) else False
        driver = True if (req.POST.get("driver")) else False

        if (rider): requests.post(f"http://127.0.0.1:8080/api/roles/add/byEmail?email={email}&role=rider")
        if (driver): requests.post(f"http://127.0.0.1:8080/api/roles/add/byEmail?email={email}&role=driver")
        requests.post(f"http://127.0.0.1:8080/api/roles/add/byEmail?email={email}&role={eat}")
        requests.post(f"http://127.0.0.1:8080/api/roles/add/byEmail?email={email}&role={smoke}")

        return redirect("home")

    return this

def user_info(req):
    if (len(req.POST) == 0):
        return None

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
    if ("email" in req.session):
        del req.session["email"]