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
    viewfineTuneEstimationConfiguration : ['privilege.view.fineTuneEstimationConfiguration.onDeviceConfiguration'],
    administrate: ['privilege.administrate.EstimationConfiguration'],
    runTask:'privilege.run.ScheduleEstimationTask',
    updateTask:['privilege.update.ScheduleEstimationTask'],
    viewTask: ['privilege.view.ScheduleEstimationTask'],
    administrateTask: ['privilege.administrate.ScheduleEstimationTask'],
    estimateManual: ['privilege.view.estimateManual'],
    all: function () {
        return Ext.Array.merge(
            Est.privileges.EstimationConfiguration.view,
            Est.privileges.EstimationConfiguration.viewfineTuneEstimationConfiguration,
            Est.privileges.EstimationConfiguration.administrate,
            Est.privileges.EstimationConfiguration.runTask,
            Est.privileges.EstimationConfiguration.updateTask,
            Est.privileges.EstimationConfiguration.viewTask,
            Est.privileges.EstimationConfiguration.administrateTask,
            Est.privileges.EstimationConfiguration.estimateManual
        );
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Est.privileges.EstimationConfiguration.view) || Uni.Auth.checkPrivileges(Est.privileges.EstimationConfiguration.viewTask) ;
    },
    canAdministrate: function () {
        return Uni.Auth.checkPrivileges(Est.privileges.EstimationConfiguration.administrate);
    },
    canRun:function(){
        return Uni.Auth.checkPrivileges(Est.privileges.EstimationConfiguration.runTask);
    },
    canUpdate:function(){
        return Uni.Auth.checkPrivileges(Est.privileges.EstimationConfiguration.updateTask);
    },
    canEstimate: function () {
        return Uni.Auth.checkPrivileges(Est.privileges.EstimationConfiguration.estimateManual);
    }
});