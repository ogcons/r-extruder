from django.test import TestCase
from django.core.files.uploadedfile import SimpleUploadedFile
from ..models import RScript
import os

class RScriptModelTest(TestCase):
    def setUp(self):
        self.sample_script = RScript.objects.create(
            script=SimpleUploadedFile("test_script.R", B"file_content")
        )
        self.rscript = RScript.objects.create(script='test_file')

    def test_rscript_creation(self):

        # when
        rscript = self.rscript

        # then
        self.assertEqual(rscript.script, 'test_file')

    def test_script_path_property(self):

        # when
        expected_path = os.path.basename(self.sample_script.script.name)

        with self.sample_script.script.open('r') as script_file:
            actual_content = script_file.read()

        # then
        self.assertEqual(self.sample_script.script_path, expected_path)
        self.assertEqual(actual_content, "file_content")