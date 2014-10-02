package com.anyfetch.companion.api;

import android.test.suitebuilder.annotation.LargeTest;

import com.anyfetch.companion.api.helpers.BaseRequestTest;
import com.anyfetch.companion.api.pojo.Document;
import com.squareup.okhttp.OkHttpClient;

import java.util.Date;

@LargeTest
public class GetDocumentRequestTest extends BaseRequestTest {
    private GetDocumentRequest mRequest;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mRequest = new GetDocumentRequest(getContext(), "docId", "test context");
        mRequest.setOkHttpClient(new OkHttpClient());
    }

    public void test_loadDataFromNetwork() throws Exception {
        Document doc = mRequest.loadDataFromNetwork();

        assertEquals("file", doc.getType());
        assertEquals("doc0", doc.getDocumentId());
        assertEquals(new Date(0), doc.getDate());
        assertEquals("Doc0", doc.getTitle());
        assertEquals("Docu0", doc.getSnippet());
        assertEquals("Document0", doc.getFull());
    }
}