Ext.define('Cfg.view.deviceconfiguration.RuleDeviceConfigurationGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.rule-device-configuration-grid',
    store: 'Cfg.store.RuleDeviceConfigurations',
    overflowY: 'auto',
    ruleSetId: null,
    requires: [
        'Cfg.view.deviceconfiguration.RuleDeviceConfigurationActionMenu'
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
                    items: 'Cfg.view.deviceconfiguration.RuleDeviceConfigurationActionMenu'
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
                                displayMsg: Uni.I18n.translate('validation.deviceconfiguration.display.msg', 'CFG', '{0} - {1} of {2} Device configurations'),
                                displayMoreMsg: Uni.I18n.translate('validation.deviceconfiguration.display.more.msg', 'CFG', '{0} - {1} of more than {2} Device configurations'),
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
                        ui: 'action',
                        listeners: {
                            click: {
                                fn: function () {
                                    me.up('ruleDeviceConfigurationBrowse').setLoading();
                                    window.location.href = '#/administration/validation/rulesets/' + me.ruleSetId + '/deviceconfig/add';
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
