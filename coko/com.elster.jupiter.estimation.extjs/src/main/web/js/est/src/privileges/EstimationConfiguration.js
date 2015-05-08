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
    run:'privilege.run.ScheduleEstimationTask',
    update:['privilege.update.EstimationConfiguration',
        'privilege.update.ScheduleEstimationTask'],
    all: function () {
        return Ext.Array.merge(
            Est.privileges.EstimationConfiguration.view,
            Est.privileges.EstimationConfiguration.administrate,
            Est.privileges.EstimationConfiguration.run,
            Est.privileges.EstimationConfiguration.update
        );
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Est.privileges.EstimationConfiguration.view);
    },
    canAdministrate: function () {
        return Uni.Auth.checkPrivileges(Est.privileges.EstimationConfiguration.administrate);
    },
    canRun:function(){
        return Uni.Auth.checkPrivileges(Est.privileges.EstimationConfiguration.run);
    },
    canUpdate:function(){
        return Uni.Auth.checkPrivileges(Est.privileges.EstimationConfiguration.update);
    }
});