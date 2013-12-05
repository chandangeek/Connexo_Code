package com.energyict.protocolimpl.edf.messages.usecases;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.Folder;

import java.sql.SQLException;
import java.util.Iterator;

public class ReadNow extends AbstractFolderAction {

    public void execute(Folder folder)
        throws SQLException, BusinessException {

        try {

            Iterator i = folder.getRtus().iterator();

            while( i.hasNext() ) {

                Device rtu = (Device)i.next();

//                Iterator schi = rtu.getCommunicationSchedulers().iterator();
//
//                if( schi.hasNext() ) {
//                    CommunicationScheduler cs = (CommunicationScheduler)schi.next();
//                    cs.startReadingNow();
//                }

            }

        } catch( Exception ex ){


            ex.printStackTrace();
            throw new BusinessException( ex );

        }



    }

    public String getVersion() {
        return " $ Revision: 1 $ ";
    }

}
