package com.energyict.protocolimpl.edf.messages.usecases;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.Folder;

import java.sql.SQLException;

/*
 * Steps:
 * 
 * (1) Importer read Meter Event Data file
 * (2) Importer sends message DiscoverMeters ( == Service04 ) 
 * (3) Protocol handles message with getMeterList()
 * (4) Protocol Updates RTUs that come back from getMeterList()
 * 
 */

public class Service04 extends AbstractFolderAction {
    
    public void execute(Folder folder) throws SQLException, BusinessException {

        /* easy: do nothing */

        
    }
    
    public String getVersion() {
        return " $ Revision: 1 $ ";
    }
    
}
