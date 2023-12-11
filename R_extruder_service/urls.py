from django.urls import path
from .views import RScriptRetrieveView, PostRunRScriptView

urlpatterns = [
    path('extractors/',  PostRunRScriptView.as_view(), name='post-run-r-script'),
    path('extractors/<int:pk>', RScriptRetrieveView.as_view(), name='rscript-retrieve'),
]
