package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.util.ArrayList;
import java.util.List;

public class CertificateRevocationResultInfo {
    public long totalCount;
    public long revokedCount;
    public long withErrorsCount;
    public long withUsagesCount;
    public long withWrongStatusCount;

    public List<IdWithNameInfo> revoked = new ArrayList<>();
    public List<IdWithNameInfo> withErrors = new ArrayList<>();
    public List<IdWithNameInfo> withUsages = new ArrayList<>();
    public List<IdWithNameInfo> withWrongStatus = new ArrayList<>();

    public void updateCounters(long total) {
        totalCount = total;
        revokedCount = revoked.size();
        withErrorsCount = withErrors.size();
        withUsagesCount = withUsages.size();
        withWrongStatusCount = withWrongStatus.size();
    }

    public void addRevoked(CertificateWrapper certificate) {
        revoked.add(new IdWithNameInfo(certificate.getId(), certificate.getAlias()));
    }

    public void addWithError(CertificateWrapper certificate) {
        withErrors.add(new IdWithNameInfo(certificate.getId(), certificate.getAlias()));
    }

    public void addWithUsages(CertificateWrapper certificate) {
        withUsages.add(new IdWithNameInfo(certificate.getId(), certificate.getAlias()));
    }

    public void addWithWrongStatus(CertificateWrapper certificate) {
        withWrongStatus.add(new IdWithNameInfo(certificate.getId(), certificate.getAlias()));
    }
}
