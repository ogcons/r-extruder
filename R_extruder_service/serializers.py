from rest_framework import serializers
from R_extruder_service.models import RScript

class ScriptSerializer(serializers.ModelSerializer):
    class Meta:
        model = RScript
        fields = '__all__'