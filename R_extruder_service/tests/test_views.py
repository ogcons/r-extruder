import os
import subprocess
from django.urls import reverse
from django.core.files.uploadedfile import SimpleUploadedFile
from django.test import TestCase
from rest_framework.test import APIClient
from rest_framework import status
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
                M027 = list(
       time = c(    0,     0,  0.25,  0.25,     1,     1,     2,     2,     7,     7,    15,    15,    21,    21,    30,    30,    60,    60,    91,    91,   120,   120,   123,   123),
    residue = c( 98.9, 101.5,  97.7,  99.5,  99.2,  98.0,  97.0,  98.4,  94.0,  91.7,  79.3,  79.3,  84.6,  78.0,  68.8,  70.5,  47.2,  48.5,  38.4,  38.3,  31.7,  30.8,  29.7,  28.8),
     weight = c(    1,     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,     1),
                      to = c("M054","M047"),
         FF = list(ini   = c(0.33,0.33),
                   fixed = c(0,0),
                   lower = c(0.0,0.0),
                   upper = c(1.0,1.0)),
                   sink  = TRUE,
       type = "SFO",
          k = list(ini   = 0.01055,
                   fixed = 0,
                   lower = 0.0,
                   upper = Inf),
         M0 = list(ini   = 98.69,
                   fixed = 0,
                   lower = 0.0,
                   upper = Inf))
    


       
       # Plotting M027
       plot(M027$time, M027$residue, type = "b", pch = 16, col = "blue",
            xlab = "Time", ylab = "Residue", main = "Time vs Residue (M027)")
       
       # Adding labels
       text(M027$time, M027$residue, labels = 1:length(M027$time), pos = 3, col = "red")
        """
        script_file = SimpleUploadedFile("script.R", r_code.encode("utf-8"), content_type="text/plain")
        r_script_data = {'script': script_file}

        # Print data
        print("Test data:", r_script_data)

        # Simulate POST
        response = self.client.post('/api/extractors/', {'script': script_file}, format='multipart')

        # Print response
        print("Response content:", response.content)

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

        # Simulate GET all
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
        # Given
        r_script = RScript.objects.create(script="Script content")  # Adjust this according to your model structure

        # When
        response = self.client.get(reverse('rscript-retrieve', kwargs={'pk': r_script.pk}))

        # Then
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        serialized_data = RScriptSerializer(r_script).data
        self.assertEqual(response.data, serialized_data)
