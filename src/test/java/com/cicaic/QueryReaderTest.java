package com.cicaic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class QueryReaderTest {

    private final static int TOTAL_UNIQUE_QUERIES = 225;
    @Test
    public void testReadQueriesFromFile() {
        List<QueryReader.QueryData> queries = QueryReader.readQueriesFromFile(ResourcePaths.CRAN_QRY.value());
        assertEquals(TOTAL_UNIQUE_QUERIES, queries.size());

        // Verify the contents of first query
        assertEquals("001", queries.get(0).queryId);
        assertEquals("what similarity laws must be obeyed when constructing aeroelastic models of heated high speed aircraft .", 
                     queries.get(0).queryText);
        // Verify contents of last query
        assertEquals("365", queries.get(224).queryId);
        assertEquals("what design factors can be used to control lift-drag ratios at mach numbers above 5 .", 
                     queries.get(224).queryText);
    }
}
