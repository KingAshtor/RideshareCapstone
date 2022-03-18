from datetime import datetime
import re
from django import template
import pytz

register = template.Library()

@register.filter("value")
def value(key, dict):
    return dict[key]
    
@register.filter("time")
def time(dateTime):
    return re.sub(":\w{2}.\w{3}\+\w{2}:\w{2}$", "", dateTime)

@register.filter("len")
def len(arr):
    return len(arr)

@register.filter("tag")
def tag(value):
    if (value == "rider"):
        return "Rider"
    elif (value == "driver"):
        return "Driver"
    elif (value == "eater"):
        return "Eats"
    elif (value == "noeater"):
        return "Won't eat"
    elif (value == "anyeater"):
        return "Allow eating"
    elif (value == "smoker"):
        return "Smokes"
    elif (value == "nosmoker"):
        return "No smoking"
    elif (value == "anysmoker"):
        return "Allow smoking"
    elif (value == "admin"):
        return "Admin"
    return ""

@register.filter("timezone")
def timezone(time, zone):
    time = datetime.strptime(time, '%Y-%m-%dT%H:%M:%S.%f%z').astimezone(pytz.timezone(zone))
    return time.strftime("%Y-%m-%dT%H:%M")