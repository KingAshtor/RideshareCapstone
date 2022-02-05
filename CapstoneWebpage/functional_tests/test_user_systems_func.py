from cgi import test
import uuid
from selenium.webdriver import *
from selenium.webdriver.common.keys import Keys
from .base import FunctionalTest

class RegisterTest(FunctionalTest):
    def test_register_duplicate(self):
        self.browser.get(self.live_server_url)

        name = str(uuid.uuid4())
        password = str(uuid.uuid4())

        self.browser.find_element_by_id("register").send_keys(Keys.ENTER)
        self.browser.find_element_by_id("name").send_keys(name)
        self.browser.find_element_by_id("password").send_keys(password)
        self.browser.find_element_by_id("submit").send_keys(Keys.ENTER)

        self.browser.find_element_by_id("register").send_keys(Keys.ENTER)
        self.browser.find_element_by_id("name").send_keys(name)
        self.browser.find_element_by_id("password").send_keys(password)
        self.browser.find_element_by_id("submit").send_keys(Keys.ENTER)

        self.assertEqual(self.browser.title, "Register")

class LoginTest(FunctionalTest):
    def test_login_process(self):
        self.browser.get(self.live_server_url)

        name = str(uuid.uuid4())
        password = str(uuid.uuid4())

        self.browser.find_element_by_id("register").send_keys(Keys.ENTER)
        self.browser.find_element_by_id("name").send_keys(name)
        self.browser.find_element_by_id("password").send_keys(password)
        self.browser.find_element_by_id("submit").send_keys(Keys.ENTER)

        self.browser.find_element_by_id("login").send_keys(Keys.ENTER)
        self.browser.find_element_by_id("name").send_keys(name)
        self.browser.find_element_by_id("password").send_keys(password)
        self.browser.find_element_by_id("submit").send_keys(Keys.ENTER)

        self.assertEqual(self.browser.title, "Home")

class LogoutTest(FunctionalTest): 
    def test_logout_after_register_and_login(self):
        self.browser.get(self.live_server_url)

        name = str(uuid.uuid4())
        password = str(uuid.uuid4())

        self.browser.find_element_by_id("register").send_keys(Keys.ENTER)
        self.browser.find_element_by_id("name").send_keys(name)
        self.browser.find_element_by_id("password").send_keys(password)
        self.browser.find_element_by_id("submit").send_keys(Keys.ENTER)

        self.browser.find_element_by_id("login").send_keys(Keys.ENTER)
        self.browser.find_element_by_id("name").send_keys(name)
        self.browser.find_element_by_id("password").send_keys(password)
        self.browser.find_element_by_id("submit").send_keys(Keys.ENTER)

        self.assertEqual(self.browser.title, "Home")
        throws = False
        try: self.browser.find_element_by_id("login")
        except: throws = True
        self.assertTrue(throws)

        self.browser.find_element_by_id("logout").send_keys(Keys.ENTER)

        self.assertEqual(self.browser.title, "Home")
        throws = False
        try: self.browser.find_element_by_id("logout")
        except: throws = True
        self.assertTrue(throws)
