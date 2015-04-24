/**
 * @class MdcApp.controller.Main
 */
Ext.define('MdcApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    applicationTitle: 'Connexo MultiSense',
    applicationKey: 'MDC',
    defaultToken: '/dashboard',
    searchEnabled: Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceData','privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication']),
    onlineHelpEnabled: true,
    privileges: ['privilege.close.issue','privilege.comment.issue','privilege.view.issue','privilege.assign.issue','privilege.action.issue','privilege.view.creationRule','privilege.administrate.creationRule','privilege.view.assignmentRule'
        ,'privilege.administrate.sharedCommunicationSchedule','privilege.view.sharedCommunicationSchedule','privilege.administrate.validationConfiguration','privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration','privilege.view.validationConfiguration'
        ,'privilege.view.validateManual','privilege.view.fineTuneValidationConfiguration.onDevice','privilege.administrate.masterData','privilege.view.masterData','privilege.administrate.deviceType','privilege.view.deviceType','view.device.security.properties.level1'
        ,'view.device.security.properties.level2','view.device.security.properties.level3','view.device.security.properties.level4','edit.device.security.properties.level1','edit.device.security.properties.level2','edit.device.security.properties.level4'
        ,'edit.device.security.properties.level3','execute.device.message.level1','execute.device.message.level3','execute.device.message.level2','execute.device.message.level4','privilege.add.device','privilege.view.device','privilege.remove.device','privilege.administrate.deviceData'
        ,'privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication','privilege.administrate.deviceGroup','privilege.view.deviceGroupDetail','privilege.administrate.deviceOfEnumeratedGroup','privilege.revoke.inventoryManagement'
        ,'privilege.import.inventoryManagement','privilege.administrate.communicationAdministration','privilege.view.communicationAdministration'],
    controllers: [
        'Cfg.controller.Main',
        'Mdc.controller.Main',
        'Isu.controller.Main',
        'Idc.controller.Main',
        'Dvi.controller.Main',
        'Dsh.controller.Main',
        'Yfn.controller.Main',
        'Dlc.main.controller.Main',
        'Fwc.controller.Main',
        'Dxp.controller.Main'
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
