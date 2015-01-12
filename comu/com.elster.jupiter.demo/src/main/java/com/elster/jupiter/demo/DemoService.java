package com.elster.jupiter.demo;

public interface DemoService {
    void createDemoData(String comServerName, String host);
    void createAppServer();
    void createUsers();
    void createValidationRules();
    void createCollectRemoteDataSetup(String comServerName, String host);
}
