package com.cicaic;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DocumentIndexer {
    private Directory indexDirectory;
    private Analyzer analyzer;

    public DocumentIndexer(String indexDirectoryPath, Analyzer analyzer) {
        try {
            Path path = Paths.get(indexDirectoryPath);
            this.indexDirectory = FSDirectory.open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.analyzer = analyzer;
    }

    public void indexFile(String filePath) {
        try {
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter indexWriter = new IndexWriter(indexDirectory, config);

            List<Document> documents = CranFileParser.parseFile(filePath);
            for (Document document : documents) {
                indexWriter.addDocument(document);
            }

            indexWriter.close();
            System.out.println("Indexing complete.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

