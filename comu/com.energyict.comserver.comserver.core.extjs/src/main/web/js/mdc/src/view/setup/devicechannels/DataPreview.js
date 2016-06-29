Ext.define('Mdc.view.setup.devicechannels.DataPreview', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.deviceLoadProfileChannelDataPreview',
    itemId: 'deviceLoadProfileChannelDataPreview',
    requires: [
        'Mdc.view.setup.devicechannels.DataActionMenu',
        'Uni.form.field.IntervalFlagsDisplay',
        'Mdc.view.setup.devicechannels.ValidationPreview',
        'Uni.form.field.EditedDisplay',
        'Mdc.view.field.ReadingQualities'
    ],
    channelRecord: null,
    channels: null,
    frame: false,

    updateForm: function (record) {
        var me = this,
            intervalEnd = record.get('interval_end'),
            title =  Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',
                [Uni.DateTime.formatDateLong(intervalEnd), Uni.DateTime.formatTimeLong(intervalEnd)],
                false),
            mainValidationInfo,
            bulkValidationInfo,
            router = me.router;

        me.setLoading();
        record.refresh(router.arguments.mRID, router.arguments.channelId, function() {
            Ext.suspendLayouts();
            me.down('#general-panel').setTitle(title);
            me.down('#values-panel').setTitle(title);
            me.down('#general-panel').loadRecord(record);
            me.down('#values-panel').loadRecord(record);

            if (record.get('multiplier')) {
                me.down('#general-panel #mdc-multiplier').show();
                me.down('#values-panel #mdc-multiplier').show();
            } else {
                me.down('#general-panel #mdc-multiplier').hide();
                me.down('#values-panel #mdc-multiplier').hide();
            }

            if (me.channels) {
                Ext.Array.each(me.channels, function (channel) {
                    var mainReadingQualitiesField = me.down('#mainReadingQualities' + channel.id),
                        channelBulkValueField = me.down('#channelBulkValue' + channel.id);
                    if (record.get('channelValidationData')[channel.id]) {
                        mainValidationInfo = record.get('channelValidationData')[channel.id].mainValidationInfo;
                        bulkValidationInfo = record.get('channelValidationData')[channel.id].bulkValidationInfo;
                        //if (mainReadingQualitiesField) {
                        //    me.setReadingQualities(mainReadingQualitiesField, mainValidationInfo);
                        //}
                        //if (bulkValidationInfo) {
                        //    me.setReadingQualities(me.down('#bulkReadingQualities' + channel.id), bulkValidationInfo);
                        //}
                        if (me.down('#channelValue' + channel.id)) {
                            me.down('#channelValue' + channel.id).setValue(record.get('channelData')[channel.id]);
                        }
                        if (channelBulkValueField) {
                            channelBulkValueField.setValue(record.get('channelCollectedData')[channel.id]);
                        }
                    } else {
                        if (mainReadingQualitiesField) {
                            mainReadingQualitiesField.hide();
                        }
                        me.down('#bulkReadingQualities' + channel.id).hide();
                    }
                });
                Ext.Array.findBy(me.channels, function (channel) {
                    if (record.get('channelValidationData')[channel.id]) {
                        me.down('#readingDataValidated').setValue(record.get('channelValidationData')[channel.id].dataValidated);
                        return !record.get('channelValidationData')[channel.id].dataValidated;
                    }
                });
                me.setGeneralReadingQualities(me.down('#generalReadingQualities'), me.up('deviceLoadProfilesData').loadProfile.get('validationInfo'));
            } else {
                me.setGeneralReadingQualities(me.down('#generalReadingQualities'), record.get('readingQualities'));
                me.down('#readingDataValidated').setValue(record.get('dataValidated'));
            }

            Ext.resumeLayouts(true);
            me.setLoading(false);

            me.down('#values-panel').loadRecord(record);
        });
    },

    setGeneralReadingQualities: function (field, value) {
        var result = '',
            me = this,
            url;

        if (value && !Ext.isEmpty(value.suspectReason)) {
            field.show();
            Ext.Array.each(value.suspectReason, function (rule) {
                if (rule.key.deleted) {
                    result += Ext.String.htmlEncode(rule.key.name) + ' ' + Uni.I18n.translate('device.registerData.removedRule', 'MDC', '(removed rule)') + ' - ' + rule.value + ' ' + Uni.I18n.translate('general.suspects', 'MDC', 'suspects') + '<br>';
                } else {
                    if (Cfg.privileges.Validation.canViewOrAdministrate()) {
                        url = me.router.getRoute('administration/rulesets/overview/versions/overview/rules').buildUrl({ruleSetId: rule.key.ruleSetVersion.ruleSet.id, versionId: rule.key.ruleSetVersion.id, ruleId: rule.key.id});
                        result += '<a href="' + url + '"> ' + Ext.String.htmlEncode(rule.key.name) + '</a>';
                    } else {
                        result = Ext.String.htmlEncode(rule.key.name);
                    }
                }
                result += ' - ' + Uni.I18n.translate('general.xsuspects', 'MDC', '{0} suspects',[rule.value]) + '<br>';
            });
            field.setValue(result);
        } else if (Array.isArray(value) && !Ext.isEmpty(value)) {
            field.show();
            Ext.Array.each(value, function (rule) {
                var application = rule.application
                    ? '<span class="application">'+ Uni.I18n.translate('general.application', 'MDC', '({0})', [rule.application.name]) + '</span>'
                    : '';
                result += Ext.String.htmlEncode(rule.name) + '&nbsp;' + application + '<br>';
            });
            field.setValue(result);
        } else {
            field.hide();
        }
    },

    setValueWithResult: function (value, type, channel) {
        var me = this,
            record = me.down('form').getRecord(),
            unitOfMeasure,
            validationInfo,
            validationResultText = '',
            formatValue,
            channelId = channel ? channel.id : undefined;

        if (type === 'main') {
            unitOfMeasure = me.channelRecord
                ? (me.channelRecord.get('calculatedReadingType')
                    ? me.channelRecord.get('calculatedReadingType').names.unitOfMeasure
                    : me.channelRecord.get('readingType').names.unitOfMeasure)
                : (channel.calculatedReadingType
                    ? channel.calculatedReadingType.names.unitOfMeasure
                    : channel.readingType.names.unitOfMeasure);
        } else { // 'bulk'
            unitOfMeasure = me.channelRecord
                ? me.channelRecord.get('readingType').names.unitOfMeasure
                : channel.readingType.names.unitOfMeasure;
        }
        if (me.channels) {
            if (record && record.get('channelValidationData')[channelId]) {
                validationInfo = (type == 'main')
                    ? record.get('channelValidationData')[channelId].mainValidationInfo
                    : record.get('channelValidationData')[channelId].bulkValidationInfo;
            }
        } else {
            if (record) {
                validationInfo = record.get(type + 'ValidationInfo');
            }
        }

        if (validationInfo && validationInfo.validationResult) {
            switch (validationInfo.validationResult.split('.')[1]) {
                case 'notValidated':
                    validationResultText = '(' + Uni.I18n.translate('devicechannelsreadings.validationResult.notvalidated', 'MDC', 'Not validated') + ')' +
                        '<span class="icon-flag6" style="margin-left:10px; display:inline-block; vertical-align:top;"></span>';
                    break;
                case 'suspect':
                    validationResultText = '(' + Uni.I18n.translate('devicechannelsreadings.validationResult.suspect', 'MDC', 'Suspect') + ')' +
                        '<span class="icon-flag5" style="margin-left:10px; display:inline-block; vertical-align:top; color:red;"></span>';
                    break;
                case 'ok':
                    validationResultText = '(' + Uni.I18n.translate('devicechannelsreadings.validationResult.notsuspect', 'MDC', 'Not suspect') + ')';
                    if (!me.channels && validationInfo.isConfirmed) {
                        validationResultText += '<span class="icon-checkmark" style="margin-left:5px; vertical-align:top;"></span>';
                    }
                    break;
            }
        }

        if (!Ext.isEmpty(value)) {
            formatValue = Uni.Number.formatNumber(
                value.toString(),
                me.channelRecord && !Ext.isEmpty(me.channelRecord.get('overruledNbrOfFractionDigits')) ? me.channelRecord.get('overruledNbrOfFractionDigits') : -1
            );
            return !Ext.isEmpty(formatValue) ? formatValue + ' ' + unitOfMeasure + ' ' + validationResultText : '';
        } else {
            if(type === 'main'){
                return Uni.I18n.translate('general.missingx', 'MDC', 'Missing {0}',[validationResultText], false);
            } else {
                return Uni.I18n.translate('general.missingx', 'MDC', 'Missing {0}',[validationResultText], false);
            }
        }
    },

    initComponent: function () {
        var me = this,
            generalItems = [],
            valuesItems = [];

        generalItems.push(
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'),
                name: 'interval',
                renderer: function (value) {
                    return value
                        ? Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',[Uni.DateTime.formatDateLong(new Date(value.start)),Uni.DateTime.formatTimeLong(new Date(value.start))])
                        + ' - ' +
                        Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',[Uni.DateTime.formatDateLong(new Date(value.end)),Uni.DateTime.formatTimeLong(new Date(value.end))])
                        : '';
                },
                htmlEncode: false
            },
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.readingTime', 'MDC', 'Reading time'),
                name: 'readingTime',
                renderer: function (value, field) {
                    return value ? Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',[Uni.DateTime.formatDateLong(new Date(value)), Uni.DateTime.formatTimeLong(new Date(value))]) : '';
                }
            },
            {
                xtype: 'interval-flags-displayfield',
                name: 'intervalFlags',
                renderer: function (value, field) {
                    if (Ext.isEmpty(value)) {
                        field.hide();
                    } else {
                        return value;
                    }
                }
            },
            {
                fieldLabel: Uni.I18n.translate('devicechannelsreadings.validationstatus.title', 'MDC', 'Validation status'),
                name: 'validationActive',
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.active', 'MDC', 'Active') :
                        Uni.I18n.translate('general.inactive', 'MDC', 'Inactive')
                }
            },
            {
                fieldLabel: Uni.I18n.translate('devicechannelsreadings.dataValidated.title', 'MDC', 'Data validated'),
                itemId: 'readingDataValidated',
                htmlEncode: false,
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No');
                }
            },
            {
                fieldLabel: Uni.I18n.translate('general.readingQualities', 'MDC', 'Reading qualities'),
                itemId: 'generalReadingQualities',
                htmlEncode: false
            },
            {
                fieldLabel: Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier'),
                name: 'multiplier',
                itemId: 'mdc-multiplier',
                hidden: true
            }
        );

        if (me.channels) {
            Ext.Array.each(me.channels, function (channel) {
                var calculatedReadingType = channel.calculatedReadingType,
                    channelName = !Ext.isEmpty(channel.calculatedReadingType)
                        ? channel.calculatedReadingType.fullAliasName
                        : channel.readingType.fullAliasName,
                    valueItem = {
                        xtype: 'fieldcontainer',
                        fieldLabel: channelName,
                        itemId: 'channelFieldContainer' + channel.id,
                        labelAlign: 'top',
                        labelWidth: 400,
                        layout: 'vbox',
                        margin: '20 0 0 0',
                        items: []
                    };

                valueItem.items.push(
                    {
                        fieldLabel: calculatedReadingType
                            ? Uni.I18n.translate('general.calculatedValue', 'MDC', 'Calculated value')
                            : Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value'),
                        xtype: 'displayfield',
                        labelWidth: 200,
                        itemId: 'channelValue' + channel.id,
                        renderer: function (value) {
                            return me.setValueWithResult(value, 'main', channel);
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('devicechannelsreadings.readingqualities.title', 'MDC', 'Reading qualities'),
                        xtype: 'displayfield',
                        labelWidth: 200,
                        itemId: (calculatedReadingType ? 'main' : 'bulk') + 'ReadingQualities' + channel.id,
                        htmlEncode: false
                    }
                );
                if (calculatedReadingType) {
                    valueItem.items.push(
                        {
                            fieldLabel: Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value'),
                            xtype: 'displayfield',
                            labelWidth: 200,
                            itemId: 'channelBulkValue' + channel.id,
                            renderer: function (value) {
                                return me.setValueWithResult(value, 'bulk', channel);
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.readingQualities', 'MDC', 'Reading qualities'),
                            xtype: 'displayfield',
                            labelWidth: 200,
                            itemId: 'bulkReadingQualities' + channel.id,
                            htmlEncode: false
                        }
                    );
                }
                valueItem.items.push(
                    {
                        fieldLabel: Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier'),
                        xtype: 'displayfield',
                        labelWidth: 200,
                        name: 'multiplier',
                        itemId: 'mdc-multiplier',
                        hidden: true
                    }
                );
                valuesItems.push(valueItem);
            });
        } else {
            var calculatedReadingType = me.channelRecord.get('calculatedReadingType');
            valuesItems.push(
                {
                    xtype: 'fieldcontainer',
                    labelWidth: 200,
                    fieldLabel: calculatedReadingType
                        ? Uni.I18n.translate('general.calculatedValue', 'MDC', 'Calculated value')
                        : Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value'),
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'displayfield',
                            name: 'value',
                            renderer: function (value) {
                                return me.setValueWithResult(value, 'main');
                            }
                        },
                        {
                            xtype: 'edited-displayfield',
                            name: 'mainModificationState',
                            margin: '0 0 0 10'
                        }
                    ]
                },
                {
                    xtype: 'reading-qualities-field',
                    labelWidth: 200,
                    name: 'mainValidationInfo',
                    router: me.router,
                    itemId: 'mainReadingQualities'
                }
            );
            if (calculatedReadingType) {
                valuesItems.push(
                    {
                        xtype: 'fieldcontainer',
                        labelWidth: 200,
                        fieldLabel: Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value'),
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'displayfield',
                                name: 'collectedValue',
                                renderer: function (value) {
                                    return me.setValueWithResult(value, 'bulk');
                                }
                            },
                            {
                                xtype: 'edited-displayfield',
                                name: 'bulkModificationState',
                                margin: '0 0 0 10'
                            }
                        ]
                    },
                    {
                        xtype: 'reading-qualities-field',
                        labelWidth: 200,
                        name: 'bulkValidationInfo',
                        router: me.router,
                        itemId: 'bulkReadingQualities'
                    }
                );
            }

            valuesItems.push(
                {
                    xtype: 'displayfield',
                    labelWidth: 200,
                    fieldLabel: Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier'),
                    name: 'multiplier',
                    itemId: 'mdc-multiplier',
                    hidden: true
                }
            );
        }
        me.items = [
            {
                title: Uni.I18n.translate('devicechannelsdata.generaltab.title', 'MDC', 'General'),
                items: {
                    xtype: 'form',
                    itemId: 'general-panel',
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
                title: Uni.I18n.translate('devicechannelsdata.readingvaluetab.title', 'MDC', 'Reading values'),
                items: {
                    xtype: 'form',
                    itemId: 'values-panel',
                    frame: true,
                    items: valuesItems,
                    layout: 'vbox'
                }
            }
        ];

        me.callParent(arguments);
    }
});