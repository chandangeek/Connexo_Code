package com.energyict.genericprotocolimpl.elster.ctr.validation;

import com.energyict.cbo.BusinessException;
import com.energyict.interval.IntervalRecord;
import com.energyict.mdw.core.*;
import com.energyict.mdw.relation.RelationAttributeType;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 1/03/11
 * Time: 14:50
 */
public class ValidationUtils {

    private static MeteringWarehouse meteringWarehouse = null;
    private static RelationAttributeType installationDateRelationType;
    private static Date now = null;
    private static Logger logger;
    private static Map<Rtu, Date> installationDates = new HashMap<Rtu, Date>();

    public static final String DATE_FORMAT = "dd-MM-yyyy HH:mm";

    public static String formatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        return format.format(date);
    }

    public static Date getInstallationDate(Rtu rtu, Date afterInstallationDate) throws BusinessException {
        if (!installationDates.containsKey(rtu)) {
            Folder folder = rtu.getFolder();
            Object relationObject = folder.get(getInstallationDateRelationType());
            Date installationDate = null;
            if (relationObject instanceof Date) {
                installationDate = (Date) relationObject;
            } else {
                throw new BusinessException("Unable to get the installation date for rtu [" + rtu + "]");
            }
            if ((afterInstallationDate != null) && (afterInstallationDate.after(installationDate))) {
                installationDates.put(rtu, afterInstallationDate);
            } else {
                installationDates.put(rtu, installationDate);
            }
        }
        return installationDates.get(rtu);
    }

    static Date getInstallationDate(Channel channel, Date afterInstallationDate) throws BusinessException {
        return getInstallationDate(channel.getRtu(), afterInstallationDate);
    }

    static RelationAttributeType getInstallationDateRelationType() {
        if (installationDateRelationType == null) {
            List<RelationAttributeType> types = getMeteringWarehouse().getRelationAttributeTypeFactory().findByName("installationdate");
            for (RelationAttributeType type : types) {
                if (type.toString().equalsIgnoreCase("foldertype.mtu155device.physicaldevice.installationdate")) {
                    installationDateRelationType = type;
                    break;
                }
            }
        }
        return installationDateRelationType;
    }

    public static MeteringWarehouse getMeteringWarehouse() {
        if (meteringWarehouse == null) {
            MeteringWarehouse.createBatchContext(true);
            meteringWarehouse = MeteringWarehouse.getCurrent();
        }
        return meteringWarehouse;
    }

    static void log(Level logLevel, String message) {
        if (logLevel.intValue() == Level.SEVERE.intValue()) {
            System.out.println(message);
            getLogger().log(logLevel, message);
        }
    }

    static Date now() {
        if (now == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 6);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            now = calendar.getTime();
        }
        return now;
    }

    public static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(ValidateProfile.class.getName());
        }
        return logger;
    }

    public static Date getFirstIntervalDate(List<IntervalRecord> intervalData, Date onlyFrom) {
        Date firstIntervalDate = null;
        for (IntervalRecord intervalRecord : intervalData) {
            Date intervalDate = intervalRecord.getDate();
            if (firstIntervalDate == null) {
                firstIntervalDate = intervalDate;
            } else if (firstIntervalDate.after(intervalDate)) {
                firstIntervalDate = intervalDate;
            }
        }
        if ((onlyFrom != null) && (firstIntervalDate != null) && (onlyFrom.after(firstIntervalDate))) {
            return onlyFrom;
        }
        return firstIntervalDate;
    }

    public static boolean isIntervalInIntervalData(List<IntervalRecord> intervalData, Date oldestDate) {
        for (IntervalRecord record : intervalData) {
            if (record.getDate().getTime() == oldestDate.getTime()) {
                return true;
            }
        }
        return false;
    }

}
