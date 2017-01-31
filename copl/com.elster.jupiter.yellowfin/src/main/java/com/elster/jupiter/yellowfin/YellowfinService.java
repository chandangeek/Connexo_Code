/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin;

import com.elster.jupiter.yellowfin.impl.YellowfinReportInfoImpl;
import com.hof.mi.web.service.ReportRow;

import java.util.List;
import java.util.Optional;

public interface YellowfinService {
    String COMPONENTNAME = "YFN";

    String getYellowfinUrl();

    Optional<String> getUser(String username);

    Optional<String> createUser(String username);

    Optional<String> login(String username);
    Optional<Boolean> logout(String username);

    boolean importContent(String filePath);

    Optional<List<YellowfinReportInfo>> getUserReports(String userName, String category, String subCategory,String reportUUId);
    Optional<List<YellowfinFilterInfo>> getReportFilters(int reportId);
    Optional<List<YellowfinFilterListItemInfo>> getFilterListItems(String filterId,int reportId);

}
