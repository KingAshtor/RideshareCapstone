from django.http import HttpRequest
from django.http import HttpRequest
from django.contrib.sessions.backends.base import SessionBase

from typing import Tuple
from hashlib import sha256
import requests

from home.util import api, logged_in, EMAIL_PATTERN, log_in, not_valid

def handle_login(request: HttpRequest, session: SessionBase) -> Tuple[bool, dict]:
    if (logged_in(session)): 
        return (False, None)
    #
    email: str = request.POST.get("email")
    if (request.method == "POST"):
        if (request.POST["type"] == "login"):
            # Check validity of email and password
            simple_hash: str = request.POST.get("password")
            if (not EMAIL_PATTERN.match(email) or not_valid(email, simple_hash)): 
                return (False, None)
            #

            # Test whether user does not exist
            (err, body) = api(requests.get, "user/byEmail", [
                ("email", email) ])
            if (err): 
                return (False, None)
            #
        
            salt: str = body["salt"]
            expected_final_hash: str = body["hashedPwd"]
            actual_final_hash: str = sha256(f"{salt}{simple_hash}".encode('utf-8')).hexdigest()

            # Test whether password hashes match
            if (expected_final_hash != actual_final_hash): 
                return (False, None)
            #

            # Send confirmation email, body is token
            (err, body) = api(requests.get, "confirmation", [
                ("email", email) ])
            if (err): 
                return (False, None)
            #

            # Put token in session for confirm step
            request.session["token"] = body
            return (True, {"email": email})
        elif (request.POST["type"] == "confirm"):
            # Test whether input token is equal to emailed token
            expected_token: str = request.session.get("token")
            input_token: str = request.POST.get("token")
            if (expected_token == None or input_token == None or expected_token != int(input_token)): 
                return (False, None)
            #

            # Clear token from session and login
            request.session["token"] = None
            log_in(session, email)
            return (True, None)
        #
    #

    return (False, None)
#