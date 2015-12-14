Ext.define('Mdc.privileges.DataCollectionKpi', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.administer.dataCollectionKpi', 'privilege.view.dataCollectionKpi'],
    admin: ['privilege.administer.dataCollectionKpi'],
    all: function () {
        return Ext.Array.merge(Mdc.privileges.DataCollectionKpi.view, Mdc.privileges.DataCollectionKpi.admin);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.DataCollectionKpi.view);
    }
});
