from django.db import models
from django.conf import settings
import os

class RScript(models.Model):
    script = models.FileField(upload_to=settings.STATIC_R)

    @property
    def script_path(self):
        return os.path.basename(self.script.name)

class WordDocument(models.Model):
    document = models.FileField(upload_to='word_documents/')
    file_name = models.CharField(max_length=255)
    file_content = models.BinaryField()

    @property
    def document_name(self):
        return os.path.basename(self.document.name)