/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.install.reports;

import com.hof.mi.web.service.AdministrationServiceRequest;
import com.hof.mi.web.service.AdministrationServiceResponse;
import com.hof.mi.web.service.AdministrationServiceService;
import com.hof.mi.web.service.AdministrationServiceServiceLocator;
import com.hof.mi.web.service.AdministrationServiceSoapBindingStub;
import com.hof.mi.web.service.ContentResource;
import com.hof.mi.web.service.ImportOption;
import com.hof.util.Base64;
import org.apache.axis.AxisFault;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenReports {

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Incorrect syntax. The following parameters are required:");
            System.out.println("path -- path to the reports xml file to import");
            System.out.println("url -- url to the Connexo Facts installation");
            System.out.println("user -- a Conenxo Facts user with administrative privileges");
            System.out.println("password -- password for the provided user");
            return;
        }

        boolean useSecureConnection = false;
        File f = new File(args[0]);
        if (f.exists() && !f.isDirectory()) {
            System.out.println("Connecting at " + args[1]);
            System.out.println("User: " + args[2] + "; Password: " + args[3]);

            Pattern pattern = Pattern.compile("(https?://)([^:^/]*):?([0-9]\\d*)?(.*)?");
            Matcher matcher = pattern.matcher(args[1]);
            matcher.find();
            if (matcher.matches()) {
                String host = matcher.group(2);
                int port = Integer.parseInt(matcher.group(3));
                String root = matcher.group(4);

                if (args[1].startsWith("https")) {
                    useSecureConnection = true;
                }

                if (importOrUpgradeContent(args[0], host, port, root, args[2], args[3], useSecureConnection)) {
                    System.out.println("Content imported successfully in Connexo Facts.");
                } else {
                    System.out.println("Error importing content in Connexo Facts.");
                }
            } else {
                System.out.println("The specified url is invalid.");
            }
        } else {
            System.out.println("The specified path is invalid.");
        }
    }

    private static boolean importOrUpgradeContent(String filePath, String host, int port, String root, String user, String password, boolean useSecureConnection) {
        Map<String, String> existingItems = null;
        List<String> itemsToImport = null;

        try {
            existingItems = getExportContentWithRetry(host, port, root, user, password, useSecureConnection);
            itemsToImport = getItemsToImport(filePath, host, port, root, user, password, useSecureConnection);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return importContent(filePath, existingItems, itemsToImport, host, port, root, user, password, useSecureConnection);
    }

    private static Map<String, String> getExportContentWithRetry(String host, int port, String root, String user, String password, boolean useSecureConnection) {
        Map<String, String> existingItems = null;
        int maxSteps = 12;
        int timeout = 5 * 1000;

        while ((maxSteps != 0) && (existingItems == null)) {
            try {
                maxSteps--;
                existingItems = getExportContent(host, port, root, user, password, useSecureConnection);
            } catch(RuntimeException e) {
                if( e.getCause() != null && e.getCause() instanceof AxisFault &&
                        ( ( e.getCause().getCause() != null && e.getCause().getCause() instanceof java.net.ConnectException) ||
                          ( ((AxisFault) e.getCause()).getFaultReason().startsWith("(404)")) )) {
                    try {
                        Thread.sleep(timeout);
                        if(maxSteps == 0) {
                            e.printStackTrace();
                            throw e;
                        }
                    } catch (InterruptedException exInterrupt) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    e.printStackTrace();
                    throw e;
                }
            }
        }

        return existingItems;
    }

    private static Map<String, String> getExportContent(String host, int port, String root, String user, String password, boolean useSecureConnection) {
        String value;
        Map<String, String> existingItems = new HashMap<>();

        AdministrationServiceResponse rs = null;
        AdministrationServiceRequest rsr = new AdministrationServiceRequest();
        AdministrationServiceService ts = new AdministrationServiceServiceLocator(host, port, root + "/services/AdministrationService", useSecureConnection);
        AdministrationServiceSoapBindingStub rssbs = null;
        try {
            rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }

        rsr.setLoginId(user);
        rsr.setPassword(password);
        rsr.setOrgId(1);
        rsr.setFunction("GETCONTENT");

        if (rssbs != null) {
            try {
                rs = rssbs.remoteAdministrationCall(rsr);
                if (rs != null) {
                    if ("SUCCESS".equals(rs.getStatusCode())) {
                        ContentResource[] resources = rs.getContentResources();
                        for (ContentResource resource : resources) {
                            if (resource.getResourceType().equals("VIEW") || resource.getResourceType()
                                    .equals("REPORT") || resource.getResourceType().equals("DATASOURCE")) {
                                value = resource.getResourceId().toString();
                            } else if (resource.getResourceType().equals("RPTCATEGORY") || resource.getResourceType()
                                    .equals("RPTSUBCATEGORY")) {
                                value = resource.getResourceCode();
                            } else {
                                value = resource.getResourceUUID();
                            }
                            existingItems.put(resource.getResourceType() + ":" + resource.getResourceName(), value);
                        }
                    }
                }

            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

        return existingItems;
    }

    private static List<String> getItemsToImport(String filePath, String host, int port, String root, String user, String password, boolean useSecureConnection) {
        List<String> itemsToImport = new ArrayList<>();
        File file = new File(filePath);
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            inputStream.read(data);

            AdministrationServiceResponse rs = null;
            AdministrationServiceRequest rsr = new AdministrationServiceRequest();
            AdministrationServiceService ts = new AdministrationServiceServiceLocator(host, port, root + "/services/AdministrationService", useSecureConnection);
            AdministrationServiceSoapBindingStub rssbs = null;
            try {
                rssbs = (AdministrationServiceSoapBindingStub) ts.getAdministrationService();
            } catch (ServiceException e) {
                throw new RuntimeException(e);
            }

            rsr.setLoginId(user);
            rsr.setPassword(password);
            rsr.setOrgId(1);
            rsr.setFunction("GETIMPORTCONTENT");

            rsr.setParameters(new String[]{Base64.encodeBytes(data)});

            if (rssbs != null) {
                try {
                    rs = rssbs.remoteAdministrationCall(rsr);
                    if (rs != null) {
                        if ("SUCCESS".equals(rs.getStatusCode())) {
                            ContentResource[] resources = rs.getContentResources();
                            for (ContentResource resource : resources) {
                                itemsToImport.add(resource.getResourceType() + ":" + resource.getResourceName());
                            }
                        }
                    }

                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return itemsToImport;
    }

    private static boolean importContent(String filePath, Map<String, String> existingItems, List<String> itemsToImport, String host, int port, String root, String user, String password, boolean useSecureConnection) {
        File file = new File(filePath);
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
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
            rsr.setOrgId(1);
            rsr.setFunction("IMPORTCONTENT");

            int index = 0;
            List<ImportOption> importOptions = new ArrayList<>();
            for (String item : itemsToImport) {
                ImportOption option = new ImportOption();
                option.setItemIndex(index);
                option.setOptionKey("OPTION");
                if (existingItems.containsKey(item)) {
                    option.setOptionValue("REPLACE");

                    ImportOption optionReplace = new ImportOption();
                    optionReplace.setItemIndex(index);
                    optionReplace.setOptionKey("EXISTING");
                    optionReplace.setOptionValue(existingItems.get(item));
                    importOptions.add(optionReplace);
                } else {
                    option.setOptionValue("ADD");
                }
                importOptions.add(option);
                index++;
            }

            ImportOption[] options = new ImportOption[importOptions.size()];
            rsr.setImportOptions(importOptions.toArray(options));
            rsr.setParameters(new String[]{Base64.encodeBytes(data)});

            if (rssbs != null) {
                try {
                    rs = rssbs.remoteAdministrationCall(rsr);
                    if (rs != null) {
                        if ("SUCCESS".equals(rs.getStatusCode())) {
                            return true;
                        } else {
                            System.out.println("Error importing file - " + rs.getStatusCode());
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
        return false;
    }

}