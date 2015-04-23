/**
 * @class Sam.privileges.DataPurge
 *
 * Class that defines privileges for DataPurge
 */
Ext.define('Sam.privileges.DataPurge', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.administrate.dataPurge','privilege.view.dataPurge'],
    admin: ['privilege.administrate.dataPurge'],
    all: function() {
        return Ext.Array.merge(Sam.privileges.DataPurge.view);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Sam.privileges.DataPurge.view);
    }
});
