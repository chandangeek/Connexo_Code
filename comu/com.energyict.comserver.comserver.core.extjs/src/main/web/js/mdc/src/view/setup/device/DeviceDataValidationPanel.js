/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.device.DeviceDataValidationPanel', {
    extend: 'Ext.panel.Panel',
    xtype: 'device-data-validation-panel',

    overflowY: 'auto',
    itemId: 'deviceDataValidationPanel',
    deviceId: null,
    ui: 'tile',
    title: Uni.I18n.translate('device.dataValidation', 'MDC', 'Data validation'),
    layout: 'fit',

    initComponent: function () {
        var me = this;

        me.items =
            {
                xtype: 'container',
                layout: 'fit',
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
                            labelWidth: 200,
                            style: {
                                marginRight: '20px',
                                padding: '20px'
                            },
                            flex: 1
                        },
                        items: [
                            {
                                itemId: 'statusField',
                                fieldLabel: Uni.I18n.translate('device.registerData.deviceValidationStatus', 'MDC', 'Device validation status'),
                                name: 'isActive',
                                renderer: function (value) {
                                    return value ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                                }
                            },
                            {
                                itemId: 'allDataValidatedField',
                                fieldLabel: Uni.I18n.translate('device.registerData.allDataValidated', 'MDC', 'All data validated'),
                                name: 'allDataValidated',
                                htmlEncode: false,
                                renderer: function (value) {
                                    return value
                                        ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                                        : Uni.I18n.translate('general.no', 'MDC', 'No') + '<span class="icon-flag6" style="margin-left:10px; position:absolute;"></span>';
                                }
                            },
                            {
                                itemId: 'fld-validation-result',
                                fieldLabel: Uni.I18n.translate('device.dataValidation.validationResult', 'MDC', 'Validation result'),
                                renderer: function (record) {
                                    if (record) {
                                        this.show();
                                        var route = me.router.getRoute('devices/device/validationresultsdata'),
                                            url = route.buildUrl(),
                                            msg;

                                        if (record.get('loadProfileSuspectCount') != 0 || record.get('registerSuspectCount') != 0) {
                                            msg = Uni.I18n.translate('device.dataValidation.recentsuspects', 'MDC', 'Recent suspects');
                                        } else {
                                            msg = Uni.I18n.translate('device.dataValidation.nosuspects', 'MDC', 'No suspects');
                                        }

                                        return '<a href="' + url + '">' + Ext.String.htmlEncode(msg) + '</a>';
                                    } else {
                                        this.hide();
                                        return null;
                                    }
                                }
                            },
                            {
                                fieldLabel: Uni.I18n.translate('device.lastValidationRun', 'MDC', 'Last validation run'),
                                itemId: 'lastValidationCont',
                                name: 'lastChecked',
                                renderer: function (value) {
                                    return Ext.String.format(
                                        '<span style="display: inline-block; float: left; margin: 0px 10px 0px 0px">{0}</span>' +
                                        '<span style="display: inline-block; float: left; width: 16px; height: 16px;" class="uni-icon-info-small" data-qtip="{1}"></span>',
                                        value ? Uni.DateTime.formatDateTimeLong(value) : Uni.I18n.translate('general.never', 'MDC', 'Never'),
                                        Uni.I18n.translate('device.lastValidation.tooltip', 'MDC', 'The moment when the validation ran for the last time.')
                                    );
                                }
                            }
                        ]
                    }
                ]
            };

        me.callParent();

    }
});


