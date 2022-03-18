from datetime import datetime, timezone
import re
import time
import uuid
from django.http.request import HttpRequest
from django.shortcuts import redirect, render
from django.http import HttpResponseForbidden
import pytz
import requests
import hashlib
import socket

EMAIL_PATTERN = re.compile(rf"^[A-Za-z0-9\._\+]+@[\w]+\.[\w]+$")
EXTERNAL = "146.168.217.8"
SPRING = f"http://127.0.0.1:8080/api"

def into(context, into, values):
    for value in values:
        context[value] = into[value]

def api_param(obj):
    (key, value) = obj
    
    return f"{key}={value}"

def api(method, endpoint, objects = None, body = {}):
    if (objects != None and len(objects) > 0 and len(list(filter(lambda obj: obj != None, objects))) == 0): return (False, None)
    params = "&".join(map(api_param, objects)) if objects != None else ""
    request = method(f"{SPRING}/{endpoint}{'?' if (len(params) > 0) else ''}{params}", json=body)
    try: body = request.json()
    except: body = None
    return (False, body) if (request.status_code == 200) else (True, None)

def home(request):
    is_logged_in = logged_in(request)
    email = request.session.get("email")
    context={"logged_in": is_logged_in}

    if (not is_logged_in): return render(request, "home.html", context)

    ip = ip_address(request)
    if (ip == "127.0.0.1"): ip = EXTERNAL
    
    # Get timezone
    geo_response = requests.get(f"http://ip-api.com/json/{ip}")
    zone = geo_response.json()["timezone"]
    context["zone"] = zone

    if (request.method == "POST"):
        type = request.POST["type"]
        if (type == "recurring"):
            id = int(request.POST["id"])
            value = True if (request.POST["value"] == "on") else False
            api(requests.post, "ride/route/recurring", [
                ("id", id),
                ("status", value)
            ])
        elif (type == "accept"):
            id = int(request.POST["id"])
            api(requests.post, "ride/accept", [
                ("id", id) ])
        elif (type == "decline"):
            id = int(request.POST["id"])
            api(requests.delete, "ride/del", [
                ("id", id) ])
        elif (type == "cancel"):
            id = int(request.POST["id"])
            api(requests.delete, "ride/del", [
                ("id", id) ])
        elif (type == "cancelreq"):
            id = int(request.POST["id"])
            (err, body) = api(requests.get, "ride", [
                ("id", id) ])
            
            if (not err):
                api(requests.delete, "ride/addr/del", [
                    ("id", body["route"]["startAddress"]["addressID"]) ])
                api(requests.delete, "ride/addr/del", [
                    ("id", body["route"]["endAddress"]["addressID"]) ])
                api(requests.delete, "ride/route/del", [
                    ("id", body["route"]["routeID"]) ])
                api(requests.delete, "ride/del", [
                    ("id", body["rideID"]) ])
    
    context["email"] = email
    # load user data into template
    (err, body) = api(requests.get, "user/byEmail", objects=[
        ("email", email) ])
    if (not err): into(context, body, ["fName", "lName", "roles"])
    else:
        log_out(request)
        is_logged_in = False; context={"logged_in": is_logged_in}
        return render(request, "home.html", context)

    # load routes into template
    (err, body) = api(requests.get, "ride/route/byEmail", [
        ("email", email) ])
    if (not err): context["routes"] = body
    
    rides = []
    #load rides into template 
    (err, body) = api(requests.get, "ride/byRiderEmail", [
        ("email", email) ])
    if (not err):
        for ride in body: rides.append(ride)
    (err, body) = api(requests.get, "ride/byDriverEmail", [
        ("email", email) ])
    if (not err):
        for ride in body: rides.append(ride)

    print(f"len: {len(rides)}")
    context["incoming"] = filter(lambda ride: not ride["accepted"] and not ride["completed"], rides)
    context["outgoing"] = filter(lambda ride: ride["accepted"] and not ride["completed"], rides)

    return render(request, "home.html", context)

