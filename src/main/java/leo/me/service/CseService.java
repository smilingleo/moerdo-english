package leo.me.service;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class CseService {

    /** Google Developer API key. */
    private String developerKey;

    /** Google CSE context key as present in the URL. */
    private String cx;

    /** Google API client instance. */
    private Customsearch customsearch;


    public CseService(String developerKey, String cx) {
        this.developerKey = developerKey;
        this.cx = cx;
        this.customsearch = buildCustomsearch();
    }

    private Customsearch buildCustomsearch() {
        Customsearch customsearch = new Customsearch(new NetHttpTransport(), new JacksonFactory(), createHttpRequestInitializer());
        return customsearch;
    }

    /**
     * Create a dummy http request initializer. (Google CSE does not require anything)
     */
    private static HttpRequestInitializer createHttpRequestInitializer() {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                request.setConnectTimeout(30000);
                request.setReadTimeout(2*60*1000);
            }
        };
    }

    public List<Result> searchImages(String query) {
        List<Result> items = Collections.emptyList();
        try {
            com.google.api.services.customsearch.Customsearch.Cse.List list = customsearch.cse().list(query);
            list.setKey(developerKey);
            list.setCx(cx);
            list.setFileType("jpg");
            list.setSearchType("image");
            // return 9 images, to have a 3x3 layout.
            list.setNum(9L);
            Search results = list.execute();
            items = results.getItems();
        } catch (IOException e) {
            e.printStackTrace(); // Ignore error and do not show pictures
        }
        return items;
    }
}
