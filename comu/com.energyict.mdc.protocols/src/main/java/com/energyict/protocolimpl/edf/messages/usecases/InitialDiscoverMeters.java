package com.energyict.protocolimpl.edf.messages.usecases;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.Folder;
import com.energyict.protocolimpl.edf.messages.MessageDiscoverMeters;

import java.sql.SQLException;
import java.util.Iterator;

public class InitialDiscoverMeters extends AbstractFolderAction {

    public void execute(Folder folder) throws SQLException, BusinessException {


        try {

            Iterator i = folder.getRtus().iterator();
            while( i.hasNext() ) {

                Device rtu = (Device)i.next();

                MessageDiscoverMeters mr = new MessageDiscoverMeters();
                mr.setOrdinal(0);
                mr.setScriptId(MessageDiscoverMeters.RESETANDREDISCOVER);
                createMessage( rtu, mr);

            }

        } catch( Exception ex ) {

            ex.printStackTrace();
            throw new BusinessException( ex );

        }


    }

    public String getVersion() {
        return " $ Revision: 1 $ ";
    }

}
