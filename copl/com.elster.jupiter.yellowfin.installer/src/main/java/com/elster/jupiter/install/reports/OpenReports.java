package com.elster.jupiter.install.reports;

import com.hof.mi.web.service.*;
import com.hof.util.Base64;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenReports {
    public static void main(String[] args){
        if(args.length < 4){
            System.out.println("Incorrect syntax. The following parameters are required:");
            System.out.println("path -- path to the reports xml file to import");
            System.out.println("url -- url to the Connexo Facts installation");
            System.out.println("user -- a Conenxo Facts user with administrative privileges");
            System.out.println("password -- password for the provided user");
            return;
        }

        boolean useSecureConnection = false;
        File f = new File(args[0]);
        if(f.exists() && !f.isDirectory()) {
            System.out.println("Connecting at " + args[1]);
            System.out.println("User: " + args[2] + "; Password: " + args[3]);

            Pattern pattern = Pattern.compile("(https?://)([^:^/]*):?([0-9]\\d*)?(.*)?");
            Matcher matcher = pattern.matcher(args[1]);
            matcher.find();
            if(matcher.matches()){
                String host   = matcher.group(2);
                int port   = Integer.parseInt(matcher.group(3));
                String root   = matcher.group(4);

                if(args[1].startsWith("https")){
                    useSecureConnection = true;
                }

                if(importContent(args[0], host, port, root, args[2], args[3], useSecureConnection)){
                    System.out.println("Content imported successfully in Connexo Facts.");
                }
                else{
                    System.out.println("Error importing content in Connexo Facts.");
                }
            }
            else{
                System.out.println("The specified url is invalid.");
            }
        }
        else{
            System.out.println("The specified path is invalid.");
        }
    }

    private static boolean importContent(String filePath, String host, int port, String root, String user, String password, boolean useSecureConnection) {

        FileInputStream inputStream = null;
        try {
            File file = new File(filePath);
            inputStream = new FileInputStream(file);
            byte[] data = new byte[(int)file.length()];
            inputStream.read(data);

            AdministrationServiceResponse rs = null;
            AdministrationServiceRequest rsr = new AdministrationServiceRequest();
            AdministrationServiceService ts = new AdministrationServiceServiceLocator(host, port, root + "/services/AdministrationService", useSecureConnection);
            AdministrationServiceSoapBindingStub rssbs = null;
            try {
                rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
            } catch (ServiceException e) {
                e.printStackTrace();
            }

            rsr.setLoginId(user);
            rsr.setPassword(password);
            //rsr.setPassword(null);
            rsr.setOrgId(new Integer(1));
            rsr.setFunction("IMPORTCONTENT");
            rsr.setParameters( new String[] { Base64.encodeBytes(data) } );

            if (rssbs != null) {
                try {
                    rs = rssbs.remoteAdministrationCall(rsr);
                    if (rs != null){
                        if("SUCCESS".equals(rs.getStatusCode()) ) {
                            return true;
                        }
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        return false;
    }

}
