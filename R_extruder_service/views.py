import os
import subprocess
import rpy2.robjects as robjects
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView
from django.conf import settings
from django.http import HttpResponse
from R_extruder.settings import BASE_DIR
from .models import RScript
from .serializers import RScriptSerializer
import rpy2.robjects as robjects

class PostRunRScriptView(APIView):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.BASE_DIR = settings.BASE_DIR
        self.media_directory = os.path.join(self.BASE_DIR, 'media')

    def check_media_directory(self):
        if not os.path.exists(self.media_directory):
            os.makedirs(self.media_directory)

    def post(self, request, *args, **kwargs):
        try:
            # Get script
            r_script_serializer = RScriptSerializer(data=request.data)
            if r_script_serializer.is_valid():
                r_script = r_script_serializer.save()
            else:
                return Response({"error": "Invalid RScript data"}, status=status.HTTP_400_BAD_REQUEST)
        except Exception as e:
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

        # Check media directory existence
        self.check_media_directory()

        # paths for script and plot
        r_script_path = os.path.join(BASE_DIR, 'media', str(r_script.script)).replace("\\", "/")
        plot_file_path = os.path.join(BASE_DIR, 'media', 'plot.png').replace("\\", "/")
        try:
            # Read r script
            with open(r_script_path, 'r') as file:
                r_script_code = file.read()

            # Check if there is plot function, if it is add to make png plot
            if 'plot(' in r_script_code:
                r_script_code = r_script_code.replace('plot(', f'png(\'{plot_file_path}\')\nplot(')
                r_script_code += "\ndev.off()"

            # Save R script
            with open(r_script_path, 'w') as file:
                file.write(r_script_code)

            command = ['Rscript', r_script_path]
            result = subprocess.run(command, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

            return Response({"message": "R script executed successfully", "plot_url": "media/plot.png"},
                            status=status.HTTP_201_CREATED)
        except subprocess.CalledProcessError as e:
            return Response({"error": f"Error executing R script: {e}"}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

    def get(self, request, *args, **kwargs):
        try:
            scripts = RScript.objects.all()
            serializer = RScriptSerializer(scripts, many=True)
            return Response(serializer.data, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

class RScriptRetrieveView(APIView):
    def get(self, request, pk, *args, **kwargs):
        try:
            r_script = RScript.objects.get(pk=pk)
        except RScript.DoesNotExist:
            return Response({"error": "RScript not found"}, status=status.HTTP_404_NOT_FOUND)

        serializer = RScriptSerializer(r_script)
        return Response(serializer.data, status=status.HTTP_200_OK)
