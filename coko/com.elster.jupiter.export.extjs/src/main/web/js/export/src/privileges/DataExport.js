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
    update:['privilege.update.dataExportTask',
        'privilege.update.schedule.dataExportTask'],
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
    canUpdate:function(){
        return Uni.Auth.checkPrivileges(Dxp.privileges.DataExport.update);
    }
});
