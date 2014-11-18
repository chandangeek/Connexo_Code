package com.elster.jupiter.yellowfin;

public interface YellowfinService {
    String COMPONENTNAME = "YFN";

    String login(String authentication);
    boolean logout(String username, String password, String sessionId);
}
