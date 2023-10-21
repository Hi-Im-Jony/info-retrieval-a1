package com.cicaic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Document {
    private int id;
    private String title;
    private String author;
    private String content;

    public Document(int id, String title, String author, String content) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.content = content;
    }
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getText() {
        return content;
    }
    @Override
    public String toString() {
        return "Document ID: " + id + "\nTitle: " + title + "\nAuthor: " + author + "\nText:\n" + content;
    }
}

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
                            documents.add(new Document(id, title, author, text));
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
                    documents.add(new Document(id, title, author, text));
                    id = -1;
                    title = "";
                    author = "";
                    text = "";
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
