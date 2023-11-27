import uuid

from django.db import models

# Create your models here.
class RScript(models.Model):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4(), editable=False)
    fileName = models.CharField(max_length=100, unique=True)
    content = models.TextField()

    class Meta:
        db_table = "scripts"
        ordering = ["-createdAt"]
        def __str__(self) -> str:
            return self.fileName