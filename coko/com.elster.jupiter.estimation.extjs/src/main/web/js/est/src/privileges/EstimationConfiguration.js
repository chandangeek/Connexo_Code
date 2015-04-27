/**
 * @class Est.privileges.EstimationConfiguration
 *
 * Class that defines privileges for EstimationConfiguration
 */
Ext.define('Est.privileges.EstimationConfiguration', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.view.EstimationConfiguration'],
    administrate: ['privilege.administrate.EstimationConfiguration'],
    all: function () {
        return Ext.Array.merge(Est.privileges.EstimationConfiguration.view,
            Est.privileges.EstimationConfiguration.administrate
        );
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Est.privileges.EstimationConfiguration.view);
    },
    canAdministrate: function () {
        return Uni.Auth.checkPrivileges(Est.privileges.EstimationConfiguration.administrate);
    }
});