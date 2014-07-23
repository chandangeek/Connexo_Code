Ext.define('Mdc.view.setup.ruledeviceconfiguration.RuleDeviceConfigurationAddGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.rule-device-configuration-add-grid',
    itemId: 'addDeviceConfigGrid',
    store: 'Mdc.store.RuleDeviceConfigurationsNotLinked',
    maxHeight: 400,

    requires: [
        'Mdc.view.setup.ruledeviceconfiguration.RuleAddDeviceConfigurationActionMenu',
        'Ext.grid.plugin.BufferedRenderer',
        'Mdc.store.RuleDeviceConfigurationsNotLinked'
    ],

    plugins : [{
        ptype: 'bufferedrenderer',
        trailingBufferZone: 5,
        leadingBufferZone: 5,
        scrollToLoadBuffer: 10,
        onViewResize: function(view, width, height, oldWidth, oldHeight) {
            if (!oldHeight || height !== oldHeight) {
                var me = this,
                    newViewSize,
                    scrollRange;
                if (view.all.getCount()) {
                    delete me.rowHeight;
                }
                scrollRange = me.getScrollHeight();
                newViewSize = 18;
                me.viewSize = me.setViewSize(newViewSize);
                me.stretchView(view, scrollRange);
            }
        }
    }],
    selType: 'checkboxmodel',
    selModel: {
        showHeaderCheckbox: false
    },

    ruleSetId: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('validation.deviceConfiguration', 'CFG', 'Device configuration'),
                dataIndex: 'config_name_link',
                flex: 0.3
            },
            {
                header: Uni.I18n.translate('validation.deviceType', 'CFG', 'Device type'),
                dataIndex: 'deviceType_name',
                flex: 0.3
            },
            {
                header: Uni.I18n.translate('validation.status', 'CFG', 'Status'),
                dataIndex: 'config_active',
                flex: 0.3
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.ruledeviceconfiguration.RuleAddDeviceConfigurationActionMenu'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'toolbar',
                dock: 'top',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'radiogroup',
                        itemId: 'radiogroupAddDeviceConfig',
                        columns: 1,
                        vertical: true,
                        submitValue: false,
                        defaults: {
                            padding: '0 0 30 0'
                        },
                        items: [
                            {
                                itemId: 'radioAll',
                                boxLabel: '<b>' + Uni.I18n.translate('validation.allDeviceConfigurations', 'CFG', 'All device configurations') + '</b><br/>' +
                                    '<span style="color: grey;">' + Uni.I18n.translate('validation.selectAllDeviceConfigurations', 'CFG', 'Select all device configurations related to filters') + '</span>',
                                name: 'configsRadio',
                                inputValue: 'ALL',
                                checked: true
                            },
                            {
                                itemId: 'radioSelected',
                                boxLabel: '<b>' + Uni.I18n.translate('validation.selectedDeviceConfigurations', 'CFG', 'Selected device configurations') + '</b><br/><span style="color: grey;">' + Uni.I18n.translate('validation.selectDeviceConfigurations', 'CFG', 'Select device configurations in table') + '</span>',
                                name: 'configsRadio',
                                inputValue: 'SELECTED'
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'text',
                                text: Uni.I18n.translate('deviceconfiguration.selectedNone', 'CFG', 'No device configurations selected'),
                                itemId: 'countLabel'
                            },
                            {
                                xtype: 'button',
                                margin: '0 0 0 8',
                                text: Uni.I18n.translate('general.uncheckAll', 'CFG', 'Uncheck all'),
                                action: 'uncheck'
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'toolbar',
                dock: 'bottom',
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.add', 'CFG', 'Add'),
                        action: 'add',
                        itemId: 'addDeviceConfigToRuleSet',
                        ui: 'action'
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                        action: 'cancel',
                        itemId: 'cancelDeviceConfigToRuleSet',
                        ui: 'link',
                        listeners: {
                            click: {
                                fn: function () {
                                    window.location.href = '#/administration/validation/rulesets/' + me.ruleSetId + '/deviceconfigurations';
                                }
                            }
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});