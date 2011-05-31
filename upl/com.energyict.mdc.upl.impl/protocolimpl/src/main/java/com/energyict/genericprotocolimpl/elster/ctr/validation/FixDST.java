package com.energyict.genericprotocolimpl.elster.ctr.validation;

import com.energyict.interval.IntervalRecord;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Group;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 16/03/11
 * Time: 12:20
 */
public class FixDST {

    public static void main(String[] args) {
        Group group = ValidationUtils.getMeteringWarehouse().getGroupFactory().findByExternalName("DST");
        List<Channel> allChannels = getAllChannels(group);
        int size = allChannels.size();
        for (int i = 0; i < size; i++) {
            Channel channel = allChannels.get(i);
            System.out.println("[" + i + "/" + size + "] " + channel);
            List<IntervalRecord> intervalData = channel.getIntervalData(new Date(System.currentTimeMillis() - (3600000 * 24 * 5)), new Date());
            for (IntervalRecord record : intervalData) {
                Date date = record.getDate();
                if (date.toString().contains("05:00:00")) {
                    Date newDate = new Date(date.getTime() + 3600000);
                    try {

/*
                        channel.add(newDate, record.getCode(), (BigDecimal) record.getValue(), record.getIntervalState(), false);
*/

/*
                        Date from = new Date(date.getTime() - 1);
                        Date to = new Date(date.getTime() + 1);
                        channel.removeAll(from, to);
*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }


    }

    private static List<Channel> getAllChannels(Group group) {
        List members = group.getMembers();
        List<Channel> channels = new ArrayList<Channel>();
        for (Object member : members) {
            if (member instanceof Channel) {
                channels.add((Channel) member);
            }
        }
        return channels;
    }

}
