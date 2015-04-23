/**
 * @class MdcApp.controller.Main
 */
Ext.define('MdcApp.controller.Main', {
    extend: 'Uni.controller.AppController',
    requires:[
        'Cfg.privileges.Validation',
        'Mdc.privileges.MasterData',
        'Mdc.privileges.DeviceGroup',
        'Mdc.privileges.DeviceType',
        'Mdc.privileges.Device',
        'Mdc.privileges.CommunicationSchedule',
        'Mdc.privileges.DeviceSecurity',
        'Mdc.privileges.DeviceCommands',
        'Mdc.privileges.Communication',
        'Dvi.privileges.InventoryManagement'
    ],
    applicationTitle: 'Connexo MultiSense',
    applicationKey: 'MDC',
    defaultToken: '/dashboard',
    searchEnabled:  Mdc.privileges.Device.canSearchDevices(),
    onlineHelpEnabled: true,
    privileges: Ext.Array.merge(
            Cfg.privileges.Validation.any(),
            Mdc.privileges.MasterData.any(),
            Mdc.privileges.DeviceGroup.any(),
            Mdc.privileges.DeviceType.any(),
            Mdc.privileges.CommunicationSchedule.any(),
            Mdc.privileges.Communication.any(),
            Mdc.privileges.DeviceSecurity.any(),
            Mdc.privileges.DeviceCommands.any(),
            Mdc.privileges.Device.any(),
            Dvi.privileges.InventoryManagement.any(),
        [   'privilege.close.issue','privilege.comment.issue','privilege.view.issue','privilege.assign.issue','privilege.action.issue'
            ,'privilege.view.creationRule','privilege.administrate.creationRule','privilege.view.assignmentRule']),
    controllers: [
        'Cfg.controller.Main',
        'Mdc.controller.Main',
        'Isu.controller.Main',
        'Idc.controller.Main',
        'Dvi.controller.Main',
        'Dsh.controller.Main',
        'Yfn.controller.Main',
        'Dlc.main.controller.Main',
        'Fwc.controller.Main'
    ],

    init: function () {
        var router = this.getController('Uni.controller.history.Router');

        // default route redirect
        router.initRoute('default', {
            redirect: this.defaultToken.slice(2, this.defaultToken.length),
            route: ''
        });

        this.callParent(arguments);
    }
});
