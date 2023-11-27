from rest_framework.parsers import MultiPartParser, FormParser
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework import status, generics
from .models import RScript
from .serializers import RScriptSerializer

class RScriptListCreateView(APIView):
    parser_classes = (MultiPartParser, FormParser)

    def post(self, request, *args, **kwargs):
        try:
            serializer = RScriptSerializer(data=request.data)
            if serializer.is_valid():
                serializer.save()
                return Response(serializer.data, status=status.HTTP_201_CREATED)
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
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

class RScriptListCreateView(generics.ListCreateAPIView):
    queryset = RScript.objects.all()
    serializer_class = RScriptSerializer