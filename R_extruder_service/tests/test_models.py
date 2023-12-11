import os
from io import BytesIO
from django.conf import settings
from django.test import TestCase
from django.core.files.uploadedfile import SimpleUploadedFile
from django.core.exceptions import ObjectDoesNotExist
from docx import Document
from ..models import RScript, WordDocument

class TestModels(TestCase):

    # Creates instances
    @classmethod
    def setUpClass(cls):
        super().setUpClass()

        # Create RScript instance
        cls.sample_script = RScript.objects.create(
            script=SimpleUploadedFile("test_script.R", b"file_content")
        )
        cls.rscript = RScript.objects.create(script='test_file')

        # Create a valid DOCX
        content = "This is a valid DOCX file."
        buffer = BytesIO()
        doc = Document()
        doc.add_paragraph(content)
        doc.save(buffer)
        buffer.seek(0)
        cls.valid_docx = SimpleUploadedFile("valid_document.docx", buffer.read())

    # Tests the creation of an RScript
    def test_rscript_creation(self):
        # when
        rscript = self.rscript

        # then
        self.assertEqual(rscript.script, 'test_file')

    # Tests the script path
    def test_script_path_property(self):
        # when
        expected_path = os.path.basename(self.sample_script.script.name)

        with self.sample_script.script.open('r') as script_file:
            actual_content = script_file.read()

        # then
        self.assertEqual(self.sample_script.script_path, expected_path)
        self.assertEqual(actual_content, "file_content")

    # Test the creation of a valid DOCX
    def test_word_document_creation(self):
        # When
        content = "This is a valid DOCX file."
        word_document = WordDocument.objects.create(
            document=self.valid_docx,
            file_name='valid_file',
            file_content=content.encode('utf-8')
        )
        # Then
        self.assertEqual(word_document.document_name, 'valid_document.docx')
        self.assertEqual(word_document.file_name, 'valid_file')
        self.assertEqual(word_document.file_content, content.encode('utf-8'))

    # Deletes script & DOCX after tests
    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()

        # Remove the created objects in the database
        cls.sample_script.delete()

        # Delete associated files on the file system
        script_path = os.path.join(settings.MEDIA_ROOT, cls.sample_script.script.name)
        docx_path = os.path.join(settings.MEDIA_ROOT, 'word_documents', cls.valid_docx.name)

        if os.path.exists(script_path):
            os.remove(script_path)

        if os.path.exists(docx_path):
            os.remove(docx_path)