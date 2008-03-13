package com.energyict.echelon;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Transaction;
import com.energyict.mdw.core.*;

import java.sql.SQLException;
import java.util.*;

/**
 * Recursively delete subfolders and rtu's.  This will only work for a 
 * typical Echelon structure, but that is the intention.
 * 
 * @author fbo
 */

public class EmptyFolderAction implements FolderAction, Transaction {

    private Folder folder;
    
    public void execute(Folder folder) throws SQLException, BusinessException {
        this.folder = folder;
        doExecute();
    }
    
    public Object doExecute() throws BusinessException, SQLException {

        Iterator i = folder.getSubFolders().iterator();
        while (i.hasNext()) {
            Folder sub = (Folder) i.next();
            empty( sub );
            sub.delete();
        }
        
        return null;
        
    }
    
    private void empty(Folder folder) throws BusinessException, SQLException{
        Iterator i = folder.getRtus().iterator();
        while( i.hasNext() ){
            Rtu rtu = (Rtu)i.next();
            rtu.delete();
        }
        i = folder.getSubFolders().iterator();
        while( i.hasNext() ){
            Folder sub = (Folder)i.next();
            empty(sub);
            sub.delete();
        }
    }

    public boolean isEnabled(Folder folder) {
        return folder.getSubFolders().size() > 0;
    }

    public void addProperties(Properties properties) { }

    public List getOptionalKeys() {
        return new ArrayList();
    }

    public List getRequiredKeys() {
        return new ArrayList();
    }

    public String getVersion() {
        return "$Revision: 1.1 $";      
    }

}
