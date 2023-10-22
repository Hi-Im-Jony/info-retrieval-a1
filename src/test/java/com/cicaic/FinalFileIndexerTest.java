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
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FinalFileIndexerTest {
    private static FinalFileIndexer indexer;
    private static Path indexDirectoryPath;
    

    @BeforeAll
    public static void setUp() throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        
        // Pass the desired similarity model to the indexer
        Similarity similarity = new BM25Similarity();  // You can change to another similarity model
        
        indexer = new FinalFileIndexer(ResourcePaths.INDEX_DIR.value(), analyzer, similarity);  // Pass similarity model
        indexDirectoryPath = Paths.get(ResourcePaths.INDEX_DIR.value());
    }

    @Test
    public void testIndexFile() throws IOException, ParseException {
        indexer.buildIndex(ResourcePaths.CRAN_ALL_1400.value());
        List<QueryReader.QueryData> queries = QueryReader.readQueriesFromFile(ResourcePaths.CRAN_QRY.value());
        FileWriter resultsWriter = new FileWriter(ResourcePaths.RESULTS.value());

        // Search the index
        for(QueryReader.QueryData queryData : queries) {
            Query query = new QueryParser("text", new StandardAnalyzer()).parse((queryData.queryText.replaceAll("[^a-zA-Z0-9\\s]", "")));
            List<Document> matchingDocuments = searchIndex(query, Integer.parseInt(queryData.queryId), resultsWriter);
            assertEquals(50, matchingDocuments.size()); // Adjust the expected count based on your test data
        }
        resultsWriter.close();
    }

     private List<Document> searchIndex(Query query, int queryId, FileWriter resultsWriter) throws IOException {
        // Create a FileWriter to write the results
        
        Directory directory = FSDirectory.open(indexDirectoryPath);
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(indexer.getSimilarity());

        TopDocs topDocs = searcher.search(query, 1000); 
        ScoreDoc[] hits = topDocs.scoreDocs;

        List<Document> matchingDocuments = new ArrayList<>();
        Set<String> seenDocumentIds = new HashSet<>(); // To track seen document IDs
        int numResultsAdded = 0; // Track the number of results added

        for (int i = 0; i < topDocs.scoreDocs.length && numResultsAdded < 50; i++) {
            int docId = topDocs.scoreDocs[i].doc;
            Document document = searcher.doc(docId);
            String documentId = document.get("id");

            // Check if we've already seen this document ID
            if (!seenDocumentIds.contains(documentId)) {
                matchingDocuments.add(document);
                seenDocumentIds.add(documentId); // Add the document ID to the set to track it
                String result = (queryId + " Q0 " + documentId + " " + (i + 1) + " " + hits[i].score + " run_id\n");
                resultsWriter.write(result);
                System.out.println(result);
                numResultsAdded++; // Increment the count of added results
            }
        }
        reader.close();
        return matchingDocuments;
    }

    @AfterAll
    public static void tearDown() {
        // Clean up resources, if needed
    }
}
