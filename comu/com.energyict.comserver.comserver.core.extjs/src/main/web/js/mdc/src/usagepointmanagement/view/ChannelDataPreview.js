Ext.define('Mdc.usagepointmanagement.view.ChannelDataPreview', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.channel-data-preview',
    requires: [
        'Cfg.view.field.ReadingQualities'
    ],

    router: null,
    channel: null,

    initComponent: function () {
        var me = this,
            defaults = {
                xtype: 'displayfield',
                labelWidth: 200
            };

        me.unit = me.channel.get('readingType').names.unitOfMeasure;

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
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.lastUpdate', 'MDC', 'Last update'),
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
                                    : Uni.I18n.translate('general.no', 'MDC', 'No');
                            }
                        },
                        {
                            itemId: 'validationResult-field',
                            fieldLabel: Uni.I18n.translate('device.dataValidation.validationResult', 'MDC', 'Validation result'),
                            name: 'validationResult',
                            renderer: function () {
                                return me.displayValidationResult(false);
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
                                return me.displayValidationResult(true, value);
                            }
                        }/*,
                        {
                            xtype: 'reading-qualities-field',
                            itemId: 'validationRules-field',
                            fieldLabel: Uni.I18n.translate('general.readingQualities', 'MDC', 'Reading qualities'),
                            name: 'validationRules',
                            router: me.router
                         }*/
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

        me.record = record;
        Ext.suspendLayouts();
        Ext.Array.each(me.query('form'), function (form) {
            form.setTitle(title);
            form.loadRecord(record);
        });
        Ext.resumeLayouts(true);
    },

    displayValidationResult: function (showValue, value) {
        if (!this.record) {
            return '-'
        }
        var me = this,
            result,
            validation = me.record.get('validation'),
            flags = {
                NOT_VALIDATED: {
                    icon: '<span class="icon-flag6"></span>',
                    text: Uni.I18n.translate('devicechannelsreadings.validationResult.notvalidated', 'MDC', 'Not validated')
                },
                SUSPECT: {
                    icon: '<span class="icon-flag5" style="color:red"></span>',
                    text: Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect')
                },
                INFORMATIVE: {
                    icon: '<span class="icon-flag5" style="color:yellow"></span>',
                    text: Uni.I18n.translate('validationStatus.informative', 'MDC', 'Informative')
                },
                OK: {
                    icon: '',
                    text: Uni.I18n.translate('general.notSuspect', 'MDC', 'Not suspect')
                },
                NO_LINKED_DEVICES: {
                    icon: '<span class="icon-flag5" style="color:#686868"></span>',
                    text: Uni.I18n.translate('validationStatus.noLinkedDevices', 'MDC', 'No linked devices')
                }
            };

        if (showValue) {
            result = (!Ext.isEmpty(value) ? value + ' ' + me.unit + ' ' : '')
                + '(' + flags[validation].text + ')'
                + ' ' + flags[validation].icon;
        } else {
            result = flags[validation].text + ' ' + flags[validation].icon;
        }

        return result;
    }
});