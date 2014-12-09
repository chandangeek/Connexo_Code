package com.elster.jupiter.yellowfin;

import com.elster.jupiter.yellowfin.impl.YellowfinReportInfoImpl;
import com.hof.mi.web.service.ReportRow;

import java.util.List;

public interface YellowfinService {
    String COMPONENTNAME = "YFN";

    String getYellowfinUrl();

    String login(String username);
    boolean logout(String username);

    boolean importContent(String filePath);

    List<YellowfinReportInfo> getUserReports(String userName, String category, String subCategory,String reportUUId);
    List<YellowfinFilterInfo> getReportFilters(int reportId);
    List<YellowfinFilterListItemInfo> getFilterListItems(String filterId,int reportId);

}
