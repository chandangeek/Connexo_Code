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
                header: Uni.I18n.translate('validation.deviceConfiguration', 'CFG', 'Device configuration'),
                dataIndex: 'config_name_link',
                flex: 0.4
            },
            {
                header: Uni.I18n.translate('validation.deviceType', 'CFG', 'Device type'),
                dataIndex: 'deviceType_name',
                flex: 0.4
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'rule-device-configuration-action-menu'
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'toolbar',
                dock: 'top',
                items: [
                    {
                        xtype: 'container',
                        flex: 1,
                        items: [
                            {
                                xtype: 'pagingtoolbartop',
                                store: me.store,
                                displayMsg: Uni.I18n.translate('validation.deviceconfiguration.display.msg', 'CFG', '{0} - {1} of {2} device configurations'),
                                displayMoreMsg: Uni.I18n.translate('validation.deviceconfiguration.display.more.msg', 'CFG', '{0} - {1} of more than {2} device configurations'),
                                emptyMsg: Uni.I18n.translate('validation.deviceconfiguration.pagingtoolbartop.emptyMsg', 'CFG', 'There are no device configurations to display'),
                                dock: 'top',
                                border: false
                            }
                        ]
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('validation.deviceconfiguration.addMultiple', 'CFG', 'Add device configurations'),
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
                itemsPerPageMsg: Uni.I18n.translate('validation.deviceconfiguration.items.per.page.msg', 'CFG', 'Device configurations per page'),
                store: me.store,
                dock: 'bottom',
                params: {id: me.ruleSetId}
            }
        ];
        me.callParent(arguments);
    }
});

