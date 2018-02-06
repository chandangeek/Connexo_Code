package com.elster.jupiter.pki.rest.impl;

import java.util.ArrayList;
import java.util.List;

public class CertificateRevocationInfo {
    public boolean isOnline;
    public List<Result> revocationResults;

    public void addResult(long id, String status) {
        if (revocationResults == null) {
            revocationResults = new ArrayList<>();
        }
        revocationResults.add(new Result(id, status));
    }

    public CertificateRevocationInfo(boolean isOnline) {
        this.isOnline = isOnline;
    }

    private static class Result {
        private long id;
        private String status;

        public Result(long id, String status) {
            this.id = id;
            this.status = status;
        }
    }
}
