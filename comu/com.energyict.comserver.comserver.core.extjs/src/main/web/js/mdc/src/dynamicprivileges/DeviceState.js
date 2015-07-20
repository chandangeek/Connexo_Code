Ext.define('Mdc.dynamicprivileges.DeviceState', {
    singleton: true,

    requires: [
        'Uni.DynamicPrivileges'
    ],

    issuesWidget: 'devices.widget.issues',
    validationWidget:'devices.widget.validation',
    topologyWidget: 'devices.widget.communication.topology',
    connectionWidget:'devices.widget.connection',
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
    communicationPlanningPages: 'devices.pages.communication.planning',

    canEditData: function() {
        return Uni.DynamicPrivileges.checkDynamicPrivileges(Mdc.dynamicprivileges.DeviceState.deviceDataEditActions);
    }

});
