package com.elster.jupiter.yellowfin.impl;

import com.elster.jupiter.yellowfin.YellowfinService;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.elster.jupiter.yellowfin.console", service = {YellowfinConsoleService.class}, property = {"name=" + "YFN" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=importYFN"}, immediate = true)

public class YellowfinConsoleService {

    private volatile YellowfinService yellowfinService;

    public void importYFN(String filePath){

        if (chkPath(filePath)) {
            if(yellowfinService.importContent(filePath)){
                System.out.println("Import successful!");
            }else{
                System.out.println("Error importing file! \n  Check Tomcat console!");
            }
        }else{
            System.out.println("Please enter a correct path!\n   Exemple: C:/Folder/report.xml");
        }
    }

    public void importYFN(){
        System.out.println("Please add file path!");
    }

    public boolean chkPath(String filePath){
        return filePath.contains("/");
    }

    @Reference
    public void setBpmService(YellowfinService yellowfinService) {

        this.yellowfinService = yellowfinService;
    }

}
