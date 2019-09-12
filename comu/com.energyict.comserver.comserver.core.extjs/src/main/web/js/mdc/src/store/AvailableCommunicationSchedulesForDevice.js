/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.AvailableCommunicationSchedulesForDevice',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CommunicationSchedule',
        'Uni.Auth'
    ],
    model: 'Mdc.model.CommunicationSchedule',
    storeId: 'AvailableCommunicationSchedulesForDevice',
    remoteSort: true,
    filters: [
    	function(record){
    		var i,
    			comTaskUsages = record.get('comTaskUsages');
    		for(i = 0; i < comTaskUsages.length; i++){
    			if(!Uni.Auth.hasAnyPrivilege(comTaskUsages[i].privileges)){
    				return false;
    			}
    		}
    		return true;
    	}
    ],
    proxy: {
        type: 'rest',
        url: '../../api/scr/schedules',
        reader: {
            type: 'json',
            root: 'schedules'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
