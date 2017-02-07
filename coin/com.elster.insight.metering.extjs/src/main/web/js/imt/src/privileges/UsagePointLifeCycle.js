Ext.define('Imt.privileges.UsagePointLifeCycle', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    configure: ['privilege.usagePoint.lifecycle.administer'],
    view: ['privilege.usagePoint.lifecycle.view'],
    all: function () {
        return Ext.Array.merge(Imt.privileges.UsagePointLifeCycle.view, Imt.privileges.UsagePointLifeCycle.configure);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePointLifeCycle.view);
    },
    canConfigure: function () {
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePointLifeCycle.configure);
    }
});
