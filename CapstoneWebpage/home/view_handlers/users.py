
from django.http import HttpRequest
from django.contrib.sessions.backends.base import SessionBase

from typing import Tuple
import requests
from requests import Response

from home.util import api, logged_in, log_out, into, ip_address, EXTERNAL

def load_users():
    (err, users) = api(requests.get, "user/list")
    if (err): 
        return None
    #
    return users

def handle_users(request: HttpRequest, session: SessionBase) -> Tuple[str, dict]:
    if (not logged_in(session)): 
        return ("home", None)
    #

    email = session.get("email")
    ip = ip_address(request)
    if (ip == "127.0.0.1"): 
        ip = EXTERNAL
    #

    (err, user) = api(requests.get, "user/byEmail", [
        ("email", email) ])
    if (err): 
        log_out(email)
        return ("home", None)
    #
    
    # Get timezone
    geo_response: Response = requests.get(f"http://ip-api.com/json/{ip}")
    zone = geo_response.json()["timezone"]

    rides = []
    #load rides into template 
    (err, body) = api(requests.get, "ride/byRiderEmail", [
        ("email", email) ])
    if (not err):
        for ride in body: rides.append(ride)
    #
    (err, body) = api(requests.get, "ride/byDriverEmail", [
        ("email", email) ])
    if (not err):
        for ride in body: rides.append(ride)
    #

    users = load_users()
    if (not users):
        return ("home", None)
    #

    if (request.method == "POST"):
        if (request.POST["type"] == "search"):
            search = request.POST["search"]
            users = filter(lambda user: search in user["fName"] or search in user["lName"] or search in user["email"], users)
        elif (request.POST["type"] == "delete"):
            delete_email = request.POST["email"]
            api(requests.delete, "user/del/byEmail", [
                ("email", delete_email) ])
            if (delete_email == email): 
                users = filter(lambda user: user["email"] != email, users)
                log_out(session)
            #
        elif (request.POST["type"] == "promote"):
            email = request.POST["email"]
            api(requests.post, "roles/add/byEmail", [
                ("email", email),
                ("role", "admin")
            ])
            users = load_users()
        elif (request.POST["type"] == "demote"):
            email = request.POST["email"]
            api(requests.delete, "roles/del/byEmail", [
                ("email", email),
                ("role", "admin")
            ])
            users = load_users()
        elif (request.POST["type"] == "request"):
            session["id"] = request.POST["id"]
            return ("request", None)
        #
    #
    
    if (not users or not logged_in(session)):
        return ("home", None)
    #
    
    return (None, into({"logged_in": logged_in(session), "users": users, "rides": rides, "zone": zone}, user, ["roles"]))
#