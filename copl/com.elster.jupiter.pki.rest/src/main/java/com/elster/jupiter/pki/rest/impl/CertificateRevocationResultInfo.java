package com.elster.jupiter.pki.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CertificateRevocationResultInfo {
    public List<Result> revocationResults;

    public void addResult(String alias, boolean success, String error) {
        if (revocationResults == null) {
            revocationResults = new ArrayList<>();
        }
        revocationResults.add(new Result(alias, success, error));
    }

    public void addResult(String alias, boolean success) {
        addResult(alias, success, null);
    }

    public void replaceResult(String alias, boolean success, String error){
        Optional<Result> rs = revocationResults.stream()
                .filter(result -> result.alias.equals(alias))
                .findFirst();
        if (!rs.isPresent()){
            addResult(alias, success, error);
            return;
        }
        revocationResults.remove(rs.get());
        addResult(alias, success, error);
    }

    public static class Result {
        public String alias;
        public boolean success;
        public String error;

        public Result(String alias, boolean success, String error) {
            this.alias = alias;
            this.success = success;
            this.error = error;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Result result = (Result) o;

            return alias.equals(result.alias);
        }

        @Override
        public int hashCode() {
            return alias.hashCode();
        }
    }
}
