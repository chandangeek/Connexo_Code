package com.energyict.protocolimpl.edf.messages.usecases;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.impl.EnvironmentImpl;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdw.amr.*;
import com.energyict.mdw.core.*;

import java.sql.SQLException;
import java.util.*;

public class DeleteRegisterReadings extends AbstractFolderAction implements Transaction {

    private Folder folder;

    public void execute(Folder folder)
        throws SQLException, BusinessException {

        this.folder = folder;

        MeteringWarehouse.getCurrent().execute(this);
    }

    public Date lastWeek( ) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -1 );
        return calendar.getTime();
    }

    public Date lastMonth( ) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        return calendar.getTime();
    }

    public String getVersion() {
        return " $ Revision: 1 $ ";
    }

    public Object doExecute() throws BusinessException, SQLException {
        RegisterReadingFactory factory =
            MeteringWarehouse.getCurrent().getRegisterReadingFactory();


        for (Device rtu : folder.getRtus()) {
            rtu.updateLastReading(lastWeek());
            rtu.updateLastLogbook(lastMonth());

            for (Register rtuRegister : rtu.getRegisters()) {
                rtuRegister.getReadingAfterOrEqual(new Date());
                for (RegisterReading registerReading : factory.findByRegister(rtuRegister.getId())) {
                    (registerReading).delete();
                }
            }

            Iterator i = rtu.getChannels().iterator();
            while (i.hasNext()) {
                Channel channel = (Channel) i.next();
                channel.removeAll(new Date(0), new Date());

            }

            i = rtu.getEvents().iterator();
            while (i.hasNext()) {
                DeviceEvent event = (DeviceEvent) i.next();
                event.delete();
            }
        }
        return null;
    }

}