package com.cicaic;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import com.cicaic.QueryReader.QueryData;

public class Evaluator {
    public static void main(String[] args) throws IOException, ParseException {
        evaluate("resources/results/vsm_results.txt", new ClassicSimilarity());
        evaluate("resources/results/bm25_results.txt", new BM25Similarity());
    }

    public static void evaluate(String resultFilePath, Similarity similarity) throws IOException, ParseException {
        System.out.println("Building Index");
        Indexer indexer = new Indexer(ResourcePaths.INDEX_DIR.value(), similarity);
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

    private static List<String> generateResults(Indexer indexer, List<QueryData> queries) throws IOException, ParseException {
        List<String> scoreResults = new ArrayList<>();
        IndexReader reader = DirectoryReader.open(indexer.getIndex());
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(indexer.getSimilarity());

        for (QueryData queryData : queries) {
            Analyzer analyzer = indexer.getAnalyzer(); // Get the analyzer from your indexer
            String[] fieldsToSearch = {"title", "text"};
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fieldsToSearch, analyzer);
            Query query = queryParser.parse((queryData.queryText.replaceAll("[^a-zA-Z0-9\\s]", "")));

            TopDocs topDocs;
            int totalResults = 0;
            topDocs = searcher.search(query, 50);
            ScoreDoc[] hits = topDocs.scoreDocs;

            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                int docId = topDocs.scoreDocs[i].doc;
                Document document = searcher.doc(docId);
                String documentId = document.get("id");
                scoreResults.add(queryData.queryNum + " Q0 " + documentId + " " + (totalResults + 1) + " " + hits[i].score + " run_id\n");
            }
        }
    

        return scoreResults;
    }

    
}
