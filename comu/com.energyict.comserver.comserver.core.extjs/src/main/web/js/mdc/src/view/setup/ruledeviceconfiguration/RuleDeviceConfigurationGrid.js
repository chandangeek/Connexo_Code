Ext.define('Mdc.view.setup.ruledeviceconfiguration.RuleDeviceConfigurationGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.rule-device-configuration-grid',
    store: 'Mdc.store.RuleDeviceConfigurations',
    overflowY: 'auto',
    ruleSetId: null,
    requires: [
        'Mdc.view.setup.ruledeviceconfiguration.RuleDeviceConfigurationActionMenu'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
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
                xtype: 'uni-actioncolumn',
                privileges: Cfg.privileges.Validation.deviceConfiguration,
                menu: {
                    xtype: 'rule-device-configuration-action-menu'
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                displayMsg: Uni.I18n.translate('validation.deviceconfiguration.display.msg', 'MDC', '{0} - {1} of {2} device configurations'),
                displayMoreMsg: Uni.I18n.translate('validation.deviceconfiguration.display.more.msg', 'MDC', '{0} - {1} of more than {2} device configurations'),
                emptyMsg: Uni.I18n.translate('validation.deviceconfiguration.pagingtoolbartop.emptyMsg', 'MDC', 'There are no device configurations to display'),
                dock: 'top',
                border: false,
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('validation.deviceconfiguration.addMultiple', 'MDC', 'Add device configurations'),
                        privileges: Cfg.privileges.Validation.deviceConfiguration,
                        action: 'addDeviceConfiguration',
                        listeners: {
                            click: {
                                fn: function () {
                                    window.location.href = '#/administration/validation/rulesets/' + me.ruleSetId + '/deviceconfigurations/add';
                                }
                            }
                        }
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                itemsPerPageMsg: Uni.I18n.translate('validation.deviceconfiguration.items.per.page.msg', 'MDC', 'Device configurations per page'),
                store: me.store,
                dock: 'bottom',
                params: {id: me.ruleSetId}
            }
        ];
        me.callParent(arguments);
    }
});

