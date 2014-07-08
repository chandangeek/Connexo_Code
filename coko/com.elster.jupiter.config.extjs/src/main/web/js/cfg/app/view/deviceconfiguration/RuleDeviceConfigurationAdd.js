Ext.define('Cfg.view.deviceconfiguration.RuleDeviceConfigurationAdd', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.rule-device-configuration-add',
    ruleSetId: null,
    requires: [
        'Cfg.view.validation.RuleSetSubMenu',
        'Cfg.view.deviceconfiguration.RuleAddDeviceConfigurationActionMenu'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('validation.deviceconfiguration.addMultiple', 'CFG', 'Add device configurations'),
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
                            inputValue: 'ALL'
                        },
                        {
                            itemId: 'radioSelected',
                            boxLabel: '<b>' + Uni.I18n.translate('validation.selectedDeviceConfigurations', 'CFG', 'Selected device configurations') + '</b><br/><span style="color: grey;">' + Uni.I18n.translate('validation.selectDeviceConfigurations', 'CFG', 'Select device configurations in table') + '</span>',
                            name: 'configsRadio',
                            checked: true,
                            inputValue: 'SELECTED'
                        }
                    ]
                },
                {
                    xtype: 'toolbar',
                    itemId: 'addDeviceConfigToolbar',
                    border: 0,
                    aling: 'left',
                    items: [
                        {
                            xtype: 'label',
                            itemId: 'countLabel'
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.uncheckAll', 'CFG', 'Uncheck all'),
                            action: 'uncheck',
                            ui: 'action'
                        }
                    ]
                },
                {
                    xtype: 'grid',
                    store: 'Cfg.store.RuleDeviceConfigurationsNotLinked',
                    height: 395,
                    itemId: 'addDeviceConfigGrid',
                    selType: 'checkboxmodel',
                    selModel: {
                        checkOnly: true,
                        enableKeyNav: false,
                        showHeaderCheckbox: false
                    },
                    columns: {
                        defaults: {
                            sortable: false,
                            menuDisabled: true
                        },
                        items: [
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
                                items: 'Cfg.view.deviceconfiguration.RuleAddDeviceConfigurationActionMenu'
                            }
                        ]
                    },
                    buttonAlign: 'left',
                    buttons: [
                        {
                            text: Uni.I18n.translate('general.add', 'CFG', 'Add'),
                            action: 'add',
                            ui: 'action',
                            margin: '0 0 0 -5'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                            action: 'cancel',
                            ui: 'link',
                            listeners: {
                                click: {
                                    fn: function () {
                                        window.location.href = '#/administration/validation/rulesets/' + this.up('rule-device-configuration-add').ruleSetId + '/deviceconfigurations';
                                    }
                                }
                            }
                        }
                    ]
                },
                {
                    xtype: 'panel',
                    itemId: 'emptyAddPanel',
                    hidden: true,
                    height: 200,
                    items: [
                        {
                            xtype: 'panel',
                            html: '<h4>' + Uni.I18n.translate('validation.empty.deviceconfiguration.title', 'CFG', 'No device configurations found') + '</h4><br>' +
                                Uni.I18n.translate('validation.empty.deviceconfiguration.detail', 'CFG', 'There are no device configurations. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                Uni.I18n.translate('validation.empty.deviceconfiguration.list.item1', 'CFG', 'No device configurations have been added yet.')
                        }
                    ]
                }
            ]
        }
    ]
});

