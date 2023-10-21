package com.cicaic;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;

public class CranfieldIndexerTest {
    private static IndexReader indexReader;
    private static IndexSearcher indexSearcher;

    @BeforeAll
    public static void setUp() throws Exception {
        // Index the Cranfield documents
        DocumentIndexer indexer = new DocumentIndexer(ResourcePaths.INDEX_DIR.value());
        indexer.indexDocuments(ResourcePaths.CRAN_ALL_1400.value());
        // Open the index
        Directory indexDirectory = FSDirectory.open(Paths.get("resources/index"));
        indexReader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(indexReader);
    }

    @Test
    public void testCranfieldIndexing() throws Exception {
        

        // Perform a sample search and ensure it returns results
        QueryParser queryParser = new QueryParser("contents", new StandardAnalyzer());
        Query query = queryParser.parse(
            "what similarity laws must be obeyed when constructing aeroelastic models of heated high speed aircraft");

        TopDocs topDocs = indexSearcher.search(query, 10);
        ScoreDoc[] hits = topDocs.scoreDocs;

        assertTrue(hits.length > 0);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        // Close the index reader
        if (indexReader != null) {
            indexReader.close();
        }
    }
}
