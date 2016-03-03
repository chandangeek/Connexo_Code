Ext.define('Mdc.privileges.UsagePoint', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,

    view: ['privilege.view.anyUsagePoint', 'privilege.view.ownUsagePoint', 'privilege.administer.ownUsagePoint', 'privilege.administer.anyUsagePoint'],
    admin: ['privilege.administer.ownUsagePoint', 'privilege.administer.anyUsagePoint'],
    all: function () {
        return Ext.Array.merge(Mdc.privileges.DataCollectionKpi.view, Mdc.privileges.DataCollectionKpi.admin);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.DataCollectionKpi.view);
    },

    canViewWithInsight: function () {
        return !(this.checkApp('INS') && Uni.Auth.checkPrivileges(Mdc.privileges.DataCollectionKpi.view));
    },
    canAdmin: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.DataCollectionKpi.admin);
    },
    canAdminWithInsight: function () {
        return !(this.checkApp('INS') && Uni.Auth.checkPrivileges(Mdc.privileges.DataCollectionKpi.admin));
    },

    checkApp: function(app){
        var status = false;
        Ext.Ajax.request({
            url: '/api/apps/apps/status/'+ app,
            method: 'GET',
            async: false,
            success: function (response) {
                var data = Ext.JSON.decode(response.responseText);
                status = data.status == 'ACTIVE';
            }
        });
        return status;
    }
});
