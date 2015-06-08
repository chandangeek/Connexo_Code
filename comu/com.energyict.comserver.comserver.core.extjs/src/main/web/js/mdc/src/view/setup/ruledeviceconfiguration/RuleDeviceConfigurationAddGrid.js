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
        return Uni.I18n.translatePlural(
            'setup.ruledeviceconfiguration.RuleDeviceConfigurationAddGrid.counterText',
            count,
            'MDC',
            '{0} device configurations selected'
        );
    },

    allLabel: Uni.I18n.translate('validation.allDeviceConfigurations', 'CFG', 'All device configurations'),
    allDescription: Uni.I18n.translate('validation.selectAllDeviceConfigurations', 'CFG', 'Select all device configurations related to filters'),

    selectedLabel: Uni.I18n.translate('validation.selectedDeviceConfigurations', 'CFG', 'Selected device configurations'),
    selectedDescription: Uni.I18n.translate('validation.selectDeviceConfigurations', 'CFG', 'Select device configurations in table'),

    columns: [
        {
            header: Uni.I18n.translate('validation.deviceConfiguration', 'CFG', 'Device configuration'),
            dataIndex: 'config_name_link',
            flex: 1,
            renderer: false
        },
        {
            header: Uni.I18n.translate('validation.deviceType', 'CFG', 'Device type'),
            dataIndex: 'deviceType_name',
            flex: 1
        },
        {
            header: Uni.I18n.translate('validation.status', 'CFG', 'Status'),
            dataIndex: 'config_active'
        }
    ],

    initComponent: function () {
        var me = this;

        me.cancelHref = '#/administration/validation/rulesets/' + me.ruleSetId + '/deviceconfigurations';
        me.callParent(arguments);
    }
});