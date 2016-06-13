Ext.define('Mdc.view.setup.deviceregisterdata.MainPreview', {
    extend: 'Ext.tab.Panel',

    requires: [
        'Mdc.view.setup.deviceregisterdata.ActionMenu',
        'Uni.form.field.EditedDisplay'
    ],
    frame: false,

    getGeneralItems: function() {
        return [];
    },

    getValidationItems: function() {
        return [];
    },

    renderDateTimeLong: function(value) {
        if (Ext.isEmpty(value)) {
            return '-';
        }
        var date = new Date(value);
        return Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateLong(date), Uni.DateTime.formatTimeLong(date)]);
    },

    initComponent: function () {
        var me = this,
            generalItems = me.getGeneralItems(),
            validationItems = me.getValidationItems(),
            qualityItems = [
                {
                    xtype: 'uni-form-info-message',
                    itemId: 'mdc-noReadings-msg',
                    text: Uni.I18n.translate('general.reading.noDataQualities', 'MDC', 'There are no reading qualities for this reading.'),
                    margin: '7 10 32 0',
                    padding: '10'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.deviceQuality', 'MDC', 'Device quality'),
                    itemId: 'mdc-device-quality',
                    labelWidth: 200,
                    htmlEncode: false
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.multiSenseQuality', 'MDC', 'MultiSense quality'),
                    itemId: 'mdc-multiSense-quality',
                    labelWidth: 200,
                    htmlEncode: false
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.insightQuality', 'MDC', 'Insight quality'),
                    itemId: 'mdc-insight-quality',
                    labelWidth: 200,
                    htmlEncode: false
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.thirdPartyQuality', 'MDC', 'Third party quality'),
                    itemId: 'mdc-thirdParty-quality',
                    labelWidth: 200,
                    htmlEncode: false
                }
            ];

        me.items = [
            {
                title: Uni.I18n.translate('registerdata.generaltab.title', 'MDC', 'General'),
                items: {
                    xtype: 'form',
                    itemId: 'mdc-register-general-form',
                    frame: true,
                    layout: 'vbox',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: generalItems
                }
            },
            {
                title: Uni.I18n.translate('registerdata.validationtab.title', 'MDC', 'Validation'),
                items: {
                    xtype: 'form',
                    itemId: 'mdc-register-validation-form',
                    frame: true,
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: validationItems,
                    layout: 'vbox'
                }
            },
            {
                title: Uni.I18n.translate('registerdata.readingqualitytab.title', 'MDC', 'Reading quality'),
                items: {
                    xtype: 'form',
                    itemId: 'mdc-register-qualities-form',
                    frame: true,
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: qualityItems,
                    layout: 'vbox'
                }
            }
        ];

        me.callParent(arguments);
    },

    updateContent: function(registerRecord) {
        var me = this,
            measurementDate = new Date(registerRecord.get('timeStamp')),
            title = Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',
                [Uni.DateTime.formatDateLong(measurementDate), Uni.DateTime.formatTimeLong(measurementDate)],
                false),
            calculatedValueField = me.down('#mdc-calculated-value-field'),
            deltaValueField = me.down('displayfield[name=deltaValue]'),
            multiplierField = me.down('#mdc-register-preview-'+registerRecord.get('type')+'-multiplier'),
            hasCalculatedValue = !Ext.isEmpty(registerRecord.get('calculatedValue')),
            hasDeltaValue = !Ext.isEmpty(registerRecord.get('deltaValue'));

        me.setLoading();
        Ext.suspendLayouts();
        me.down('#mdc-register-general-form').setTitle(title);
        me.down('#mdc-register-validation-form').setTitle(title);
        me.down('#mdc-register-qualities-form').setTitle(title);
        me.down('#mdc-register-general-form').loadRecord(registerRecord);
        me.down('#mdc-register-validation-form').loadRecord(registerRecord);

        if (calculatedValueField) {
            calculatedValueField.setVisible(hasCalculatedValue);
        }
        if (deltaValueField) {
            deltaValueField.setVisible(hasDeltaValue);
        }
        if (multiplierField) {
            if (hasCalculatedValue) {
                multiplierField.setValue(record.get('multiplier'));
            }
            multiplierField.setVisible(hasCalculatedValue);
        }

        me.setDataQualities(registerRecord.get('readingQualities'));

        Ext.resumeLayouts(true);
        me.setLoading(false);
    },

    setDataQualities: function(dataQualities) {
        var me = this,
            deviceQualityField = me.down('#mdc-device-quality'),
            multiSenseQualityField = me.down('#mdc-multiSense-quality'),
            insightQualityField = me.down('#mdc-insight-quality'),
            thirdPartyQualityField = me.down('#mdc-thirdParty-quality');

        if (Ext.isEmpty(dataQualities)) {
            me.down('#mdc-noReadings-msg').show();
        } else {
            me.down('#mdc-noReadings-msg').hide();
        }
        me.setDataQualityFields(deviceQualityField, multiSenseQualityField, insightQualityField, thirdPartyQualityField, dataQualities);
    },

    setDataQualityFields: function(deviceQualityField, multiSenseQualityField, insightQualityField, thirdPartyQualityField, dataQualities) {
        var showDeviceQuality = false,
            showMultiSenseQuality = false,
            showInsightQuality = false,
            show3rdPartyQuality = false,
            field = undefined;

        deviceQualityField.setValue('');
        multiSenseQualityField.setValue('');
        insightQualityField.setValue('');
        thirdPartyQualityField.setValue('');

        Ext.Array.forEach(dataQualities, function(readingQuality) {
            if (readingQuality.cimCode.startsWith('1.')) {
                showDeviceQuality |= true;
                field = deviceQualityField;
            } else if (readingQuality.cimCode.startsWith('2.')) {
                showMultiSenseQuality |= true;
                field = multiSenseQualityField;
            } else if (readingQuality.cimCode.startsWith('3.')) {
                showInsightQuality |= true;
                field = insightQualityField;
            } else if (readingQuality.cimCode.startsWith('4.')||readingQuality.cimCode.startsWith('5.')) {
                show3rdPartyQuality |= true;
                field = thirdPartyQualityField;
            }
            if (!Ext.isEmpty(field)) {
                field.setValue(field.getValue()
                    + (Ext.isEmpty(field.getValue()) ? '' : '<br>')
                    + readingQuality.indexName + ' (' + readingQuality.cimCode + ')'
                );
            }
        });

        showDeviceQuality ? deviceQualityField.show() : deviceQualityField.hide();
        showMultiSenseQuality ? multiSenseQualityField.show() : multiSenseQualityField.hide();
        showInsightQuality ? insightQualityField.show() : insightQualityField.hide();
        show3rdPartyQuality ? thirdPartyQualityField.show() : thirdPartyQualityField.hide();
    }

});


