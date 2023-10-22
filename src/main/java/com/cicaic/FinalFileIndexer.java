package com.cicaic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;

public class FinalFileIndexer {
    private Directory index;
    private Analyzer analyzer;
    private Similarity similarity; // Add a Similarity field

    FinalFileIndexer(String indexDirectoryPath, Analyzer analyzer, Similarity similarity) {
        try {
            Path path = Paths.get(indexDirectoryPath);
            this.index = FSDirectory.open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.analyzer = analyzer;
        this.similarity = similarity; // Initialize the Similarity
    }

    public Similarity getSimilarity() {
        return this.similarity;
    }

    public Directory getIndex() {
        return this.index;
    }

    public Analyzer getAnalyzer() {
        return this.analyzer;
    }

    public void buildIndex(String filePath) {
        try {
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setSimilarity(similarity); // Set the provided Similarity
            IndexWriter iWriter = new IndexWriter(index, config);

            List<Document> documents = parseDocuments(filePath);
            for (Document document : documents) {
                iWriter.addDocument(document);
            }
            iWriter.close();
            System.out.println("Indexing complete.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Document> parseDocuments(String filePath) {
        List<Document> documents = new ArrayList<>();
        int id = -1;
        String title = "";
        String author = "";
        String text = "";
        String currentAttr = "";


        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Define the field types with term vectors and tokenization options
            FieldType idFieldType = new FieldType(TextField.TYPE_STORED);
            idFieldType.setStored(true);

            FieldType textFieldType = new FieldType(TextField.TYPE_STORED);
            textFieldType.setStored(true);
            textFieldType.setTokenized(true);
            textFieldType.setStoreTermVectors(true);
            textFieldType.setStoreTermVectorPositions(true);
            textFieldType.setStoreTermVectorOffsets(true);
            textFieldType.setStoreTermVectorPayloads(true);
            
            String line  = reader.readLine();
            while (line != null) {
                if (isAttributeLine(line)) {
                    // if all attributes set, then a full document has been parsed
                    if (id != -1 && !title.equals("") && !author.equals("") && !text.equals("")) {
                        Document doc = new Document();
                        doc.add(new Field("id", String.valueOf(id), idFieldType));
                        doc.add(new Field("author", author, textFieldType));
                        doc.add(new Field("title", title, textFieldType));
                        doc.add(new Field("text", text, textFieldType));
                        documents.add(doc);

                        id = -1;
                        title = "";
                        author = "";
                        text = "";
                    }
                    if (!isValidAttribute(line)){
                        // find next attribute line
                        currentAttr = "invalid";
                    } else {
                        if (line.startsWith(".I")) {
                            id = Integer.parseInt(line.split(" ")[1]);
                        } else if (line.startsWith(".T")) {
                            currentAttr = "title";
                        } else if (line.startsWith(".A")) {
                            currentAttr = "auth";
                        } else if (line.startsWith(".W")) {
                            currentAttr = "text";
                        }
                    }
                } else {
                    // "content" line
                    if (currentAttr.equals("title")) {  
                        title += line + " ";
                    } else if (currentAttr.equals("auth")) {
                        author += line + " ";
                    } else if (currentAttr.equals("text")) {
                        text += line + " ";
                    }
                }
                 line  = reader.readLine();
            }
            // if all attributes set, add last document
            if (id != -1 && !title.equals("") && !author.equals("") && !text.equals("")) {
                Document doc = new Document();
                doc.add(new Field("id", String.valueOf(id), idFieldType));
                doc.add(new Field("author", author, textFieldType));
                doc.add(new Field("title", title, textFieldType));
                doc.add(new Field("text", text, textFieldType));
                documents.add(doc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return documents;
    }

    private static boolean isAttributeLine(String line) {
        String regex = "^\\.[A-Z]";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        return matcher.find();
    }

    private static boolean isValidAttribute(String line) {
        return ((line.startsWith(".I")) || (line.startsWith(".T")) 
        || (line.startsWith(".A")) || (line.startsWith(".W")));
    }

    public static boolean hasDuplicates(List<Document> documents, String fieldName) {
        // Create a set to store the values of the specified field
        Set<String> uniqueValues = new HashSet<>();

        for (Document document : documents) {
            // Retrieve the field value from the document
            IndexableField field = document.getField(fieldName);

            if (field != null) {
                String value = field.stringValue();

                // If the value is already in the set, it's a duplicate
                if (uniqueValues.contains(value)) {
                    return true;
                }

                // Otherwise, add it to the set
                uniqueValues.add(value);
            }
        }

        // No duplicates found
        return false;
    }

    public static void main(String[] args) {
        // // Example usage
        // List<Document> documents = parseDocuments(ResourcePaths.CRAN_ALL_1400.value());
        // String fieldName = "id"; 

        // if (hasDuplicates(documents, fieldName)) {
        //     System.out.println("Duplicates found in the list of documents.");
        // } else {
        //     System.out.println("No duplicates found in the list of documents.");
        // }
    }
}
