/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.ruledeviceconfiguration.RuleDeviceConfigurationAddGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'rule-device-configuration-add-grid',
    itemId: 'addDeviceConfigGrid',
    store: 'Mdc.store.RuleDeviceConfigurationsNotLinked',

    requires: [
        'Mdc.view.setup.ruledeviceconfiguration.RuleAddDeviceConfigurationActionMenu',
        'Ext.grid.plugin.BufferedRenderer',
        'Mdc.store.RuleDeviceConfigurationsNotLinked'
    ],

    ruleSetId: null,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfDeviceConfigurations.selected', count, 'MDC',
            'No device configurations selected', '{0} device configuration selected', '{0} device configurations selected'
        );
    },

    allLabel: Uni.I18n.translate('validation.allDeviceConfigurations', 'MDC', 'All device configurations'),
    allDescription: Uni.I18n.translate('validation.selectAllDeviceConfigurations', 'MDC', 'Select all device configurations related to filters'),

    selectedLabel: Uni.I18n.translate('validation.selectedDeviceConfigurations', 'MDC', 'Selected device configurations'),
    selectedDescription: Uni.I18n.translate('validation.selectDeviceConfigurations', 'MDC', 'Select device configurations in table'),

    columns: [
        {
            header: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
            dataIndex: 'config_name_link',
            flex: 1,
            renderer: false
        },
        {
            header: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
            dataIndex: 'deviceType_name',
            flex: 1
        },
        {
            header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
            dataIndex: 'config_active'
        }
    ],

    initComponent: function () {
        var me = this;

        me.cancelHref = '#/administration/validation/rulesets/' + me.ruleSetId + '/deviceconfigurations';
        me.callParent(arguments);
    }
});