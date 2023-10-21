package com.cicaic;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DocumentIndexerTest {
    private static DocumentIndexer indexer;
    private static Path indexDirectoryPath;

    @BeforeAll
    public static void setUp() throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        indexer = new DocumentIndexer(ResourcePaths.INDEX_DIR.value(), analyzer);
        indexDirectoryPath = Paths.get(ResourcePaths.INDEX_DIR.value());
    }

    @Test
    public void testIndexFile() throws IOException, ParseException {
        indexer.indexFile(ResourcePaths.CRAN_ALL_1400.value());
        List<QueryReader.QueryData> queries = QueryReader.readQueriesFromFile(ResourcePaths.CRAN_QRY.value());
                

        // Search the index
        for(QueryReader.QueryData queryData : queries) {
            Query query = new QueryParser("text", new StandardAnalyzer()).parse((queryData.queryText.replaceAll("[^a-zA-Z0-9\\s]", "")));
            List<Document> matchingDocuments = searchIndex(query, Integer.parseInt(queryData.queryId));
            assertEquals(50, matchingDocuments.size()); // Adjust the expected count based on your test data
        }
        

        // Assert that you have the expected number of matching documents

        // Use Lucene's search API to verify the correctness of indexing
        // For example, you can search for documents by ID, title, or other fields
    }

     private List<Document> searchIndex(Query query, int queryId) throws IOException {
        // Create a FileWriter to write the results
        FileWriter resultsWriter = new FileWriter(ResourcePaths.RESULTS.value());
        Directory directory = FSDirectory.open(indexDirectoryPath);
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        TopDocs topDocs = searcher.search(query, 50); 
        ScoreDoc[] hits = topDocs.scoreDocs;

        List<Document> matchingDocuments = new ArrayList<>();
        for (int i = 0; i < topDocs.scoreDocs.length; i++) {
            int docId = topDocs.scoreDocs[i].doc;
            Document document = searcher.doc(docId);
            matchingDocuments.add(document);
            String result = (queryId + " Q0 " + document.get("id") + " " + (i + 1) + " " + hits[i].score + " run_id\n");
            resultsWriter.write(result);
            System.out.println(result);
        }
        resultsWriter.close();
        reader.close();
        return matchingDocuments;
    }

    @AfterAll
    public static void tearDown() {
        // Clean up resources, if needed
    }
}
