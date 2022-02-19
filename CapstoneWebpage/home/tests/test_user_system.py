import uuid
from django.test import TestCase

class RegisterPageTest(TestCase):
    def test_register(self):
        email = f"{str(uuid.uuid4()).replace('-', '_')}@gmail.com"
        password = uuid.uuid4()
        res = self.client.post("/register", data={
            "email": email,
            "password": password
        })
        self.assertRedirects(res, '/')
        
    def test_register_empty_password(self):
        email = f"{str(uuid.uuid4()).replace('-', '_')}@gmail.com"
        res = self.client.post("/register", data={
            "email": email,
            "password": ""
        })
        self.assertTemplateUsed(res, 'register.html')
        
    def test_register_empty_username(self):
        password = uuid.uuid4()
        res = self.client.post("/register", data={
            "email": "",
            "password": password
        })
        self.assertTemplateUsed(res, 'register.html')

    def test_register_empty(self):
        res = self.client.post("/register", data={
            "email": "",
            "password": ""
        })
        self.assertTemplateUsed(res, 'register.html')

