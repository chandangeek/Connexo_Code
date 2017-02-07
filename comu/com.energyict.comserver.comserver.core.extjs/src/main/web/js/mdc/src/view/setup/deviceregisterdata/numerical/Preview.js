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

    getGeneralItems: function () {
        var me = this;
        return [
            {
                fieldLabel: Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                name: 'timeStamp',
                renderer: me.renderDateTimeLong
            },
            {
                fieldLabel: Uni.I18n.translate('device.registerData.measurementPeriod', 'MDC', 'Measurement period'),
                labelWidth: 200,
                name: 'interval',
                renderer: function (value) {
                    var startDate,endDate;
                    if (!Ext.isEmpty(value) && !!value.start) {
                        startDate = new Date(value.start);
                        endDate = new Date(value.end);
                        return Uni.DateTime.formatDateTimeShort(startDate) + ' - ' + Uni.DateTime.formatDateTimeShort(endDate);
                    } else if (!Ext.isEmpty(value) && !!value.end){
                        endDate = new Date(value.end);
                        return Uni.DateTime.formatDateTimeShort(endDate)
                    }
                    return '-';
                }
            },
            //{
            //    fieldLabel: Uni.I18n.translate('device.registerData.readingTime', 'MDC', 'Reading time'),
            //    name: 'reportedDateTime',
            //    renderer: me.renderDateTimeLong
            //},
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected'),
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
                fieldLabel: Uni.I18n.translate('general.calculatedValue', 'MDC', 'Calculated'),
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
                    debugger;
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
            },
            {
                fieldLabel: Uni.I18n.translate('device.registerData.lastUpdated', 'MDC', 'Last updated'),
                name: 'reportedDateTime',
                renderer: function(value){
                    if(value) {
                        var date = new Date(value);
                        return Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateShort(date), Uni.DateTime.formatTimeShort(date)]);
                    }
                }
            }
        ];
    },

    getValidationItems: function () {
        return [
            {
                xtype: 'deviceregisterreportpreview-validation',
                router: this.router,
                fieldLabel: ''
            }
        ];
    }

});