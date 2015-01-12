package com.elster.jupiter.demo;

public interface DemoService {
    void createDemoData(String comServerName, String host);
    void createAppServer(String appServerName);
    void createUsers();
    void createValidationRules();
    void createCollectRemoteDataSetup(String comServerName, String host);
}
