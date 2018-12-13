/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.numerical.HistoryPreview', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainPreview',
    alias: 'widget.preview-device-registers-history-numerical',
    requires: [
        'Mdc.view.setup.deviceregisterdata.HistoryValidationPreview'
    ],

    title: '',
    unitOfMeasureCollected: '',
    unitOfMeasureCalculated: '',
    multiplier: null,

    getGeneralItems: function () {
        var me = this;
        return [
            {
                xtype: 'container',
                layout: {
                    type: 'column',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
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
                                fieldLabel: Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                                name: 'timeStamp',
                                renderer: me.renderDateTimeLong
                            },
                            {
                                fieldLabel: Uni.I18n.translate('device.registerData.changedOn', 'MDC', 'Changed on'),
                                name: 'reportedDateTime',
                                renderer: me.renderDateTimeLong
                            },
                            {
                                fieldLabel: Uni.I18n.translate('device.registerData.changedBy', 'MDC', 'Changed by'),
                                name: 'userName'
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
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
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value'),
                                layout: {
                                    type: 'hbox'
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        margin: '0 10 0 0',
                                        name: 'value',
                                        renderer: function (value) {
                                            var record = this.up('form').getRecord();
                                            if (record && value) {
                                                return Uni.Number.formatNumber(value, -1) + ' ' + me.unitOfMeasureCollected;
                                            } else {
                                                return '-'
                                            }
                                        }
                                    },
                                    {
                                        xtype: 'edited-displayfield',
                                        name: 'modificationState'
                                    }
                                ]
                            },
                            {
                                xtype: 'fieldcontainer',
                                itemId: 'mdc-calculated-value-field',
                                fieldLabel: Uni.I18n.translate('general.historyCalculatedValue', 'MDC', 'Calculated value'),
                                layout: {
                                    type: 'hbox'
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        margin: '0 10 0 0',
                                        name: 'calculatedValue',
                                        renderer: function (value) {
                                            var record = this.up('form').getRecord();
                                            if (record && value) {
                                                var unit = record.get('calculatedUnit');
                                                return Uni.Number.formatNumber(value, -1) + ' ' + (unit ? unit : '');
                                            } else {
                                                return '-'
                                            }
                                        }
                                    },
                                    {
                                        xtype: 'edited-displayfield',
                                        name: 'calculatedModificationState'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];
    },

    getValidationItems: function () {
        return [
            {
                xtype: 'devicehistoryregisterreportpreview-validation',
                router: this.router,
                fieldLabel: ''
            }
        ];
    }

});