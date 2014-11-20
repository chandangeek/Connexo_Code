package com.elster.jupiter.yellowfin;

public interface YellowfinService {
    String COMPONENTNAME = "YFN";

    String getYellowfinUrl();
    String login(String authentication);
    boolean logout(String username, String password, String sessionId);
}
