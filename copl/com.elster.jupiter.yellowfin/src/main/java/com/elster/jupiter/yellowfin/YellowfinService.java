package com.elster.jupiter.yellowfin;

import com.elster.jupiter.yellowfin.impl.YellowfinReportInfoImpl;

import java.util.List;

public interface YellowfinService {
    String COMPONENTNAME = "YFN";

    String getYellowfinUrl();

    String login(String username);
    boolean logout(String username);

    boolean importContent(String filePath);

    List<YellowfinReportInfo> getUserReports(String userName, String category, String subCategory);

}
