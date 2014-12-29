package com.anyfetch.companion.commons.api.testhelpers;

import com.anyfetch.companion.commons.api.pojo.Document;
import com.anyfetch.companion.commons.api.pojo.DocumentsList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import java.util.Date;

public class MockApiFactory {
    private static DocumentsList createFakeDocumentSet(int amount, boolean isImportant) {
        DocumentsList docs = new DocumentsList();
        for (int i = 0; i < amount; i++) {
            docs.add(new Document(
                            "file",
                            "Dropbox",
                            "doc" + i,
                            isImportant ? "company0" : null,
                            isImportant ? "event0" : null,
                            new Date(0),
                            "Doc" + i,
                            "Docu" + i,
                            "Document" + i,
                            "url",
                            false)
            );
        }
        return docs;
    }

    public static MockWebServer create(final String testToken) {
        MockWebServer mock = new MockWebServer();
        mock.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                Gson gson = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        .create();
                MockResponse response = null;
                // 1. Check for Auth
                if (!request.getHeader("Authorization").equals("Bearer " + testToken)) {
                    response = new MockResponse();
                    response.setResponseCode(403);
                    response.setStatus("Forbidden");
                }
                // 2. Route request
                if (response == null) {
                    if (request.getPath().startsWith("/documents?") && request.getMethod().equals("GET")) {
                        response = new MockResponse();
                        response.setBody(gson.toJson(createFakeDocumentSet(2, false)));
                    } else if (request.getPath().startsWith("/documents/") && request.getMethod().equals("GET")) {
                        response = new MockResponse();
                        response.setBody(gson.toJson(createFakeDocumentSet(1, false).get(0)));
                    } else if (request.getPath().startsWith("/events/eventId/importants") && request.getMethod().equals("GET")) {
                        response = new MockResponse();
                        response.setBody(gson.toJson(createFakeDocumentSet(2, true)));
                    } else if (request.getPath().startsWith("/events/eventId/importants/") && request.getMethod().equals("POST")) {
                        response = new MockResponse();
                        response.setResponseCode(204);
                    } else if (request.getPath().startsWith("/events/eventId/importants/") && request.getMethod().equals("DELETE")) {
                        response = new MockResponse();
                        response.setResponseCode(204);
                    }
                }
                // 3. 404 otherwise
                if (response == null) {
                    response = new MockResponse();
                    response.setResponseCode(404);
                    response.setStatus("Not Found");
                }

                return response;
            }
        });

        return mock;
    }
}
