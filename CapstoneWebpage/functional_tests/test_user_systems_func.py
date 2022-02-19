from cgi import test
import time
import uuid
from selenium.webdriver import *
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from .base import FunctionalTest

class RegisterTest(FunctionalTest):
    def test_register_duplicate(self):
        self.browser.get(self.live_server_url)

        email = f"{str(uuid.uuid4()).replace('-', '_')}@gmail.com"
        password = str(uuid.uuid4())
        
        self.browser.find_element(By.ID, "register").send_keys(Keys.ENTER)
        self.browser.find_element(By.ID, "email").send_keys(email)
        self.browser.find_element(By.ID, "password").send_keys(password)
        time.sleep(10)
        self.browser.find_element(By.ID, "submit").send_keys(Keys.ENTER)
        

        self.browser.find_element(By.ID, "register").send_keys(Keys.ENTER)
        self.browser.find_element(By.ID, "email").send_keys(email)
        self.browser.find_element(By.ID, "password").send_keys(password)
        
        self.browser.find_element(By.ID, "submit").send_keys(Keys.ENTER)
        
        
        self.assertEqual(self.browser.title, "Register")

class LoginTest(FunctionalTest):
    def test_login_process(self):
        wait = WebDriverWait(self.browser, 10)
        self.browser.get(self.live_server_url)

        email = f"{str(uuid.uuid4()).replace('-', '_')}@gmail.com"
        password = str(uuid.uuid4())

        self.browser.find_element(By.ID, "register").send_keys(Keys.ENTER)
        self.browser.find_element(By.ID, "email").send_keys(email)
        self.browser.find_element(By.ID, "password").send_keys(password)
        
        self.browser.find_element(By.ID, "submit").send_keys(Keys.ENTER)
        
        # wait.until(EC.title_is("Home"))

        self.browser.find_element(By.ID, "login").send_keys(Keys.ENTER)
        self.browser.find_element(By.ID, "email").send_keys(email)
        self.browser.find_element(By.ID, "password").send_keys(password)
        
        self.browser.find_element(By.ID, "submit").send_keys(Keys.ENTER)
        
        # wait.until(EC.element_to_be_clickable((By.ID, "logout")))

        self.assertEqual(self.browser.title, "Home")

class LogoutTest(FunctionalTest): 
    def test_logout_after_register_and_login(self):
        self.browser.get(self.live_server_url)
        
        email = f"{str(uuid.uuid4()).replace('-', '_')}@gmail.com"
        password = str(uuid.uuid4())

        self.browser.find_element(By.ID, "register").send_keys(Keys.ENTER)
        self.browser.find_element(By.ID, "email").send_keys(email)
        self.browser.find_element(By.ID, "password").send_keys(password)
        
        self.browser.find_element(By.ID, "submit").send_keys(Keys.ENTER)
        

        self.browser.find_element(By.ID, "login").send_keys(Keys.ENTER)
        self.browser.find_element(By.ID, "email").send_keys(email)
        self.browser.find_element(By.ID, "password").send_keys(password)
        
        self.browser.find_element(By.ID, "submit").send_keys(Keys.ENTER)
        

        self.assertEqual(self.browser.title, "Home")
        throws = False
        try: self.browser.find_element(By.ID, "login")
        except: throws = True
        self.assertTrue(throws)

        self.browser.find_element(By.ID, "logout").send_keys(Keys.ENTER)

        self.assertEqual(self.browser.title, "Home")
        throws = False
        try: self.browser.find_element(By.ID, "logout")
        except: throws = True
        self.assertTrue(throws)
