from selenium import webdriver
from django.contrib.staticfiles.testing import StaticLiveServerTestCase

MAX_WAIT = 10

class FunctionalTest(StaticLiveServerTestCase):
    def setUp(self):
        self.browser = webdriver.Chrome()
        
    def tearDown(self):
        self.browser.quit()