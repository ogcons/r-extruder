from django.test import TestCase
from django.urls import reverse, resolve
from rest_framework import status
from ..models import RScript

class TestUrls(TestCase):
    def setUp(self):
        RScript.objects.create(id=1, script='Test.R')

    def test_get_script_by_id(self):
        # given
        first_script_id = 1

        # when
        url = reverse('rscript-retrieve', args=[first_script_id])
        response = self.client.get(url)

        # then
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    def test_get_all_scripts(self):

        # when
        url = reverse('post-run-r-script')
        response = self.client.get(url)

        # then
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    def test_get_script_by_invalid_id(self):
        # when
        url = reverse('rscript-retrieve', args=[999])
        response = self.client.get(url)

        # then
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)
