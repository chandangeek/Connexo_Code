Ext.define('Mdc.view.setup.device.DeviceDataValidationPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.device-data-validation-panel',
    requires: [
        'Mdc.view.setup.device.DeviceActionMenu'
    ],
    overflowY: 'auto',
    itemId: 'deviceDataValidationPanel',
    mRID: null,
    ui: 'tile',
    title: Uni.I18n.translate('device.dataValidation', 'MDC', 'Data validation'),
    initComponent: function () {
        var me = this;
        this.items = [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'form',
                        flex: 1,
                        itemId: 'deviceDataValidationForm',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 150
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                itemId: 'statusField',
                                fieldLabel: Uni.I18n.translate('device.dataValidation.statusSection.title', 'MDC', 'Status')
                            },
                            {
                                xtype: 'displayfield',
                                itemid: 'resultField',
                                fieldLabel: Uni.I18n.translate('device.dataValidation.validationResult', 'MDC', 'Validation result'),
                                value: Uni.I18n.translate('validationStatus.notValidated', 'MDC', 'Not validated yet')
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'registersField',
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.registers', 'MDC', 'Registers')
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'profilesField',
                                fieldLabel: Uni.I18n.translate('deviceconfigurationmenu.loadProfiles', 'MDC', 'Load profiles')
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        itemId: 'contForFloatBtn',
                        margin: '-20 50 0 0',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'floatBtn',
                                text: Uni.I18n.translate('general.actions', 'CFG','Actions'),
                                iconCls: 'x-uni-action-iconD',
                                menu: {
                                    xtype: 'device-action-menu'
                                },
                                renderTo: Ext.getCmp('contForFloatBtn'),
                                floating: true
                            }
                        ]
                    }
                ]
            }
        ];
        this.callParent();
    }
});


