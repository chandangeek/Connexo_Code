/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface YellowfinService {
    String COMPONENTNAME = "YFN";

    String getYellowfinUrl();

    Optional<String> getUser(String username);

    Optional<String> createUser(String userName, String email);
    Optional<String> createUser(String username);

    Optional<String> login(String username);
    Optional<String> login(String username, String email);

    Optional<Boolean> logout(String username);

    boolean importContent(String filePath);

    Optional<List<YellowfinReportInfo>> getUserReports(String userName, String category, String subCategory,String reportUUId);
    Optional<List<YellowfinFilterInfo>> getReportFilters(int reportId);
    Optional<List<YellowfinFilterListItemInfo>> getFilterListItems(String filterId,int reportId);

}
