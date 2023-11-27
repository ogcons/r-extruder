from rest_framework import serializers
from R_extruder_service.models import RScript

class RScriptForm(serializers.ModelSerializer):
    class Metal:
        model = RScript
        fields = ('script',)