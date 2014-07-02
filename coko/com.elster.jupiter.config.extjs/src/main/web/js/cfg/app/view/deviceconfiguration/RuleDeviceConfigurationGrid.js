Ext.define('Cfg.view.deviceconfiguration.RuleDeviceConfigurationGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.rule-device-configuration-grid',
    store: 'Cfg.store.RuleDeviceConfigurations',
    overflowY: 'auto',
    ruleSetId: null,

    initComponent: function () {
        var me = this;
        me.columns = {
            defaults: {
                menuDisabled: true
            },
            items: [
                {
                    header: Uni.I18n.translate('validation.deviceConfiguration', 'CFG', 'Device configuration'),
                    dataIndex: 'deviceconfiguration',
                    flex: 0.4
                },
                {
                    header: Uni.I18n.translate('validation.deviceType', 'CFG', 'Device type'),
                    dataIndex: 'devicetype',
                    flex: 0.4
                },
                {
                    xtype: 'uni-actioncolumn',
                    items: 'Cfg.view.deviceconfiguration.RuleDeviceConfigurationActionMenu'
                }
            ]
        };
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
                        hrefTarget: '',
                        href: '#/administration/validation/rulesets/' + me.ruleSetId + '/adddeviceconfig'
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
    }
});
