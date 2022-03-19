from django.http import HttpRequest
from django.shortcuts import redirect, render
from home.util import log_out
from .view_handlers.register import handle_register
from .view_handlers.login import handle_login
from .view_handlers.home import handle_home
from .view_handlers.request import handle_request
from .view_handlers.users import handle_users


def home(request: HttpRequest):
    context = handle_home(request, request.session)
    return render(request, "home.html", context)

def request(request: HttpRequest):
    (to, context) = handle_request(request, request.session)
    if (to): return redirect(to)
    return render(request, "request.html", context)

def users(request):
    (to, context) = handle_users(request, request.session)
    if (to): 
        return redirect(to)
    return render(request, "users.html", context)

def logout(request):
    log_out(request.session)
    return redirect("home")

def login(request: HttpRequest):
    (success, context) = handle_login(request, request.session)
    if (not success):return render(request, "login.html")
    elif (context): return render(request, "confirmation.html", context)
    return redirect("home")

def register(request: HttpRequest):
    if not handle_register(request, request.session): return render(request, "register.html")
    return redirect("home")

def edit(request):
    # is_logged_in = logged_in(request)

    # if (not is_logged_in): return redirect("home")

    # email = request.session["email"]
    # res = requests.get(f"http://127.0.0.1:8080/api/user/byEmail?email={email}")

    # if (res.status_code == 200):
    #     json = res.json()
    #     context={
    #         "email": email,
    #         "fName": json["fName"],
    #         "lName": json["lName"],
    #         "roles": json["roles"]
    #     }

    # this = render(request, "edit.html", context)

    # # if (request.method == "POST"):


    '''TODO ADD EDITING'''


    return redirect("home")