package com.example.healthiconiq;

import android.net.Uri;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CHATGPT_API {

    public static String getData(Uri imageUri, String language, String apiKey) {
        try {
            // Read the image file as bytes
            byte[] imageBytes = Files.readAllBytes(Paths.get(imageUri.getPath()));

            // Create the boundary
            String boundary = "-----011000010111000001101001";

            // Build the multipart form data body
            String body = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"language\"\r\n\r\n" +
                    language + "\r\n" +
                    "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"api_key\"\r\n\r\n" +
                    apiKey + "\r\n" +
                    "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"image\"; filename=\"health.png\"\r\n" +
                    "Content-Type: image/png\r\n\r\n" +
                    new String(imageBytes) + "\r\n" +
                    "--" + boundary + "--\r\n";

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://medical.pythonanywhere.com/process_image"))
                    .header("Accept", "*/*")
                    .header("User-Agent", "Thunder Client (https://www.thunderclient.com)")
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            // Send the request and get the response
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            // Return the response body
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
