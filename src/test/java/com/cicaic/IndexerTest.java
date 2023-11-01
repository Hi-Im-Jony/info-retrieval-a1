package com.cicaic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.List;

public class IndexerTest {

    private static Indexer indexer;

    @BeforeAll
    public static void setUp() throws IOException {
        // Initialize the Indexer with a StandardAnalyzer and BM25Similarity
        indexer = new Indexer("testIndex",  new BM25Similarity());
    }

    @Test
    public void testBuildIndex() throws IOException {
        // Inject the RAMDirectory instead of an actual directory

        // Create a mock IndexWriter
        IndexWriter mockIndexWriter = mock(IndexWriter.class);
        indexer.setIndexWriter(mockIndexWriter);
        // Call the buildIndex method
        indexer.buildIndex(ResourcePaths.CRAN_ALL_1400.value());

        // Verify that documents were added to the index
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        verify(mockIndexWriter, atLeast(1300)).addDocument(documentCaptor.capture());

        // Optionally, you can perform more meaningful assertions based on your document content
        List<Document> addedDocuments = documentCaptor.getAllValues();
        Document firstDocument = addedDocuments.get(0);

        // Assert the content of the added document
        assertEquals("experimental investigation of the aerodynamics of a wing in a slipstream . ", firstDocument.getField("title").stringValue());
        assertEquals("brenckman,m. ", firstDocument.getField("author").stringValue());
        assertEquals("experimental investigation of the aerodynamics of a wing in a slipstream .  " + 
                    " an experimental study of a wing in a propeller slipstream was made in order to determine " + 
                    "the spanwise distribution of the lift increase due to slipstream at different angles of attack " + 
                    "of the wing and at different free stream to slipstream velocity ratios .  " + 
                    "the results were intended in part as an evaluation basis for different theoretical treatments of this problem .   " + 
                    "the comparative span loading curves, together with supporting evidence, showed that a substantial part of the " +
                    "lift increment produced by the slipstream was due to a /destalling/ or boundary-layer-control effect ." +
                    "  the integrated remaining lift increment, after subtracting this destalling lift, was found to agree well" +
                    " with a potential flow theory .   an empirical evaluation of the destalling effects was made for the " +
                    "specific configuration of the experiment . ", firstDocument.getField("text").stringValue());
    }
}
