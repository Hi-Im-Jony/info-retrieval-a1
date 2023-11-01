package com.cicaic;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Searcher {

    public static void main(String[] args) {
        try {

            Directory index = FSDirectory.open(Paths.get(ResourcePaths.INDEX_DIR.value()));

            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);

            // Create an analyzer (use the same analyzer used during indexing)
            Analyzer analyzer = new EnglishAnalyzer(); 
            String[] fieldsToSearch = {"title", "text"};
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fieldsToSearch, analyzer);

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Enter your query (or type 'exit' to quit): ");
                String userInput = scanner.nextLine().trim();

                if (userInput.equalsIgnoreCase("exit")) {
                    break; 
                }

                try {
                    Query query = queryParser.parse(userInput); 
                    TopDocs topDocs = searcher.search(query, 10); 
                    ScoreDoc[] hits = topDocs.scoreDocs;
                    System.out.println("Search results:");
                    for (int i = 0; i < hits.length; i++) {
                        int docId = hits[i].doc;
                        Document document = searcher.doc(docId);
                        String documentId = document.get("id");
                        System.out.println("Rank " + (i + 1) + ": Document ID = " + documentId);
                    }
                } catch (ParseException e) {
                    System.out.println("Error parsing the query: " + e.getMessage());
                }
            }

            scanner.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
