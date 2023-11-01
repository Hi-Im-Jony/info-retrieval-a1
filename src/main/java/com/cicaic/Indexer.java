package com.cicaic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {
    private Directory index;
    private IndexWriter iWriter;
    private EnglishAnalyzer analyzer;
    private Similarity similarity; 

    Indexer(String indexDirectoryPath, Similarity similarity) throws IOException {
        try {
            Path path = Paths.get(indexDirectoryPath);
            this.index = FSDirectory.open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.similarity = similarity; 
        CharArraySet stopwords = CharArraySet.copy(EnglishAnalyzer.getDefaultStopSet());
        String[] customStopWords = {
            "must", "above", "what", "would", "should", "when", "if", "far", "above", "do", "we", "can",
            "does", "have", "over", "how", "can't", "like", "been", "did", "which", "why", "else",
            "find", "has", "any", "done", "best", "anyone", "on", "result", "number", "from", "be",
            "of", "about", "along", "being", "simple", "practical", "possible", "information",
            "pertaining", "very", "available", "details"
        };
        
        // Add your custom stop words to the set
        for (String word : customStopWords) {
            stopwords.add(word);
        }
        analyzer = new EnglishAnalyzer(stopwords);
        iWriter = getIndexWriter();
    }

    public void buildIndex(String filePath) {
        try {
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

    public void setIndexWriter(IndexWriter indexWriter) {
        this.iWriter = indexWriter;
    }
    public IndexWriter getIndexWriter() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setSimilarity(similarity);
        config.setOpenMode(OpenMode.CREATE);
        return new IndexWriter(index, config);
    }

    public Similarity getSimilarity() {
        return this.similarity;
    }

    public Directory getIndex() {
        return this.index;
    }

    public EnglishAnalyzer getAnalyzer() {
        return this.analyzer;
    }
    
    private static List<Document> parseDocuments(String filePath) {
        List<Document> documents = new ArrayList<>();

         // Define idFieldType for the "id" field
        FieldType idFieldType = new FieldType(StringField.TYPE_STORED);
        idFieldType.setTokenized(false);
        idFieldType.freeze();  

        // Define textFieldType for text fields like "author" and "text"
        FieldType textFieldType = new FieldType(TextField.TYPE_STORED);
        textFieldType.setTokenized(true);
        textFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        textFieldType.freeze();

        // Define titleFieldType for the "title" field
        FieldType titleFieldType = new FieldType(TextField.TYPE_STORED);
        titleFieldType.setTokenized(true);
        titleFieldType.setIndexOptions(IndexOptions.DOCS);  
        titleFieldType.freeze(); 

        String cranFileContents = readFile(Paths.get(ResourcePaths.CRAN_ALL_1400.value()));
        String[] unparsedDocs = cranFileContents.split(".I");
        System.out.println("File parts: " + unparsedDocs.length);
        for (int i = 1; i < unparsedDocs.length; i++) {
            HashMap<String, String> docContents = parseDocContents(unparsedDocs[i]);
            Document doc = new Document();
                doc.add(new Field("id", docContents.get(".I"), idFieldType));
                doc.add(new Field("author", docContents.get(".A"), textFieldType));
                doc.add(new Field("title", docContents.get(".T"), titleFieldType));
                doc.add(new Field("text", docContents.get(".W"), textFieldType));
                
                documents.add(doc);
        }

        System.out.println("Parsed " + documents.size() + "documents");
        return documents;
    }

    private static HashMap<String, String> parseDocContents(String doc) {
        HashMap<String, String> fields = new HashMap<>();
        char[] chars = doc.toCharArray();
        String value = "";
        String currentAttr = ".I";
        for (int i=0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '.') {
                c = chars[++i];
                // get next char
                switch (c) {
                    case 'T':
                        fields.put(currentAttr, value);
                        currentAttr = ".T";
                        break;
                    case 'A':
                        fields.put(currentAttr, value);
                        currentAttr = ".A";
                        break;
                    case 'W':
                        fields.put(currentAttr, value);
                        currentAttr = ".W";
                    case 'B':
                        fields.put(currentAttr, value);
                        currentAttr = ".B";
                        break;
                    default:
                        value += '.' + c;
                }
            } else {
                value += c;
            }
        }
        return fields;
    }

    public static String readFile(Path path) {		
		
		InputStream stream = null;
		try {
			stream = Files.newInputStream(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String line = null;
		StringBuilder fileContent = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));

		try {
			while((line = in.readLine()) != null) {
				fileContent.append(line + " ");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		return fileContent.toString();
	}
}

