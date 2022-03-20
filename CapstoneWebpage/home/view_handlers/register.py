from django.http import HttpRequest
from django.http import HttpRequest
from django.contrib.sessions.backends.base import SessionBase

from uuid import uuid4
from hashlib import sha256
import requests

from home.util import EMAIL_PATTERN, api, logged_in, not_valid

'''Create user, address, and roles'''
def handle_register(request: HttpRequest, session: SessionBase) -> bool:
    # Test if logged in
    if (logged_in(session)): 
        return False
    #

    if (request.method == "POST"):
        # Test if no form data
        if (len(request.POST) == 0):
            return False
        #

        # Collect form data
        email: str = request.POST.get("email")
        simple_hash: str = request.POST.get("password")
        line1: str = request.POST.get("line1")
        line2: str = request.POST.get("line2")
        city: str = request.POST.get("city")
        state: str = request.POST.get("state")
        zip: str = request.POST.get("zip")
        eat: str = request.POST.get("eat")
        smoke: str = request.POST.get("smoke")

        # Test validity of form data
        if (not_valid(email, simple_hash, line1, city, state, zip, eat, smoke) or not EMAIL_PATTERN.match(email) or email == ""):
            return False
        #

        # Test if user by email already exists
        (err, _) = api(requests.get, "api/user/byEmail", [
            ("email", email) ])
        if (not err): 
            return False
        #

        # Generate a salt, then salt and hash the simple hash
        salt: str = str(uuid4())
        final_hash: str = sha256(f"{salt}{simple_hash}".encode('utf-8')).hexdigest()

        # Create user address
        (err, body) = api(requests.post, "ride/addr/add", [
            ("line1", line1),
            ("line2", line2),
            ("city", city),
            ("state", state),
            ("zip", zip)
        ])
        if (not err): address_id = body
        else: 
            return False
        #

        # Create user with form data
        (err, body) = api(requests.put, "user/put", body={
            "email": email,
            "fName": request.POST["first"],
            "lName": request.POST["last"],
            "salt": salt,
            "hashedPwd": final_hash,
            "homeAddress": address_id
        })
        if (err):
            # Revert Address
            api(requests.post, "ride/addr/del", [
                ("id", address_id)
            ])

            return False
        #
        
        # Non null roles form data
        rider: bool = True if (request.POST.get("rider")) else False
        driver: bool = True if (request.POST.get("driver")) else False

        # Add roles to user
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

        return True
    #

    return False
#