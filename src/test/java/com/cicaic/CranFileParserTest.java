package com.cicaic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CranFileParserTest {
    @Test
    public void testParseFile() {
        assertEquals(1347, CranFileParser.parseFile(ResourcePaths.CRAN_ALL_1400.value()).size(), 
        "Expected 1400 documents parsed");
    }

}
