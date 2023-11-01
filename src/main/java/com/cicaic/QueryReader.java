package com.cicaic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryReader {

    public static List<QueryData> readQueriesFromFile(String filePath) {
        List<QueryData> queries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath)) ) {
            String line;
            String queryId = null;
            String queryText = "";
            Pattern id_pattern = Pattern.compile("^\\.I\\s(\\d+)$");
            int i = 1;

            while ((line = reader.readLine()) != null) {
                Matcher matcher = id_pattern.matcher(line);
                if (matcher.matches()) {
                    if (queryId != null) {
                        queries.add(new QueryData(i++, queryId, queryText));
                        queryText = "";
                    }
                    queryId = matcher.group(1);
                } else if (!line.startsWith(".W")) {
                    queryText += queryText.equals("") ? line : " " + line;
                }
            }

            // Add the last query (if any)
            if (queryId != null && !queryText.isEmpty()) {
                queries.add(new QueryData(i, queryId, queryText));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return queries;
    }

    public static class QueryData {
        public int queryNum;
        public String queryId;
        public String queryText;

        QueryData(int queryNum, String queryId, String queryText) {
            this.queryNum = queryNum;
            this.queryId = queryId;
            this.queryText = queryText;
        }
    }
}

