Ext.define('Mdc.view.setup.deviceregisterdata.numerical.Preview', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainPreview',
    alias: 'widget.deviceregisterreportpreview-numerical',
    requires: [
        'Mdc.view.setup.deviceregisterdata.ValidationPreview'
    ],
    itemId: 'deviceregisterreportpreview',
    title: '',
    unitOfMeasureCollected: '',
    unitOfMeasureCalculated: '',
    multiplier: null,

    items: {
        xtype: 'form',
        defaults: {
            xtype: 'container',
            layout: 'form'
        },
        items: [
            {
                xtype: 'fieldcontainer',
                itemId: 'mdc-register-data-preview-fields-container',
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200,
                    width: 1000
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                        name: 'timeStamp',
                        renderer: function (value) {
                            if (!Ext.isEmpty(value)) {
                                return Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',
                                    [Uni.DateTime.formatDateLong(new Date(value)), Uni.DateTime.formatTimeLong(new Date(value))]
                                );
                            }
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.readingTime', 'MDC', 'Reading time'),
                        name: 'reportedDateTime',
                        renderer: function (value) {
                            if (!Ext.isEmpty(value)) {
                                return Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',
                                    [Uni.DateTime.formatDateLong(new Date(value)), Uni.DateTime.formatTimeLong(new Date(value))]
                                );
                            }
                        }
                    },
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
                                        var me = this.up('form').up('#deviceregisterreportpreview');
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
                        fieldLabel: Uni.I18n.translate('general.calculatedValue', 'MDC', 'Calculated value'),
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
                    },
                    {
                        fieldLabel: Uni.I18n.translate('device.registerData.deltaValue', 'MDC', 'Delta value'),
                        name: 'deltaValue',
                        renderer: function (value) {
                            var record = this.up('form').getRecord();
                            if (record && value) {
                                var me = this.up('form').up('#deviceregisterreportpreview'),
                                    calculatedUnit = record.get('calculatedUnit');
                                return Uni.Number.formatNumber(value, -1) + ' ' +
                                    (calculatedUnit !== '' ? calculatedUnit : me.unitOfMeasureCollected);
                            } else {
                                return '-'
                            }
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier'),
                        itemId: 'mdc-register-preview-numerical-multiplier',
                        name: 'multiplier'
                    }
                ]
            },
            {
                xtype: 'deviceregisterreportpreview-validation'
            }
        ]
    }
});