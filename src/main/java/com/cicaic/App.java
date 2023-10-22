package com.cicaic;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
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
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import com.cicaic.QueryReader.QueryData;

public class App {
    public static void main(String[] args) throws IOException, ParseException {
        evaluate("resources/results/vsm_results.txt", new EnglishAnalyzer(), new ClassicSimilarity());
        evaluate("resources/results/bm25_results.txt", new EnglishAnalyzer(), new BM25Similarity());
    }

    public static void evaluate(String resultFilePath, Analyzer analyzer, Similarity similarity) throws IOException, ParseException {
        System.out.println("Building Index");
        FinalFileIndexer indexer = new FinalFileIndexer(ResourcePaths.INDEX_DIR.value(), analyzer, similarity);
        indexer.buildIndex(ResourcePaths.CRAN_ALL_1400.value());
        
        System.out.println("Parsing Queries");
        List<QueryReader.QueryData> queries = QueryReader.readQueriesFromFile(ResourcePaths.CRAN_QRY.value());
        System.out.println("Generating Results");
        List<String> results = generateResults(indexer, queries);
        FileWriter resultsWriter = new FileWriter(resultFilePath);
        for (String result : results) {
            resultsWriter.write(result);
        }
        resultsWriter.close();
    }

    private static List<String> generateResults(FinalFileIndexer indexer, List<QueryData> queries) throws IOException, ParseException {
        List<String> scoreResults = new ArrayList<>();
        IndexReader reader = DirectoryReader.open(indexer.getIndex());
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(indexer.getSimilarity());

        TopDocs topDocs;
        ScoreDoc[] hits;
        for (QueryData queryData : queries) {
            Query query = new QueryParser("text", indexer.getAnalyzer())
                .parse((queryData.queryText.replaceAll("[^a-zA-Z0-9\\s]", "")));
            topDocs = searcher.search(query, 50);
            hits = topDocs.scoreDocs;
            List<Document> matchingDocuments = new ArrayList<>();
            Set<String> seenDocumentIds = new HashSet<>(); 

            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                int docId = topDocs.scoreDocs[i].doc;
                Document document = searcher.doc(docId);
                String documentId = document.get("id");

                // Check if we've already seen this document ID
                if (!seenDocumentIds.contains(documentId)) {
                    matchingDocuments.add(document);
                    seenDocumentIds.add(documentId); // Add the document ID to the set to track it
                    scoreResults.add(queryData.queryId + " Q0 " + documentId + " " + (i + 1) + " " + hits[i].score + " run_id\n");
                }
            }   
        }

        return scoreResults;
    }
    
}
