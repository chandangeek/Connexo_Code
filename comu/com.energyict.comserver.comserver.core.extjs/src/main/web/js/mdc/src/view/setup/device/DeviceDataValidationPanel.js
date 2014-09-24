Ext.define('Mdc.view.setup.device.DeviceDataValidationPanel', {
    extend: 'Ext.panel.Panel',
    xtype: 'device-data-validation-panel',

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

        me.tools = [
            {
                xtype: 'button',
                itemId: 'floatBtn',
                text: Uni.I18n.translate('general.actions', 'CFG', 'Actions'),
                iconCls: 'x-uni-action-iconD',
                menu: {
                    xtype: 'device-action-menu'
                }
            }
        ];

        me.items = [
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
                            xtype: 'displayfield',
                            labelWidth: 150
                        },
                        items: [
                            {
                                itemId: 'statusField',
                                fieldLabel: Uni.I18n.translate('device.dataValidation.statusSection.title', 'MDC', 'Status'),
                                name: 'isActive',
                                renderer: function (value) {
                                    return value ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive')
                                }
                            },
                            {
                                itemid: 'allDataValidatedField',
                                fieldLabel: Uni.I18n.translate('device.registerData.allDataValidated', 'MDC', 'All data validated'),
                                name: 'allDataValidated',
                                renderer: function (value) {
                                    return value ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No') + '&nbsp;&nbsp;<span class="icon-validation icon-validation-black"></span>';
                                }
                            },
                            {
                                itemId: 'registersField',
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.registers', 'MDC', 'Registers'),
                                name: 'registerSuspectCount',
                                renderer: function (value) {
                                    return value + ' ' + Uni.I18n.translate('device.suspects.lastYear', 'MDC', 'suspects (last year)');
                                }
                            },
                            {
                                itemId: 'profilesField',
                                fieldLabel: Uni.I18n.translate('deviceconfigurationmenu.loadProfiles', 'MDC', 'Load profiles'),
                                name: 'loadProfileSuspectCount',
                                renderer: function (value) {
                                    return value + ' ' + Uni.I18n.translate('device.suspects.lastMonth', 'MDC', 'suspects (last month)');
                                }
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('device.lastValidation', 'MDC', 'Last validation'),
                                itemId: 'lastValidationCont',
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'lastChecked',
                                        margin: '3 0 0 0',
                                        renderer: function (value, field) {
                                            if (!Ext.isEmpty(value)) {
                                                field.up('#lastValidationCont').show();
                                                this.nextSibling('button').setVisible(value ? true : false);
                                                return Uni.I18n.formatDate('deviceloadprofiles.dateFormat', new Date(value), 'MDC', 'M d, Y H:i');
                                            } else {
                                                field.up('#lastValidationCont').hide();
                                            }
                                        }
                                    },
                                    {
                                        xtype: 'button',
                                        tooltip: Uni.I18n.translate('device.lastValidation.tooltip', 'MDC', 'The moment when the validation ran for the last time.'),
                                        iconCls: 'icon-info-small',
                                        ui: 'blank',
                                        shadow: false,
                                        margin: '6 0 0 10',
                                        width: 16
                                    }
                                ]
                            }
                        ]
                    }
//                    ,
//                    {
//                        xtype: 'container',
//                        itemId: 'contForFloatBtn',
//                        margin: '-20 50 0 0',
//                        items: [
//                            {
//                                xtype: 'button',
//                                itemId: 'floatBtn',
//                                text: Uni.I18n.translate('general.actions', 'CFG', 'Actions'),
//                                iconCls: 'x-uni-action-iconD',
//                                menu: {
//                                    xtype: 'device-action-menu'
//                                },
//                                renderTo: Ext.getCmp('contForFloatBtn'),
//                                floating: true
//                            }
//                        ]
//                    }
                ]
            }
        ];

        me.callParent();
    }
});


