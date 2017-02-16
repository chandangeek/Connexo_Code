/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.dynamicprivileges.DeviceState', {
    singleton: true,

    requires: [
        'Uni.DynamicPrivileges'
    ],

    issuesWidget: 'devices.widget.issues',
    validationWidget: 'devices.widget.validation',
    topologyWidget: 'devices.widget.communication.topology',
    connectionWidget: 'devices.widget.connection',
    communicationTasksWidget: 'devices.widget.communication.tasks',
    validationActions: 'devices.actions.validation',
    estimationActions: 'devices.actions.estimation',
    generalAttributesActions: 'devices.actions.general.attributes',
    securitySettingsActions: 'devices.actions.security.settings',
    protocolDialectsActions: 'devices.actions.protocol.dialects',
    communicationTopologyActions: 'devices.actions.communication.topology',
    communicationPlanningActions: 'devices.actions.communication.planning',
    communicationTasksActions: 'devices.actions.communication.tasks',
    connectionMethodsActions: 'devices.actions.connection.methods',
    validationRuleSetsActions: 'devices.actions.validation.rulesets',
    estimationRuleSetsActions: 'devices.actions.estimation.rulesets',
    deviceCommandActions: 'devices.actions.device.commands',
    firmwareManagementActions: 'devices.actions.firmware.management',
    deviceDataEditActions: 'devices.actions.data.edit',
    changeDeviceConfiguration: 'devices.actions.change.device.configuration',
    communicationPlanningPages: 'devices.pages.communication.planning',
    deviceCommandWithPrivileges: 'privilege.command.has.privileges',
    timeOfUseAllowed: 'devices.pages.timeofuseallowed',
    sendCalendar: 'devices.actions.timeofuse.send',
    sendWithDateType: 'devices.actions.timeofuse.sendDateType',
    sendWithDate: 'devices.actions.timeofuse.sendDate',
    sendWithDateContract: 'devices.actions.timeofuse.sendDateContract',
    sendWithDateTime: 'devices.actions.timeofuse.sendDateTime',
    sendSpecialDays: 'devices.actions.timeofuse.sendSpecial',
    sendSpecialDaysWithType: 'devices.actions.timeofuse.sendSpecialType',
    sendSpecialDaysWithContractAndDate: 'devices.actions.timeofuse.sendSpecialContractDate',
    verifyCalendar: 'devices.actions.timeofuse.verify',
    clearAndDisable: 'devices.actions.timeofuse.clearanddisable',
    activatePassive: 'devices.actions.timeofuse.activatepassive',
    supportsPassive: 'devices.timeofuse.supportspassive',
    supportsSend: 'devices.timeofuse.supportssend',

    allDeviceCommandPrivileges: ['privilege.command.has.privileges', 'devices.actions.device.commands'],


    canEditData: function () {
        return Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.deviceDataEditActions);
    },

    canVerify: function () {
        return Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.verifyCalendar);
    },

    canSendCalendar: function () {
        return Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendCalendar)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateType)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDate)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateContract)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateTime)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendSpecialDays)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendSpecialDaysWithType)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendSpecialDaysWithContractAndDate)
    },

    typeSupported: function () {
        return Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateType)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendSpecialDaysWithType)
    },

    bothFullAndSpecialSupported: function () {
        return (Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendCalendar)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateType)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDate)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateContract)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateTime))
            && (Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendSpecialDays)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendSpecialDaysWithType)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendSpecialDaysWithContractAndDate))
    },

    contractSupported: function () {
        return Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendSpecialDaysWithContractAndDate)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateContract)
    },

    activationDateSupported: function () {
        return Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateType)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDate)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateContract)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateTime)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendSpecialDaysWithContractAndDate)
    },

    bothSendSupported: function () {
        return (Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendCalendar)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendSpecialDays))
            && (Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateType)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDate)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateContract)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateTime)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendSpecialDaysWithContractAndDate))
    },

    supportsSpecialDays: function (){
        return Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendSpecialDays)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendSpecialDaysWithType)
            || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendSpecialDaysWithContractAndDate)
    },

    supportsNormalSend: function() {
        return Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendCalendar)
        || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateType)
        || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDate)
        || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateContract)
        || Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.sendWithDateTime)
    }

});
