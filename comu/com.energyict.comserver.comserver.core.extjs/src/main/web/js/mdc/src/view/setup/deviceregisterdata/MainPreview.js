Ext.define('Mdc.view.setup.deviceregisterdata.MainPreview', {
    extend: 'Ext.tab.Panel',
    itemId: 'mdc-register-data-tab-panel',

    requires: [
        'Mdc.view.setup.deviceregisterdata.ActionMenu',
        'Uni.form.field.EditedDisplay'
    ],
    frame: false,

    getGeneralItems: function () {
        return [];
    },
    mentionDataLoggerSlave: false,
    router: null,

    getValidationItems: function () {
        return [];
    },

    renderDateTimeLong: function (value) {
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
                    fieldLabel: Uni.I18n.translate('general.MDCQuality', 'MDC', 'MDC quality'),
                    itemId: 'mdc-multiSense-quality',
                    labelWidth: 200,
                    htmlEncode: false
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.MDMQuality', 'MDC', 'MDM quality'),
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

        if (me.mentionDataLoggerSlave) {
            me.on('afterrender', function() {
                me.down('#mdc-register-general-form').insert(1,
                    {
                        xtype: 'displayfield',
                        labelWidth: 200,
                        fieldLabel: Uni.I18n.translate('general.dataLoggerSlave', 'MDC', 'Data logger slave'),
                        itemId: 'mdc-register-data-preview-data-logger-slave',
                        name: 'slaveRegister',
                        renderer: function() {
                            var record = this.up('form').getRecord(),
                                slaveRegister = record ? record.get('slaveRegister') : undefined;
                            if (Ext.isEmpty(slaveRegister)) {
                                return '-';
                            }
                            var slaveId = slaveRegister.deviceName,
                                registerId = slaveRegister.registerId;
                            return Ext.String.format('<a href="{0}">{1}</a>',
                                me.router.getRoute('devices/device/registers/registerdata').buildUrl(
                                    {
                                        deviceId: encodeURIComponent(slaveId),
                                        registerId: registerId
                                    },
                                    me.router.queryParams
                                ),
                                slaveId);
                        }
                    }
                );
            });
        }
        me.callParent(arguments);
    },


    updateContent: function (registerRecord, registerBeingViewed) {
        debugger;
        var me = this,
            measurementDate = new Date(registerRecord.get('timeStamp')),
            title = Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',
                [Uni.DateTime.formatDateLong(measurementDate), Uni.DateTime.formatTimeLong(measurementDate)],
                false),
            calculatedValueField = me.down('#mdc-calculated-value-field'),
            deltaValueField = me.down('displayfield[name=deltaValue]'),
            measurementTime = me.down('displayfield[name=timeStamp]'),
            intervalField = me.down('displayfield[name=interval]'),
            multiplierField = me.down('#mdc-register-preview-' + registerRecord.get('type') + '-multiplier'),
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
            debugger;
 //           calculatedValueField.setValue(registerRecord.get('calculatedValue'));
        }
        if (deltaValueField) {
            deltaValueField.setVisible(hasDeltaValue);
        }
        if (multiplierField) {
            if (hasCalculatedValue) {
                multiplierField.setValue(registerRecord.get('multiplier'));
            }
            multiplierField.setVisible(hasCalculatedValue);
        }
        if(!!intervalField) {
            if (!Ext.isDefined(registerBeingViewed) || registerBeingViewed.get('isCumulative')) {
                measurementTime.hide();
                intervalField.show();
            } else {
                measurementTime.show();
                intervalField.hide();
            }
        }

        me.setDataQualities(registerRecord.get('readingQualities'));

        Ext.resumeLayouts(true);
        me.setLoading(false);
    },

    setDataQualities: function (dataQualities) {
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

    setDataQualityFields: function (deviceQualityField, multiSenseQualityField, insightQualityField, thirdPartyQualityField, dataQualities) {
        var me = this,
            showDeviceQuality = false,
            showMultiSenseQuality = false,
            showInsightQuality = false,
            show3rdPartyQuality = false,
            field = undefined;

        deviceQualityField.setValue('');
        multiSenseQualityField.setValue('');
        insightQualityField.setValue('');
        thirdPartyQualityField.setValue('');

        Ext.Array.forEach(dataQualities, function (readingQuality) {
            if (readingQuality.cimCode.startsWith('1.')) {
                showDeviceQuality |= true;
                field = deviceQualityField;
            } else if (readingQuality.cimCode.startsWith('2.')) {
                showMultiSenseQuality |= true;
                field = multiSenseQualityField;
            } else if (readingQuality.cimCode.startsWith('3.')) {
                showInsightQuality |= true;
                field = insightQualityField;
            } else if (readingQuality.cimCode.startsWith('4.') || readingQuality.cimCode.startsWith('5.')) {
                show3rdPartyQuality |= true;
                field = thirdPartyQualityField;
            }
            if (!Ext.isEmpty(field)) {
                field.setValue(field.getValue()
                    + (Ext.isEmpty(field.getValue()) ? '' : '<br>')
                    + '<span style="display:inline-block; float: left; margin-right:7px;" >' + readingQuality.indexName + ' (' + readingQuality.cimCode + ')' + '</span>'
                    + '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px;" data-qtip="'
                    + me.getTooltip(readingQuality.systemName, readingQuality.categoryName, readingQuality.indexName) + '"></span>'
                );
            }
        });

        showDeviceQuality ? deviceQualityField.show() : deviceQualityField.hide();
        showMultiSenseQuality ? multiSenseQualityField.show() : multiSenseQualityField.hide();
        showInsightQuality ? insightQualityField.show() : insightQualityField.hide();
        show3rdPartyQuality ? thirdPartyQualityField.show() : thirdPartyQualityField.hide();
    },

    getTooltip: function (systemName, categoryName, indexName) {
        var me = this,
            tooltip = '<table><tr><td>';

        tooltip += '<b>' + Uni.I18n.translate('general.readingQuality.field1.name', 'MDC', 'System') + ':</b></td>';
        tooltip += '<td>' + systemName + '</td></tr>';
        tooltip += '<tr><td><b>' + Uni.I18n.translate('general.readingQuality.field2.name', 'MDC', 'Category') + ':</b></td>';
        tooltip += '<td>' + categoryName + '</td></tr>';
        tooltip += '<tr><td><b>' + Uni.I18n.translate('general.readingQuality.field3.name', 'MDC', 'Index') + ':</b></td>';
        tooltip += '<td>' + indexName + '</td></tr></table>';
        return tooltip;
    }

});


