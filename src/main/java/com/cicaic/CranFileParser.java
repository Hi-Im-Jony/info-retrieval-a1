package com.cicaic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;

public class CranFileParser {
    public static List<Document> parseFile(String filePath) {
        List<Document> documents = new ArrayList<>();
        int id = -1;
        String title = "";
        String author = "";
        String text = "";
        String currentAttr = "";


        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line  = reader.readLine();
            while (line != null) {
                if (isAttributeLine(line)) {
                    // if all attributes set, then a full document has been parsed
                    if (id != -1 && !title.equals("") && !author.equals("") && !text.equals("")) {
                            Document doc = new Document();
                            doc.add(new TextField("id", String.valueOf(id), TextField.Store.YES));
                            doc.add(new TextField("title", title, TextField.Store.YES));
                            doc.add(new TextField("author", author, TextField.Store.YES));
                            doc.add(new TextField("text", text, TextField.Store.YES));
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
                    doc.add(new TextField("id", String.valueOf(id), TextField.Store.YES));
                    doc.add(new TextField("title", title, TextField.Store.YES));
                    doc.add(new TextField("author", author, TextField.Store.YES));
                    doc.add(new TextField("text", text, TextField.Store.YES));
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

    public static void main(String[] args) {
        List<Document> parsedDocuments = parseFile(ResourcePaths.CRAN_ALL_1400.value());

        for (Document doc : parsedDocuments) {
            System.out.println(doc.toString());
        }
    }
}
