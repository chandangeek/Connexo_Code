/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * OtherDataIds.java
 *
 * Created on 6 september 2005, 8:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author koen
 */
public class OtherDataIds {

    static List<RegisterDataId> list = new ArrayList<>();
    static {
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.STRING,66,-1,-1, -1, "Special ID code #1"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.STRING,67,-1,-1, -1, "Special ID code #2"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.STRING,70,-1,-1, -1, "Program ID Code"));

        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.INT,59,-1,-1, -1, "Average PF between demand resets WH Out"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.INT,52,-1,-1, -1, "Average PF between demand resets WH In"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.INT,60,-1,-1, -1, "Average PF during peak interval WH Out"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.INT,53,-1,-1, -1, "Average PF during peak interval WH In"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.INT,61,-1,-1, -1, "Average PF last 5 peak intervals WH Out"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.INT,54,-1,-1, -1, "Average PF last 5 peak intervals WH In"));

        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,306,-1,-1, -1, "Date of #2 Peak Interval WH Out"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,62,-1,-1, -1, "Time of #2 Peak Interval WH Out"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,307,-1,-1, -1, "Date of #3 Peak Interval WH Out"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,63,-1,-1, -1, "Time of #3 Peak Interval WH Out"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,308,-1,-1, -1, "Date of #4 Peak Interval WH Out"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,64,-1,-1, -1, "Time of #4 Peak Interval WH Out"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,309,-1,-1, -1, "Date of #5 Peak Interval WH Out"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,65,-1,-1, -1, "Time of #5 Peak Interval WH Out"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,310,-1,-1, -1, "Date of #2 Peak Interval WH In"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,55,-1,-1, -1, "Time of #2 Peak Interval WH In"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,311,-1,-1, -1, "Date of #3 Peak Interval WH In"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,56,-1,-1, -1, "Time of #3 Peak Interval WH In"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,312,-1,-1, -1, "Date of #4 Peak Interval WH In"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,57,-1,-1, -1, "Time of #4 Peak Interval WH In"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,313,-1,-1, -1, "Date of #5 Peak Interval WH In"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,58,-1,-1, -1, "Time of #5 Peak Interval WH In"));

        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,314,-1,-1, -1, "Date"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,0,-1,-1, -1, "Time"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.INT,1,-1,-1, -1, "Day of week"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,3,-1,-1, -1, "Date of last reset"));

        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,89,-1,-1, -1, "Cumulative time on carryover battery"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,86,-1,-1, -1, "Alarm time limit on carryover battery"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.TIME,90,-1,-1, -1, "Date battery installed"));

        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.STRING,85,-1,-1, -1, "Segment test display"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.STRING,83,-1,-1, -1, "Error alert display"));
        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.STRING,84,-1,-1, -1, "Diagnostics display"));

        list.add(new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.INT,101,-1,-1, -1, "Number of power outages count"));

    }

    public static List<RegisterDataId> getRegisterDataIds() {
        return list;
    }

}