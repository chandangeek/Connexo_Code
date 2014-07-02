Ext.define('Cfg.view.deviceconfiguration.RuleDeviceConfigurationAdd', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.rule-device-configuration-add',
    deviceTypeId: null,

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('validation.deviceconfiguration.addMultiple', 'CFG', 'Add device configurations'),
            items: [
                {
                    xtype: 'toolbar',
                    border: 0,
                    aling: 'left',
                    items: [
                        {
                            xtype: 'label',
                            name: 'DeviceConfigCount'
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
                    store: 'Cfg.store.RuleDeviceConfigurations',
                    height: 395,
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
                                dataIndex: 'deviceconfiguration',
                                flex: 0.3
                            },
                            {
                                header: Uni.I18n.translate('validation.deviceType', 'CFG', 'Device type'),
                                dataIndex: 'devicetype',
                                flex: 0.3
                            }
                        ]
                    },
                    buttonAlign: 'left',
                    buttons: [
                        {
                            text: Uni.I18n.translate('general.add', 'CFG', 'Add'),
                            action: 'add',
                            disabled: true,
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
                                        window.location.href = '#/administration/devicetypes/' + this.up('add-logbook-types').deviceTypeId + '/logbooktypes';
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
                                Uni.I18n.translate('validation.empty.deviceconfiguration.list.item1', 'CFG', 'No device configurations have been added yet.') + '</li></lv><br>' +
                                Uni.I18n.translate('validation.empty.steps', 'CFG', 'Possible steps:')
                        }
                    ]
                }
            ]
        }
    ]
});

