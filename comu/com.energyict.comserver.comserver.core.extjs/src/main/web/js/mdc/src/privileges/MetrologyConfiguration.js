Ext.define('Mdc.privileges.MetrologyConfiguration', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,

    view: ['privilege.view.metrologyConfiguration'],
    admin: ['privilege.administer.metrologyConfiguration'],

    all: function () {
        return Ext.Array.merge(Mdc.privileges.MetrologyConfiguration.view, Mdc.privileges.MetrologyConfiguration.admin);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.MetrologyConfiguration.view);
    },

    canAdmin: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.MetrologyConfiguration.admin);
    }
});