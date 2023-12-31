/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Mdc.privileges.Device
 *
 * Class that defines privileges for Device
 */
Ext.define('Mdc.privileges.Device', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,

    viewDeviceData:['privilege.administrate.deviceData','privilege.view.device'],
    viewDevice:['privilege.administrate.device','privilege.view.device'],
    administrateDeviceOrDeviceCommunication: ['privilege.administrate.device', 'privilege.administrate.deviceCommunication'],
    viewDevices: ['privilege.administrate.deviceData','privilege.view.device','privilege.view.masterData'],
    deviceOperator: ['privilege.view.device','privilege.administrate.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'],
    administrateDevice:['privilege.administrate.device'],
    flagDevice:['privilege.administrate.deviceData','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'],
    administrateDeviceData:['privilege.administrate.deviceData'],
    changeDeviceConfiguration:['privilege.administrate.deviceCommunication'],
    viewDeviceCommunication:['privilege.administrate.deviceData','privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'],
    administrateDeviceCommunication:['privilege.administrate.deviceCommunication'],
    operateDeviceCommunication:['privilege.operate.deviceCommunication'],
    runWithPriority:['privilege.operate.deviceCommunication.runWithPriority'],
    administrateOrOperateDeviceCommunication:['privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication'],
    viewOrAdministrateDeviceData : ['privilege.view.device','privilege.administrate.deviceData'],
    viewOrAdministrateOrOperateDeviceCommunication: ['privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication','privilege.view.device'],
    editDeviceAttributes: ['privilege.administrate.attribute.device'],
    deviceProcesses: ['privilege.view.bpm', 'privilege.administrate.bpm'],
    viewProcesses: ['privilege.view.bpm'],
    viewValidationConfiguration: ['privilege.administrate.device', 'privilege.view.device', 'privilege.administer.device.validationConfiguration'],
    administerValidationConfiguration: ['privilege.administer.device.validationConfiguration'],
    viewEstimationConfiguration: ['privilege.administrate.device', 'privilege.view.device', 'privilege.administer.device.estimationConfiguration'],
    administerEstimationConfiguration: ['privilege.administer.device.estimationConfiguration'],
    viewAdminAlarm: ['privilege.view.alarm', 'privilege.comment.alarm', 'privilege.close.alarm', 'privilege.assign.alarm', 'privilege.action.alarm'],
    deviceExecuteProcesses: ['privilege.execute.processes.lvl.1',
                    'privilege.execute.processes.lvl.2',
                    'privilege.execute.processes.lvl.3',
                    'privilege.execute.processes.lvl.4'],
    adminTimeSlicedCps: ['privilege.administer.device.time.sliced.cps'],
    viewCommands: [
        'privilege.operate.deviceCommunication',
        'privilege.administrate.deviceCommunication',
        'execute.device.message.level1',
        'execute.device.message.level2',
        'execute.device.message.level3',
        'execute.device.message.level4'
    ],
    sendSapRegisteredNotification: 'privilege.send.webservice.request',

    all: function() {
        return Ext.Array.merge(Mdc.privileges.Device.viewDevice,
            Mdc.privileges.Device.viewDeviceData,
            Mdc.privileges.Device.deviceOperator,
            Mdc.privileges.Device.administrateDevice,
            Mdc.privileges.Device.administrateDeviceData,
            Mdc.privileges.Device.viewDeviceCommunication,
            Mdc.privileges.Device.administrateDeviceCommunication,
            Mdc.privileges.Device.administrateOrOperateDeviceCommunication,
            Mdc.privileges.Device.editDeviceAttributes
        );
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.viewDevice );
    },
    canAdministrateDeviceData : function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.administrateDeviceData );
    },
    canViewOrAdministrateDeviceData:function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.viewOrAdministrateDeviceData );
    },
    canAddDevice: function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.administrateDevice );
    },
    canAdministrateDevice: function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.administrateDevice );
    },
    canSearchDevices : function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.viewDeviceCommunication );
    },
    canOperateDevice : function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.deviceOperator);
    },
    canOperateDeviceCommunication : function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.operateDeviceCommunication );
    },
    canAdministrateOrOperateDeviceCommunication : function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.administrateOrOperateDeviceCommunication );
    },
    canAadministrateDeviceOrDeviceCommunication : function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.administrateDeviceOrDeviceCommunication );
    },
    canViewDeviceCommunication: function() {
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.viewDeviceCommunication);
    },
    canEditDeviceAttributes: function() {
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.editDeviceAttributes);
    },
    canFlagDevice: function() {
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.flagDevice);
    },
    canViewDevices: function() {
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.viewDevices);
    },
    canViewProcessMenu: function() {
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.deviceProcesses) && Uni.Auth.checkPrivileges(Mdc.privileges.Device.deviceExecuteProcesses);
    },
    canAdministrateTimeSlicedCps: function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.adminTimeSlicedCps);
    },
    hasFullAdministrateTimeSlicedCps: function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.adminTimeSlicedCps) && Uni.Auth.checkPrivileges(Mdc.privileges.Device.administrateDeviceData);
    },
    canViewValidationConfiguration: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.viewValidationConfiguration);
    },
    canViewEstimationConfiguration: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.viewEstimationConfiguration);
    },
    canAdministerValidationConfiguration: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.administerValidationConfiguration);
    },
    canAdministerEstimationConfiguration: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.administerEstimationConfiguration);
    },
    canViewCommands: function() {
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.viewCommands);
    },
    canViewAdminAlarm: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.Device.viewAdminAlarm);
    }
});
