
from django.http import HttpRequest
from django.contrib.sessions.backends.base import SessionBase

import requests

from home.util import api, logged_in, log_in, log_out, not_valid, into, ip_address, EXTERNAL

def load_rides(email: str):
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
    return rides

def not_has(dict: dict, *args: tuple[str]):
    for input in args: 
        if (input not in dict): return True
    #
    return False

def handle_home(request: HttpRequest, session: SessionBase) -> dict:
    if (not logged_in(session)): 
        return {"logged_in": False}
    #

    email = request.session.get("email")

    ip = ip_address(request)
    if (ip == "127.0.0.1"): 
        ip = EXTERNAL
    #

    # Get timezone
    geo_response = requests.get(f"http://ip-api.com/json/{ip}")
    zone = geo_response.json()["timezone"]

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
            #
        #
    #
    
    # load user data into template
    (err, user) = api(requests.get, "user/byEmail", objects=[
        ("email", email) ])
    if (err):
        log_out(request)
        return {"logged_in": logged_in(session)}
    #

    # load routes and rides
    (routes_err, routes) = api(requests.get, "ride/route/byEmail", [
        ("email", email) ])
    rides = load_rides(email)

    context = into({
        "logged_in": logged_in(session),
        "zone": zone,
        "email": email,
        "routes": routes if (not routes_err) else [],
        "incoming": filter(lambda ride: not ride["accepted"] and not ride["completed"], rides),
        "outgoing": filter(lambda ride: ride["accepted"] and not ride["completed"], rides)
    }, user, ["fName", "lName", "roles"])

    return context