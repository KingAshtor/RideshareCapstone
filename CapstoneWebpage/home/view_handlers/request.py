
from django.http import HttpRequest
from django.contrib.sessions.backends.base import SessionBase

from typing import Tuple
import requests
from requests import Response

from home.util import api, logged_in, not_valid

def not_has(dict: dict, *args: tuple[str]):
    for input in args: 
        if (input not in dict): return True
    #
    return False

def handle_request(request: HttpRequest, session: SessionBase) -> Tuple[str, dict]:
    if (not logged_in(session) or "id" not in request.session):
        return ("home", None)

    email = request.session.get("email")

    if (request.method == "POST"):
        (err, body) = api(requests.get, "user/byEmail", [
            ("email", email) ])
        if (not err): 
            rider_id = body["usrID"]
        else: 
            return ("home", None)
        #

        if (not_has(request.POST,
            "from-line1", "from-city", "from-state", "from-zip",
            "to-line1", "to-city", "to-state", "to-zip"
        ) or request.POST.get("datetime") == ""):
            return (None, {"logged_in": logged_in(session)})
        #

        driver_id = request.session["id"]
        del request.session["id"]

        (err, body) = api(requests.post, "ride/addr/add", [
            ("line1", request.POST['from-line1']),
            ("line2", request.POST['from-line2']),
            ("city", request.POST['from-city']),
            ("state", request.POST['from-state']),
            ("zip", request.POST['from-zip'])
        ])
        if (not err): 
            from_addr_id = body
        else: 
            return (None, {"logged_in": logged_in(session)})
        #
        
        (err, body) = api(requests.post, "ride/addr/add", [
            ("line1", request.POST['to-line1']),
            ("line2", request.POST['to-line2']),
            ("city", request.POST['to-city']),
            ("state", request.POST['to-state']),
            ("zip", request.POST['to-zip'])
        ])
        if (not err): to_addr_id = body
        else: 
            api(requests.delete, "ride/addr/del", [
                ("id", from_addr_id)])
            return (None, {"logged_in": logged_in(session)})
        #

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
            return (None, {"logged_in": logged_in(session)})
        #

        (err, body) = api(requests.post, "ride/add", [
            ("route", route_id),
            ("rider", rider_id),
            ("datetime", request.POST['datetime'])
        ])
        if (err): 
            api(requests.delete, "ride/addr/del", [
                ("id", from_addr_id)])
            api(requests.delete, "ride/addr/del", [
                ("id", to_addr_id)])
            api(requests.delete, "ride/route/del", [
                ("id", route_id)])
        #

        return ("home", None)
    #

    return (None, {"logged_in": logged_in(session)})
#