def request(request):
    is_logged_in = logged_in(request)
    email = request.session.get("email")

    if (not is_logged_in or "id" not in request.session):
        return redirect("home")

    context={"logged_in": is_logged_in}
    this = render(request, "request.html", context)

    if (request.method == "POST"):
        if 'from-line1' not in request.POST or \
            'from-city' not in request.POST or \
            'from-state' not in request.POST or \
            'from-zip' not in request.POST or \
            'to-line1' not in request.POST or \
            'to-city' not in request.POST or \
            'to-state' not in request.POST or \
            'to-zip' not in request.POST or \
            request.POST["datetime"] == "":
            return this

        (err, body) = api(requests.get, "user/byEmail", [
            ("email", email) ])
        if (not err): rider_id = body["usrID"]
        else: return this

        driver_id = request.session["id"]
        del request.session["id"]

        (err, body) = api(requests.get, "user/byEmail", [
            ("email", email) ])
        if (not err): into(context, body, ["usrID"])
        else: return redirect("home")

        (err, body) = api(requests.post, "ride/addr/add", [
            ("line1", request.POST['from-line1']),
            ("line2", request.POST['from-line2']),
            ("city", request.POST['from-city']),
            ("state", request.POST['from-state']),
            ("zip", request.POST['from-zip'])
        ])
        if (not err): from_addr_id = body
        else: return this
        
        (err, body) = api(requests.post, "ride/addr/add", [
            ("line1", request.POST['to-line1']),
            ("line2", request.POST['to-line2']),
            ("city", request.POST['to-city']),
            ("state", request.POST['to-state']),
            ("zip", request.POST['to-zip'])
        ])
        if (not err): to_addr_id = body
        else: return this

        (err, body) = api(requests.post, "ride/route/add", [
            ("from", from_addr_id),
            ("to", to_addr_id),
            ("driver", driver_id)
        ])
        if (not err): route_id = body
        else:
            api(requests.delete, "ride/addr/del", [
                ("id", from_addr_id)])
            api(requests.delete, "ride/addr/del", [
                ("id", to_addr_id)])
            return this

        (err, body) = api(requests.post, "ride/add", [
            ("route", route_id),
            ("rider", rider_id),
            ("datetime", request.POST['datetime'])
        ])
        if (err): return this

        return redirect("users")

    return this

def users(request):
    is_logged_in = logged_in(request)

    if (not is_logged_in): return redirect("home")

    email = request.session.get("email")
    context={"logged_in": is_logged_in}

    (err, body) = api(requests.get, "user/byEmail", [
        ("email", email) ])
    if (not err): into(context, body, ["roles"])
    else: return redirect("home")

    (err, body) = api(requests.get, "user/list")
    if (not err): context["users"] = body
    else: return redirect("home")

    if (request.method == "POST"):
        refresh = \
            request.POST["type"] == "delete" \
            or request.POST["type"] == "promote" \
            or request.POST["type"] == "demote"
        if (request.POST["type"] == "search"):
            search = request.POST["search"]
            context["users"] = filter(lambda user: search in user["fName"] or search in user["lName"] or search in user["email"], context["users"])
        elif (request.POST["type"] == "delete"):
            user_email = request.POST["email"]
            api(requests.delete, "user/del/byEmail", [
                ("email", user_email) ])
            if (user_email == email): is_logged_in = False
        elif (request.POST["type"] == "promote"):
            email = request.POST["email"]
            api(requests.post, "roles/add/byEmail", [
                ("email", email),
                ("role", "admin")
            ])
        elif (request.POST["type"] == "demote"):
            email = request.POST["email"]
            api(requests.delete, "roles/del/byEmail", [
                ("email", email),
                ("role", "admin")
            ])
        elif (request.POST["type"] == "request"):
            request.session["id"] = request.POST["id"]
            return redirect("request")
        if (refresh):
            (err, body) = api(requests.get, "user/list")
            if (not err): context["users"] = body
            else: return redirect("home")

    if (not is_logged_in): return redirect("home")
    
    return render(request, "users.html", context)

def logout(request):
    log_out(request)

    return redirect("home")

