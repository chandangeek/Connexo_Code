/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Dxp.privileges.Users
 *
 * Class that defines privileges for DataExport
 */
Ext.define('Dxp.privileges.DataExport', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.administrate.dataExportTask',
        'privilege.view.dataExportTask',
        'privilege.update.dataExportTask',
        'privilege.update.schedule.dataExportTask',
        'privilege.run.dataExportTask'],
    run:'privilege.run.dataExportTask',
    update: ['privilege.update.dataExportTask', 'privilege.update.schedule.dataExportTask'],
    updateSchedule: 'privilege.update.schedule.dataExportTask',
    admin : ['privilege.administrate.dataExportTask'],

    all: function() {
        return Ext.Array.merge(Dxp.privileges.DataExport.view);
    },
    canRun:function(){
        return Uni.Auth.checkPrivileges(Dxp.privileges.DataExport.run);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Dxp.privileges.DataExport.view);
    },
    canUpdateFull:function(){
        return Uni.Auth.checkPrivileges('privilege.update.dataExportTask');
    },
    canUpdateSchedule:function(){
        return Uni.Auth.checkPrivileges(Dxp.privileges.DataExport.updateSchedule);
    }
});
