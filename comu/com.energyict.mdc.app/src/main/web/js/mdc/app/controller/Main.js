/**
 * @class MdcApp.controller.Main
 */
Ext.define('MdcApp.controller.Main', {
    extend: 'Uni.controller.AppController',
    requires: [
        'Cfg.privileges.Validation',
        'Yfn.privileges.Yellowfin',
        'Mdc.privileges.MasterData',
        'Mdc.privileges.DeviceGroup',
        'Mdc.privileges.DeviceType',
        'Mdc.privileges.Device',
        'Mdc.privileges.CommunicationSchedule',
        'Mdc.privileges.DeviceSecurity',
        'Mdc.privileges.DeviceCommands',
        'Mdc.privileges.Communication',
        'Dvi.privileges.InventoryManagement',
        'Dxp.privileges.DataExport',
        'Est.privileges.EstimationConfiguration'/*,
        'Mdc.privileges.DeviceConfigurationEstimations'*/
    ],
    applicationTitle: 'Connexo MultiSense',
    applicationKey: 'MDC',
    defaultToken: '/dashboard',
    searchEnabled:  Mdc.privileges.Device.canSearchDevices(),
    onlineHelpEnabled: true,
    privileges: Ext.Array.merge(
        Cfg.privileges.Validation.all(),
        Yfn.privileges.Yellowfin.all(),
        Mdc.privileges.MasterData.all(),
        Mdc.privileges.DeviceGroup.all(),
        Mdc.privileges.DeviceType.all(),
        Mdc.privileges.CommunicationSchedule.all(),
        Mdc.privileges.Communication.all(),
        Mdc.privileges.DeviceSecurity.all(),
        Mdc.privileges.DeviceCommands.all(),
        Mdc.privileges.Device.all(),
        Dvi.privileges.InventoryManagement.all(),
        Isu.privileges.Issue.all(),
        Dxp.privileges.DataExport.all(),
        Mdc.privileges.DeviceConfigurationEstimations.all()
    ),
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
        'Dxp.controller.Main',
        'Est.main.controller.Main'
  //      'PlaygroundApp.controller.Main'
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
