package com.cicaic;

import com.cicaic.QueryReader.QueryData;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.TopDocs;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ResultsFileGenerator {
    public static void main(String[] args) {
        try {
            // Create an IndexSearcher
            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(ResourcePaths.INDEX_DIR.value())));
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            // Read and parse your list of queries (query ID and query text)
            List<QueryData> queries = QueryReader.readQueriesFromFile(ResourcePaths.CRAN_QRY.value());

            // Create a FileWriter to write the results
            FileWriter resultsWriter = new FileWriter(ResourcePaths.RESULTS.value());
            
            for (QueryData queryData : queries) {
                try {
                    StandardAnalyzer analyzer = new StandardAnalyzer();
                    QueryParser queryParser = new QueryParser("content", analyzer);
                    Query query = queryParser.parse(queryData.queryText.replaceAll("[^a-zA-Z0-9\\s]", ""));

                    
                    int numResults = 50; 
                    TopDocs topDocs = indexSearcher.search(query, numResults);
                    ScoreDoc[] hits = topDocs.scoreDocs;

                    
                    for (int i = 0; i < hits.length; i++) {
                        System.out.println("hit");
                        int docId = hits[i].doc;
                        Document doc = indexReader.document(docId);
                        String result = (queryData.queryId + " Q0 " + doc.get("docId") + " " + (i + 1) + " " + hits[i].score + " run_id\n");
                        resultsWriter.write(result);
                        System.out.println(result);
                    }
                } catch (Exception e) {
                    System.out.println(queryData.queryId);
                    System.out.println(queryData.queryText);
                    resultsWriter.close();
                    throw e;
                }
                
            }

            // Close the results file
            resultsWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Document> searchIndex(Query query) throws IOException {
        Directory directory = FSDirectory.open(Paths.get(ResourcePaths.INDEX_DIR.value()));
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        TopDocs topDocs = searcher.search(query, 50); 

        List<Document> matchingDocuments = new ArrayList<>();
        for (int i = 0; i < topDocs.scoreDocs.length; i++) {
            int docId = topDocs.scoreDocs[i].doc;
            Document document = searcher.doc(docId);
            matchingDocuments.add(document);
        }

        reader.close();
        return matchingDocuments;
    }
}
