from rest_framework import serializers
from R_extruder_service.models import RScript

class RScriptSerializer(serializers.ModelSerializer):
    class Meta:
        model = RScript
        fields = ('script',)