package com.cicaic;

public enum ResourcePaths {
    INDEX_DIR("resources/index"),
    CRAN_DIR("resources/cran"),
    CRAN_ALL_1400("resources/cran/cran.all.1400"),
    CRANQREL("resources/cran/cranqrel"),
    CRAN_QRY("resources/cran/cran.qry"),
    RESULTS("resources/results/results.txt");

    private final String path;

    ResourcePaths(String path) {
        this.path = path;
    }

    public String value() {
        return path;
    }
}
