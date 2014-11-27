package com.elster.jupiter.yellowfin.impl;

//import com.elster.jupiter.http.whiteboard.App;

import com.elster.jupiter.yellowfin.YellowfinService;
import com.hof.mi.web.service.*;
import com.hof.util.Base64;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

@Component(name = "com.elster.jupiter.yellowfin", service = {YellowfinService.class}, immediate = true, property = "name=" + YellowfinService.COMPONENTNAME)
public class YellowfinServiceImpl implements YellowfinService {
    private static final String YELLOWFIN_URL = "com.elster.jupiter.yellowfin.url";
    private static final String YELLOWFIN_WEBSERVICES_USER = "com.elster.jupiter.yellowfin.user";
    private static final String YELLOWFIN_WEBSERVICES_PASSWORD = "com.elster.jupiter.yellowfin.password";
    private static final String DEFAULT_YELLOWFIN_URL = "http://localhost:8081";

    private String yellowfinHost;
    private int yellowfinPort;
    private String yellowfinUrl;
    private String yellowfinWebServiceUser;
    private String yellowfinWebServicePassword;

    
    @Override
    public String getYellowfinUrl(){
        return yellowfinUrl;
    }

    @Override
    public boolean importContent(String filePath) {;
    	return false;
    }

    @Override
    public String login(String username)  {
        return null;
    }



    @Override
    public boolean logout(String username)  {
        return false;
    }

}
