import os
import subprocess
from rest_framework.test import APIClient
from rest_framework import status
from django.urls import reverse
from django.core.files.uploadedfile import SimpleUploadedFile
from django.test import TestCase
from django.conf import settings
from ..models import RScript
from ..serializers import RScriptSerializer

class PostRunRScriptViewTest(TestCase):
    def setUp(self):
        self.client = APIClient()
        self.base_dir = settings.BASE_DIR

    def test_post_run_r_script_view(self):
        # Temporary R Script
        r_code = """
                # Create some sample data
                x <- 1:10
                y <- c(2, 4, 6, 8, 10, 8, 6, 4, 2, 0)

                # Create a basic plot
                plot(x, y, type = "l", col = "blue", lwd = 2, main = "Simple Plot", xlab = "X-axis", ylab = "Y-axis")
                """
        script_file = SimpleUploadedFile("script.R", r_code.encode("utf-8"), content_type="text/plain")
        r_script_data = {'script': script_file}

        # Simulate POST
        response = self.client.post('/api/extractors/', {'script': script_file}, format='multipart')

        # Asserts
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(response.data['message'], 'R script executed successfully')
        self.assertIn('plot_url', response.data)

        self.assertEqual(RScript.objects.count(), 1)
        saved_r_script = RScript.objects.first()
        self.assertIsNotNone(saved_r_script)

        # Check if the plot file was created
        plot_file_path = os.path.join(settings.BASE_DIR, 'media', 'plot.png').replace("\\", "/")
        self.assertTrue(os.path.exists(plot_file_path))

    def test_get_all_r_scripts(self):
        # Create R Scripts
        r_script_data_1 = {'script': 'your_sample_script_content_1'}
        r_script_data_2 = {'script': 'your_sample_script_content_2'}

        r_script_1 = RScript.objects.create(**r_script_data_1)
        r_script_2 = RScript.objects.create(**r_script_data_2)

        # Simulate GET all R scripts
        response = self.client.get('/api/extractors/')

        # Assert
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        # Compare data
        serializer = RScriptSerializer([r_script_1, r_script_2], many=True)
        self.assertEqual(response.data, serializer.data)

class RScriptRetrieveViewTest(TestCase):
    def setUp(self):
        self.client = APIClient()

    def test_retrieve_rscript(self):
        # given
        r_script = RScript.objects.create(script="Script content")

        # when
        response = self.client.get(reverse('rscript-retrieve', kwargs={'pk': r_script.pk}))

        # then
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        serialized_data = RScriptSerializer(r_script).data
        self.assertEqual(response.data, serialized_data)