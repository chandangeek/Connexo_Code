package com.elster.jupiter.yellowfin;

public interface YellowfinService {
    String COMPONENTNAME = "YFN";

    String getYellowfinUrl();

    String login(String username);
    boolean logout(String username);

    boolean importContent(String filePath);

}
