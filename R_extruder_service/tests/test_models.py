from django.test import TestCase
from django.core.files.uploadedfile import SimpleUploadedFile
from django.core.exceptions import ObjectDoesNotExist
from ..models import RScript, WordDocument
import os

class RScriptModelTest(TestCase):
    @classmethod
    def setUpClass(cls):
        super().setUpClass()

        # create RScript instance
        cls.sample_script = RScript.objects.create(
            script=SimpleUploadedFile("test_script.R", b"file_content")
        )
        cls.rscript = RScript.objects.create(script='test_file')

        # create WordDocument instance
        cls.sample_document = WordDocument.objects.create(
            document=SimpleUploadedFile("test_document.docx", b"document_content"),
            file_name='test_document',
            file_content=b"document_content"
        )
        cls.word_document = WordDocument.objects.create(document='test_file.docx', file_name='test_file',
                                                        file_content=b"file_content")
    @classmethod
    def tearDownClass(cls):
        # delete testFiles after tests
        try:
            os.remove(cls.sample_script.script.path)
        except (ObjectDoesNotExist, FileNotFoundError):
            pass

        try:
            os.remove(cls.sample_document.document.path)
        except (ObjectDoesNotExist, FileNotFoundError):
            pass

        super().tearDownClass()

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

    def test_word_document_creation(self):
        # when
        word_document = self.word_document

        # then
        self.assertEqual(word_document.document, 'test_file.docx')
        self.assertEqual(word_document.file_name, 'test_file')
        self.assertEqual(word_document.file_content, b"file_content")

    def test_document_name_property(self):
        # given
        expected_name = os.path.basename(self.sample_document.document.name)

        # when
        with self.sample_document.document.open('rb') as document_file:
            actual_content = document_file.read()

        # then
        self.assertEqual(self.sample_document.document_name, expected_name)
        self.assertEqual(actual_content, b"document_content")
