import re
import socket

EMAIL_PATTERN = re.compile(rf"^[A-Za-z0-9\._\+]+@[\w]+\.[\w]+(\.[\w]+)?$")
EXTERNAL = "146.168.217.8"
SPRING = f"http://127.0.0.1:8080/api"

def into(context, into, values):
    for value in values:
        context[value] = into[value]
    return context

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

def log_in(session, email):
    session["email"] = email

def logged_in(session):
    return "email" in session
    
def log_out(session):
    if ("email" in session):
        del session["email"]

def ip_address(request):
    forwarded = request.META.get('HTTP_X_FORWARDED_FOR')

    if forwarded: ip = forwarded.split(',')[0]
    else: ip = request.META.get('REMOTE_ADDR')
    
    try: socket.inet_aton(ip)
    except socket.error: return ""
    return ip

def not_valid(*args: tuple[str]):
    for input in filter(lambda arg: arg, args): 
        if (not input): return True
    #
    return False