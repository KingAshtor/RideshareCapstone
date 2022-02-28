from django import template

register = template.Library()

@register.filter("value")
def value(key, dict):
    return dict[key]

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
