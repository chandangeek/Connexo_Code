package com.elster.jupiter.demo;

public interface DemoService {
    void createDemoData(String comServerName, String host);
    void createApplicationServer(String appServerName);
    void createUserManagement();
    void createDeliverDataSetup();
    void createIssues();
    void createA3Device();
    void createValidationSetup();
    void createCollectRemoteDataSetup(String comServerName, String host);

    void createMockedDataDevice(String serialNumber);

    void createValidationDevice(String serialNumber);
}
