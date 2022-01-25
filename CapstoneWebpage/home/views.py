from django.http.request import HttpRequest
from django.shortcuts import redirect, render
from django.http import HttpResponseForbidden

def home(request):
    return render(request, 'home.html')