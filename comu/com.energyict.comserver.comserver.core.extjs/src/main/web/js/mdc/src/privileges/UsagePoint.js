Ext.define('Mdc.privileges.UsagePoint', {
    requires: [
        'Uni.Auth',
        'Uni.store.Apps'
    ],
    singleton: true,
    appsStoreLoaded: false,
    insightStatus: false,

    view: ['privilege.view.anyUsagePoint', 'privilege.view.ownUsagePoint', 'privilege.administer.ownUsagePoint', 'privilege.administer.anyUsagePoint'],
    admin: ['privilege.administer.ownUsagePoint', 'privilege.administer.anyUsagePoint'],
    insightView: ['privilege.administer.anyUsagePoint', 'privilege.view.anyUsagePoint', 'privilege.administer.ownUsagePoint', 'privilege.view.ownUsagePoint'],
    insightAdmin: ['privilege.administer.ownUsagePoint', 'privilege.administer.anyUsagePoint'],
    all: function () {
        return this.checkInsightLicense(Ext.Array.merge(Mdc.privileges.UsagePoint.view, Mdc.privileges.UsagePoint.admin));
    },
    canView: function () {
        return this.checkInsightLicense(Mdc.privileges.UsagePoint.view);
    },

    canAdmin: function () {
        return this.checkInsightLicense(Mdc.privileges.UsagePoint.admin);
    },

    checkInsightLicense: function (privileges) {
        var me = this,
            checkStatus = function(){
                if(me.insightStatus != 'ACTIVE'){
                    return (Uni.Auth.checkPrivileges(privileges));
                } else {
                    return false;
                }
            };

        if(!me.insightStatus){
            Ext.Ajax.request({
                url: '/api/apps/apps/status/INS',
                method: 'GET',
                success: function(response) {
                    me.insightStatus = Ext.decode(response.responseText, true).status;
                    return checkStatus();
                }
            });
        } else {
            return checkStatus();
        }
    },

    canViewInInsight: function () {
        var result = false;
        Mdc.privileges.UsagePoint.insightView.forEach(function (item) {
            if (Uni.Auth.hasPrivilegeInApp(item, 'INS')) {
                result = true;
            }
        });
        return result;
    }
});
