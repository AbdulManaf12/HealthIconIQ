# File: src/main/python/image_processor.py
import requests

def upload_image(image_data, url, headers, data):
    files = {'image': ('image.png', image_data, 'image/png')}
    response = requests.post(url, files=files, data=data, headers=headers)
    return response.text
