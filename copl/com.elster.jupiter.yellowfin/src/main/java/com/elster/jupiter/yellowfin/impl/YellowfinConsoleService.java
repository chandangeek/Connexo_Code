/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.impl;

import com.elster.jupiter.yellowfin.YellowfinService;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Component;

import java.io.File;

@Component(name = "com.elster.jupiter.yellowfin.console", service = {YellowfinConsoleService.class}, property = {"name=" + "YFN" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=importFacts"}, immediate = true)

public class YellowfinConsoleService {

    private volatile YellowfinService yellowfinService;

    public void importFacts(String filePath){

        if (fileExists(filePath)) {
            if(yellowfinService.importContent(filePath)){
                System.out.println("Import successful!");
            }else{
                System.out.println("Error importing file! \n  Check Tomcat console!");
            }
        }else{
            System.out.println("Please enter a correct path!\n   Exemple: C:/Folder/report.xml");
        }
    }

    public void importFacts(){
        System.out.println("Please add file path!");
    }

    public boolean fileExists(String filePath){
        File f = new File(filePath);
        if(f.exists() && !f.isDirectory()) {
            return true;
        }

        return false;
    }

    @Reference
    public void setYellowfinService(YellowfinService yellowfinService) {

        this.yellowfinService = yellowfinService;
    }

}
