/*
 * DataAccessResult.java
 *
 * Created on 5 december 2007, 9:18
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import java.util.*;

/**
 *
 * @author kvds
 */
public class DataAccessResult {
    
    static List list = new ArrayList();
    static {
        list.add(new DataAccessResult(16,"No_long_get_in_progress"));
        list.add(new DataAccessResult(17,"Long_set_aborted"));
        list.add(new DataAccessResult(18,"No_long_set_in_progress"));
        list.add(new DataAccessResult(19,"System_err_transmit"));
        list.add(new DataAccessResult(20,"System_err_receive"));
        list.add(new DataAccessResult(30,"Plcc_momently_unavailable"));
        list.add(new DataAccessResult(129,"Obiscode_file_not_found"));
        list.add(new DataAccessResult(130,"Object_not_initialized"));
        list.add(new DataAccessResult(131,"Get_with_list_not_implemented"));
        list.add(new DataAccessResult(132,"Connection_socket_app_impossible"));
        list.add(new DataAccessResult(133,"Write_socket_app_error"));
        list.add(new DataAccessResult(134,"Read_socket_app_error"));
        list.add(new DataAccessResult(135,"Last_data_bloc_error"));
        list.add(new DataAccessResult(136,"Get_response_error"));
        list.add(new DataAccessResult(137,"Other_set_not_implemented"));
        list.add(new DataAccessResult(138,"Other_action_not_implemented"));
        list.add(new DataAccessResult(139,"Connection_socket_CPL_impossible"));
        list.add(new DataAccessResult(140,"Write_socket_CPL_error"));
        list.add(new DataAccessResult(141,"Read_socket_CPL_error"));
        list.add(new DataAccessResult(142,"CPL_error"));
        list.add(new DataAccessResult(143,"Create_directory_error"));
        list.add(new DataAccessResult(144,"Change_directory_error"));
        list.add(new DataAccessResult(145,"Create_file_error"));
        list.add(new DataAccessResult(146,"Read_file_error"));
        list.add(new DataAccessResult(147,"Task_schedule_decode_error"));
        list.add(new DataAccessResult(148,"Task_schedule_datetime_error"));
        list.add(new DataAccessResult(149,"Script_table_decode_error"));
        list.add(new DataAccessResult(150,"Test_date_range_descriptor_error"));
        list.add(new DataAccessResult(151,"Connection_socket_fictive_impossible"));
        list.add(new DataAccessResult(152,"Write_socket_fictive_error"));
        list.add(new DataAccessResult(153,"Read_socket_fictive_error"));
        list.add(new DataAccessResult(154,"Fictive_error"));
        list.add(new DataAccessResult(155,"Connection_socket_logdev_error"));
        list.add(new DataAccessResult(156,"Write_socket_logdev_error"));
        list.add(new DataAccessResult(157,"Read_socket_logdev_error"));
        list.add(new DataAccessResult(158,"Buffer_CPL_Too_Long"));
        list.add(new DataAccessResult(250,"Other-reason"));        
    }
    
    private int id;
    private String description;
    
    /** Creates a new instance of DataAccessResult */
    private DataAccessResult(int id, String description) {
        this.id=id;
        this.description=description;
    }
    
    public String evalDataAccessResult(int val) {
        String strErr;
        switch(val) {
            case 1: strErr = "Hardware fault";break;
            case 2: strErr = "Temporary fauilure";break;
            case 3: strErr = "R/W denied";break;
            case 4: strErr = "Object undefined";break;
            case 9: strErr = "Object class inconsistent";break;
            case 11: strErr = "Object unavailable";break;
            case 12: strErr = "Type unmatched";break;
            case 13: strErr = "Scope of access violated";break;
            case 14: strErr = "Data block unavailable";break;
            default: strErr = "Unknown data-access-result code "+val;break;
        }
        return strErr;
        
    } // private void evalDataAccessResult(int val) throws IOException      
    
    public String toString() {
        if ((id > 200) && (id < 250))
            return "DataAccessResult code "+id+", "+evalDataAccessResult(id-200);
        else
            return "DataAccessResult code "+id+", "+description;
            
    }
    
    public boolean isCPLError() {
        return getId()==142;    
    }
    
    static public DataAccessResult findDataAccessResult(int id) {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            DataAccessResult o = (DataAccessResult)it.next();
            if (o.getId()==id)
                return o;
        }
        return new DataAccessResult(id,"Unknown data-access-result code "+id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        if ((id > 200) && (id < 250))
            return evalDataAccessResult(id-200);
        else
            return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
}
