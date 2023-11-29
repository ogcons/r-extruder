from rest_framework import serializers
from R_extruder_service.models import RScript, WordDocument


class RScriptSerializer(serializers.ModelSerializer):
    class Meta:
        model = RScript
        fields = ('script',)

class WordDocumentSerializer(serializers.ModelSerializer):
    class Meta:
        model = WordDocument
        fields = ('document', 'file_name', 'file_content',)
