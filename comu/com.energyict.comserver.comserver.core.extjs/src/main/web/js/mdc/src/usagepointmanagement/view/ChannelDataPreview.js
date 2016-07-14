Ext.define('Mdc.usagepointmanagement.view.ChannelDataPreview', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.channel-data-preview',

    router: null,
    channel: null,

    initComponent: function () {
        var me = this,
            readingType = me.channel.get('readingType'),
            unit = readingType && readingType.names ? readingType.names.unitOfMeasure : '',
            defaults = {
                xtype: 'displayfield',
                labelWidth: 200
            };

        me.items = [
            {
                title: Uni.I18n.translate('general.general', 'MDC', 'General'),
                items: {
                    itemId: 'general-tab',
                    xtype: 'form',
                    frame: true,
                    defaults: defaults,
                    items: [
                        {
                            itemId: 'interval-field',
                            fieldLabel: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
                            name: 'interval',
                            htmlEncode: false,
                            renderer: function (value) {
                                return value
                                    ? Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateLong(new Date(value.start)), Uni.DateTime.formatTimeLong(new Date(value.start))], false)
                                + ' - ' +
                                Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateLong(new Date(value.end)), Uni.DateTime.formatTimeLong(new Date(value.end))], false)
                                    : '-';
                            }
                        },
                        {
                            itemId: 'readingTime-field',
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.readingTime', 'MDC', 'Reading time'),
                            name: 'readingTime',
                            htmlEncode: false,
                            renderer: function (value) {
                                return value
                                    ? Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateLong(new Date(value)), Uni.DateTime.formatTimeLong(new Date(value))], false)
                                    : '-';
                            }
                        },
                        {
                            itemId: 'dataValidated-field',
                            fieldLabel: Uni.I18n.translate('device.registerData.dataValidated', 'MDC', 'Data validated'),
                            name: 'dataValidated',
                            renderer: function (value) {
                                return value
                                    ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                                    : '-';
                            }
                        },
                        {
                            itemId: 'validationResult-field',
                            fieldLabel: Uni.I18n.translate('device.dataValidation.validationResult', 'MDC', 'Validation result'),
                            name: 'validationResult',
                            renderer: function (value) {
                                return value
                                    ? value
                                    : '-';
                            }
                        }
                    ]
                }
            },
            {
                title: Uni.I18n.translate('general.readingValue', 'MDC', 'Reading value'),
                items: {
                    itemId: 'reading-value-tab',
                    xtype: 'form',
                    frame: true,
                    defaults: defaults,
                    items: [
                        {
                            itemId: 'value-field',
                            fieldLabel: Uni.I18n.translate('general.value', 'MDC', 'Value'),
                            name: 'value',
                            renderer: function (value) {
                                return value
                                    ? value + ' ' + unit
                                    : '-';
                            }
                        },
                        {
                            itemId: 'validationRules-field',
                            fieldLabel: Uni.I18n.translate('general.readingQualities', 'MDC', 'Reading qualities'),
                            name: 'validationResult',
                            renderer: function (value) {
                                return value
                                    ? value
                                    : '-';
                            }
                        }
                    ]
                }
            }
        ];

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this,
            interval = record.get('interval'),
            title = Uni.I18n.translate(
                'general.dateAtTime', 'MDC', '{0} at {1}',
                [Uni.DateTime.formatDateLong(new Date(interval.end)), Uni.DateTime.formatTimeLong(new Date(interval.end))], false);

        Ext.suspendLayouts();
        me.down('#validationRules-field').setVisible(!Ext.isEmpty(record.get('validationRules')));
        Ext.Array.each(me.query('form'), function (form) {
            form.setTitle(title);
            form.loadRecord(me.prepareDataForDisplay(record));
        });
        Ext.resumeLayouts(true);
    },

    prepareDataForDisplay: function (record) {
        record.beginEdit();

        record.endEdit();

        return record;
    }
});