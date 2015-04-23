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
        'Mdc.privileges.CommunicationSchedule',
        'Mdc.privileges.Communication'
    ],
    applicationTitle: 'Connexo MultiSense',
    applicationKey: 'MDC',
    defaultToken: '/dashboard',
    searchEnabled: Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceData','privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication']),
    onlineHelpEnabled: true,
    privileges: Ext.Array.merge(
            Cfg.privileges.Validation.any(),
            Mdc.privileges.MasterData.any(),
            Mdc.privileges.DeviceGroup.any(),
            Mdc.privileges.DeviceType.any(),
            Mdc.privileges.CommunicationSchedule.any(),
            Mdc.privileges.Communication.any(),
        ['privilege.close.issue','privilege.comment.issue','privilege.view.issue','privilege.assign.issue','privilege.action.issue'
            ,'privilege.view.creationRule','privilege.administrate.creationRule','privilege.view.assignmentRule',
            'view.device.security.properties.level1'
        ,'view.device.security.properties.level2','view.device.security.properties.level3','view.device.security.properties.level4','edit.device.security.properties.level1','edit.device.security.properties.level2','edit.device.security.properties.level4'
        ,'edit.device.security.properties.level3','execute.device.message.level1','execute.device.message.level3','execute.device.message.level2','execute.device.message.level4','privilege.add.device','privilege.view.device','privilege.remove.device','privilege.administrate.deviceData'
        ,'privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication',
            'privilege.revoke.inventoryManagement','privilege.import.inventoryManagement']),
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
