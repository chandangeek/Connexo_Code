/**
 * @class MdcApp.controller.Main
 */
Ext.define('MdcApp.controller.Main', {
    extend: 'Uni.controller.AppController',
    requires:[
        'Sam.privileges.DeploymentInfo',
        'Sam.privileges.DataPurge',
        'Sam.privileges.License',
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
        'Dxp.privileges.DataExport',
        'Est.privileges.EstimationConfiguration',
        'Dlc.privileges.DeviceLifeCycle',
        'Mdc.privileges.DeviceConfigurationEstimations',
        'Fim.privileges.DataImport',
        'Fwc.privileges.FirmwareCampaign',
        'Bpm.privileges.BpmManagement',
        'Dbp.privileges.DeviceProcesses'

    ],
    applicationTitle: 'Connexo MultiSense',
    applicationKey: 'MDC',
    defaultToken: '/dashboard',
    searchEnabled:  Mdc.privileges.Device.canSearchDevices(),
    onlineHelpEnabled: true,
    privileges: Ext.Array.merge(
        Sam.privileges.DeploymentInfo.all(),
        Sam.privileges.DataPurge.all(),
        Sam.privileges.License.all(),
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
        Isu.privileges.Issue.all(),
        Dxp.privileges.DataExport.all(),
        Dlc.privileges.DeviceLifeCycle.all(),
        Mdc.privileges.DeviceConfigurationEstimations.all(),
        Fim.privileges.DataImport.all(),
        Fwc.privileges.FirmwareCampaign.all(),
        Bpm.privileges.BpmManagement.all(),
        Dbp.privileges.DeviceProcesses.all()

    ),
    controllers: [
        'Sam.controller.Main',
        'Cfg.controller.Main',
        'Mdc.controller.Main',
        'Isu.controller.Main',
        'Idc.controller.Main',
        'Idv.controller.Main',
        'Bpm.controller.Main',
        'Ddv.controller.Main',
        'Dsh.controller.Main',
        'Yfn.controller.Main',
        'Dlc.main.controller.Main',
        'Fwc.controller.Main',
        'Dxp.controller.Main',
        'Est.main.controller.Main',
        'Fim.controller.Main',
        'Dbp.controller.Main'
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
