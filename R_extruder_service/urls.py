from django.urls import path
from .views import RScriptListCreateView, RScriptRetrieveView

urlpatterns = [
    path('extractors/', RScriptListCreateView.as_view(), name='rscript-list-create'),
    path('extractors/<int:pk>', RScriptRetrieveView.as_view(), name='rscript-retrieve'),
]