def login(request):
    is_logged_in = logged_in(request)

    if (is_logged_in): return redirect("home")

    email = request.session["email"] if is_logged_in else ""
    this = render(request, "login.html")

    if (request.method == "POST"):
        email = request.POST["email"]
        if (not EMAIL_PATTERN.match(email) or email == ""): 
            return this

        simple_hash = request.POST["password"]
        if (simple_hash == ""):
            return this
        
        (err, body) = api(requests.get, "user/byEmail", [
            ("email", email) ])
        if (err): return this
        
        salt = body["salt"]
        expected_final_hash = body["hashedPwd"]
        actual_final_hash = hashlib.sha256(f"{salt}{simple_hash}".encode('utf-8')).hexdigest()

        if (expected_final_hash != actual_final_hash): return this
        
        log_in(request)

        return redirect("home")
        
    return this

def register(request):
    is_logged_in = logged_in(request)

    if (is_logged_in): return redirect("home")

    this = render(request, "register.html")

    if (request.method == "POST"):
        info = email, simple_hash, line1, line2, city, state, zip = user_info(request)
        eat = request.POST.get("eat")
        smoke = request.POST.get("smoke")

        if (eat == None or smoke == None or info == None): return this
        
        (err, _) = api(requests.get, "api/user/byEmail", [
            ("email", email) ])
        if (not err): return this

        salt = str(uuid.uuid4())
        final_hash = hashlib.sha256(f"{salt}{simple_hash}".encode('utf-8')).hexdigest()

        (err, body) = api(requests.post, "ride/addr/add", [
            ("line1", line1),
            ("line2", line2),
            ("city", city),
            ("state", state),
            ("zip", zip)
        ])
        if (not err): address_id = body
        else: return this

        (err, body) = api(requests.put, "user/put", body={
            "email": email,
            "fName": request.POST["first"],
            "lName": request.POST["last"],
            "salt": salt,
            "hashedPwd": final_hash,
            "homeAddress": address_id
        })
        if (err): return this
        
        rider = True if (request.POST.get("rider")) else False
        driver = True if (request.POST.get("driver")) else False

        if (rider): 
            api(requests.post, "roles/add/byEmail", [
                ("email", email),
                ("role", "rider")
            ])
        if (driver): 
            api(requests.post, "roles/add/byEmail", [
                ("email", email),
                ("role", "driver")
            ])
        api(requests.post, "roles/add/byEmail", [
            ("email", email),
            ("role", eat)
        ])
        api(requests.post, "roles/add/byEmail", [
            ("email", email),
            ("role", smoke)
        ])

        return redirect("home")

    return this

def edit(request):
    is_logged_in = logged_in(request)

    if (not is_logged_in): return redirect("home")

    email = request.session["email"]
    res = requests.get(f"http://127.0.0.1:8080/api/user/byEmail?email={email}")

    if (res.status_code == 200):
        json = res.json()
        context={
            "email": email,
            "fName": json["fName"],
            "lName": json["lName"],
            "roles": json["roles"]
        }

    this = render(request, "edit.html", context)

    # if (request.method == "POST"):
    # TODO ADD EDITING

    return this

def user_info(request):
    if (len(request.POST) == 0):
        return None

    email = request.POST["email"]
    if (not EMAIL_PATTERN.match(email) or email == ""): 
        return None

    simple_hash = request.POST["password"]
    if (simple_hash == ""):
        return None

    line1 = request.POST["line1"]
    if (line1 == ""):
        return None
        
    line2 = request.POST["line2"]
    
    city = request.POST["city"]
    if (city == ""):
        return None
      
    state = request.POST["state"]
    if (state == ""):
        return None
        
    zip = request.POST["zip"]
    if (zip == ""):
        return None

    return email, simple_hash, line1, line2, city, state, zip

def logged_in(request):
    return "email" in request.session

def log_in(request):
    request.session["email"] = request.POST["email"]
    
def log_out(request):
    if ("email" in request.session):
        del request.session["email"]

def ip_address(request):
    x_forwarded_for = request.META.get('HTTP_X_FORWARDED_FOR')

    if x_forwarded_for:
        ip = x_forwarded_for.split(',')[0]
    else:
        ip = request.META.get('REMOTE_ADDR')
    
    try:
        socket.inet_aton(ip)
    except socket.error:
        return ""
    return ip