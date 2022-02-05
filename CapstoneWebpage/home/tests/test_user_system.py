import uuid
from django.test import TestCase

class RegisterPageTest(TestCase):
    def test_register(self):
        name = uuid.uuid4()
        password = uuid.uuid4()
        res = self.client.post("/register", data={
            "name": name,
            "password": password
        })
        self.assertRedirects(res, '/')
        
    def test_register_empty_password(self):
        name = uuid.uuid4()
        res = self.client.post("/register", data={
            "name": name,
            "password": ""
        })
        self.assertTemplateUsed(res, 'register.html')
        
    def test_register_empty_username(self):
        password = uuid.uuid4()
        res = self.client.post("/register", data={
            "name": "",
            "password": password
        })
        self.assertTemplateUsed(res, 'register.html')

    def test_register_empty(self):
        res = self.client.post("/register", data={
            "name": "",
            "password": ""
        })
        self.assertTemplateUsed(res, 'register.html')

