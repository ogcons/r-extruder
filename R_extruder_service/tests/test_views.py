from django.test import TestCase
from rest_framework import status
from rest_framework.test import APIClient
from django.urls import reverse
from django.core.files.uploadedfile import SimpleUploadedFile
from ..models import RScript
import os

class TestView(TestCase):
    def setUp(self):
        self.client = APIClient()
        self.url = reverse('rscript-list-create')

    def create_test_file(self, content, filename='test_file.R'):
        return SimpleUploadedFile(filename, content.encode())

    def test_create_rscript(self):
        # given
        script_content = "Script content"

        # when
        script_file = self.create_test_file(script_content)
        data = {'script': script_file}
        response = self.client.post(self.url, data, format='multipart')

        # then
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)

    def test_retrieve_rscript(self):
        # given
        r_script = RScript.objects.create(script=self.create_test_file("Script content"))

        # when
        response = self.client.get(reverse('rscript-retrieve', kwargs={'pk': r_script.pk}))

        # then
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('script', response.data)

    def test_list_rscripts(self):
        # given
        RScript.objects.create(script=self.create_test_file("Script content 1"))
        RScript.objects.create(script=self.create_test_file("Script content 2"))

        # when
        response = self.client.get(self.url)

        # then
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 2)