Ext.define('Mdc.view.setup.device.DeviceDataValidationPanel', {
    extend: 'Ext.panel.Panel',
    xtype: 'device-data-validation-panel',

    overflowY: 'auto',
    itemId: 'deviceDataValidationPanel',
    mRID: null,
    ui: 'tile',
    title: Uni.I18n.translate('device.dataValidation', 'MDC', 'Data validation'),

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'form',
                        flex: 1,
                        itemId: 'deviceDataValidationForm',
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
                                itemId: 'allDataValidatedField',
                                fieldLabel: Uni.I18n.translate('device.registerData.allDataValidated', 'MDC', 'All data validated'),
                                name: 'allDataValidated',
                                renderer: function (value) {
                                    return value ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') :
                                        Uni.I18n.translate('general.no', 'MDC', 'No') + ' ' + '<span class="icon-validation icon-validation-black"></span>';
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
                                    return Ext.String.htmlEncode(value) + ' ' + Uni.I18n.translate('device.suspects.lastMonth', 'MDC', 'suspects (last month)');
                                }
                            },
                            {
                                fieldLabel: Uni.I18n.translate('device.lastValidation', 'MDC', 'Last validation'),
                                itemId: 'lastValidationCont',
                                name: 'lastChecked',
                                renderer: function (value) {
                                    var icon = '<span style="display: inline-block; width: 16px; height: 16px; margin: 0 0 0 10px" class="uni-icon-info-small" data-qtip="'
                                            + Uni.I18n.translate('device.lastValidation.tooltip', 'MDC', 'The moment when the validation ran for the last time.')
                                            + '"></span>',
                                        text = value ? Uni.DateTime.formatDateTimeLong(value) : Uni.I18n.translate('general.never', 'MDC', 'Never');

                                    return text + icon;
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent();
    }
});


