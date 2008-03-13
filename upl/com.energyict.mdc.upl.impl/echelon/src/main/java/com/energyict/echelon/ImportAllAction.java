package com.energyict.echelon;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.*;
import com.energyict.mdwswing.util.UiHelper;

import java.sql.SQLException;
import java.util.*;

/**
 * Import Meters and Concentrators
 * @author fbo
 */

public class ImportAllAction implements FolderAction {

    public void execute(Folder folder) throws SQLException, BusinessException {
        ImportPanel panel = new ImportPanel(folder, true);
        
        String title = "Import Meters/Dataconcentrators";
        UiHelper.getMainWindow().showModalDialog(panel, title);
    }

    public boolean isEnabled(Folder folder) {
        return true;
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
