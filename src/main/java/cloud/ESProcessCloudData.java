package cloud;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ESProcessCloudData implements IProcessCloudData {
    private ElasticsearchClient esClient;

    public ESProcessCloudData(){
        init();
    }

    public void init(){

        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost("localhost", 9200, "https"));
        RestClientBuilder.HttpClientConfigCallback httpClientConfigCallback = new HttpClientConfigCallbackImpl();
        restClientBuilder.setHttpClientConfigCallback(httpClientConfigCallback);

        // Create the low-level client
        RestClient restClient = restClientBuilder.build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // create the API client
        esClient = new ElasticsearchClient(transport);

        try {
            System.out.println(esClient.info().name());
            System.out.println(esClient.info().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createIndex(List<FileDataModel> dataList) {

        if (dataList == null || dataList.isEmpty()){
            return;
        }

        BulkRequest.Builder br = new BulkRequest.Builder();
        for(FileDataModel fileDataModel : dataList){
            br.operations(op -> op
                    .index(idx -> idx
                            .index("files")
                            .id(fileDataModel.getPath())
                            .document(fileDataModel)
                    )
            );
        }
        try {
            BulkResponse result = esClient.bulk(br.build());

            if (result.errors()) {
                System.out.println("Bulk Indexing error");
                for (BulkResponseItem item : result.items()) {
                    if (item.error() != null) {
                        System.out.println(item.error().reason());
                    }
                }
            } else {
                System.out.println("Indexed successfully");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println(esClient);
    }

    @Override
    public void recreateIndex(List<FileDataModel> dataList) {

        System.out.println("recreateIndex enter!");
        deleteIndex("files");

        //create the index..
        createIndex(dataList);
    }

    public void deleteIndex(String index){
        try {
            DeleteIndexRequest request = new DeleteIndexRequest.Builder().index(index).build();
            esClient.indices().delete(request);
            System.out.println("delete index success!");
        } catch (ElasticsearchException | IOException exception) {
            System.out.println("Failed to delete index!");
        }
    }

    @Override
    public List<FileDataModel> searchData(String searchText) {

        if(searchText.isEmpty()){
            System.out.println("input error: searchText is empty!");
            return null;
        }

        List<FileDataModel> res = null;
        try {
            SearchResponse<FileDataModel> queryResponse = esClient.search(s -> s
                            .index("files")
                            .query(q -> q
                                    .match(t -> t
                                            .field("content")
                                            .query(searchText)
                                    )
                            ),
                    FileDataModel.class
            );

            TotalHits total = queryResponse.hits().total();

            if(total.value() == 0){
                System.out.println("No search record found!");
                return null;
            }

            List<Hit<FileDataModel>> hits = queryResponse.hits().hits();
            res = new ArrayList<>();
            for (Hit<FileDataModel> hit: hits) {
                FileDataModel filedata = hit.source();
                res.add(filedata);
                //System.out.println("Found file " + filedata.getName() + ", file path: " + filedata.getPath() + ", file content: "+filedata.getContent() + ", score " + hit.score());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
}


}

class HttpClientConfigCallbackImpl implements RestClientBuilder.HttpClientConfigCallback{

    @Override
    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
        try{
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials("elastic", "VWNQwIEe1GDFCGPHXPpk");
            credentialsProvider.setCredentials(AuthScope.ANY, usernamePasswordCredentials);
            httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

            String cert_location = "/Users/amit91.kumar/Documents/CloudDoc/elasticsearch-8.10.1/config/certs/elasticsearch.p12";

            File cert_file = new File(cert_location);
            SSLContextBuilder sslContextBuilder = SSLContexts.custom().loadTrustMaterial(cert_file, "password".toCharArray());
            SSLContext sslContext = sslContextBuilder.build();
            httpAsyncClientBuilder.setSSLContext(sslContext);

        }catch (Exception e){
            e.printStackTrace();
        }

        return httpAsyncClientBuilder;
    }
}
