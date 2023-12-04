from django.urls import path
from .views import RScriptListCreateView, RScriptRetrieveView, RunRScriptView, generate_word_document, test_view

urlpatterns = [
    path('extractors/', RScriptListCreateView.as_view(), name='rscript-list-create'),
    path('extractors/<int:pk>', RScriptRetrieveView.as_view(), name='rscript-retrieve'),
    path('run_r_script/', RunRScriptView.as_view(), name='run-r-script'),
    path('test/', test_view, name='test-view'),
]
