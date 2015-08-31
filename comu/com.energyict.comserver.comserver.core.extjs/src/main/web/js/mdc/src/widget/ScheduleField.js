Ext.define('Mdc.widget.ScheduleField', {
    extend: 'Ext.form.FieldContainer',
    requires: [
        'Mdc.store.TimeUnits'
    ],
    stores: [
        'TimeUnits'
    ],
    mixins: {
        field: 'Ext.form.field.Field'
    },
    alias: 'widget.scheduleField',
    layout: {
        type: 'hbox',
        flex: 1,
        defaultMargins: {
            top: 0,
            right: 5,
            bottom: 0,
            left: 0
        }
    },
    msgTarget: 'under',
    submitFormat: 'c',

    valueCfg: null,
    unitCfg: null,
    hourCfg: null,
    minuteCfg: null,
    secondCfg: null,

    initComponent: function () {
        var me = this;
        if (!me.valueCfg) {
            me.valueCfg = {};
        }
        if (!me.unitCfg) {
            me.unitCfg = {};
        }
        if (!me.hourCfg) {
            me.hourCfg = {};
        }
        if (!me.minuteCfg) {
            me.minuteCfg = {};
        }
        if (!me.secondCfg) {
            me.secondCfg = {};
        }

        me.buildField();
        me.callParent(arguments);
        me.valueField = me.down('#valueField');
        me.unitField = me.down('#unitField');
        me.hourField = me.down('#hourField');
        me.minuteField = me.down('#minuteField');
        me.secondField = me.down('#secondField');
        me.dayField = me.down('#dayField');
        me.dayIndexField = me.down('#dayIndexField');
        me.offSetSeparator = me.down('#offSetSeparator');
        me.secondLabel = me.down('#secondLabel');
        me.minuteLabel = me.down('#minuteLabel');
        me.dayFieldSeparator = me.down('#dayFieldSeparator');
        me.initField();
    },

    //@private
    buildField: function () {
        var timeUnits = Ext.create('Mdc.store.TimeUnits');
        var me = this;
        me.items = [
            Ext.apply({
                xtype: 'combobox',
                itemId: 'valueField',
                submitValue: false,
                width: 70,
                store: new Ext.data.SimpleStore({
                    data: [
                        [1, 1],
                        [2, 2],
                        [3, 3],
                        [4, 4],
                        [5, 5],
                        [6, 6],
                        [10, 10],
                        [12, 12],
                        [15, 15],
                        [20, 20],
                        [30, 30]
                    ],
                    id: 0,
                    fields: ['valueFieldKey', 'translation']
                }),
                queryMode: 'local',
                displayField: 'translation',
                valueField: 'valueFieldKey',
                submitValue: false,
                forceSelection: true,
                editable: false,

                listeners: {
                    //  blur: me.numberFieldValidation,
                    change: {
                        fn: me.onItemChange,
                        scope: me
                    }
                }
            }, me.valueCfg),
            Ext.apply({
                xtype: 'combobox',
                itemId: 'unitField',
                store: new Ext.data.SimpleStore({
                    data: [
                        ['minutes', Uni.I18n.translate('schedulefield.minutes', 'MDC', 'minute(s)')],
                        ['hours', Uni.I18n.translate('schedulefield.hours', 'MDC', 'hour(s)')],
                        ['days', Uni.I18n.translate('schedulefield.days', 'MDC', 'day(s)')],
                        ['weeks', Uni.I18n.translate('schedulefield.weeks', 'MDC', 'week(s)')],
                        ['months', Uni.I18n.translate('schedulefield.months', 'MDC', 'month(s)')]
                    ],
                    id: 0,
                    fields: ['timeUnitKey', 'translation']
                }),
                queryMode: 'local',
                displayField: 'translation',
                valueField: 'timeUnitKey',
                submitValue: false,
                forceSelection: true,
                editable: false,
                listeners: {
                    change: {
                        fn: function (combo, newValue) {
                            me.clearOffsetValues();
                            me.adjustOffsetGui(newValue);
                            me.adjustValueFieldGui(newValue);
                            me.clearValueField();
                            me.onItemChange();
                            me.fireEvent('schedulefieldupdated');
                        },
                        scope: me
                    }
                },
                width: 100
            }, me.unitCfg),
            Ext.apply({
                xtype: 'displayfield',
                itemId: 'offSetSeparator',
                submitValue: false,
                hidden: true,
                msgTarget: 'none'
            }),
            Ext.apply({
                xtype: 'combobox',
                itemId: 'dayField',
                store: new Ext.data.SimpleStore({
                    data: [
                        [1, 'monday', Uni.I18n.translate('general.daysofweek.monday', 'MDC', 'Monday')],
                        [2, 'tuesday', Uni.I18n.translate('general.daysofweek.tuesday', 'MDC', 'Tuesday')],
                        [3, 'wednesday', Uni.I18n.translate('general.daysofweek.wednesday', 'MDC', 'Wednesday')],
                        [4, 'thursday', Uni.I18n.translate('general.daysofweek.thursday', 'MDC', 'Thursday')],
                        [5, 'friday', Uni.I18n.translate('general.daysofweek.friday', 'MDC', 'Friday')],
                        [6, 'saturday', Uni.I18n.translate('general.daysofweek.saturday', 'MDC', 'Saturday')],
                        [7, 'sunday', Uni.I18n.translate('general.daysofweek.sunday', 'MDC', 'Sunday')]
                    ],
                    id: 0,
                    fields: ['dayId', 'dayKey', 'translation']
                }),
                queryMode: 'local',
                displayField: 'translation',
                valueField: 'dayId',
                submitValue: false,
                forceSelection: true,
                editable: false,
                hidden: true,
                width: 100,
                listeners: {
                    change: {
                        fn: me.onItemChange,
                        scope: me
                    }
                }
            }, me.unitCfg),
            Ext.apply({
                xtype: 'combobox',
                itemId: 'dayIndexField',
                store: new Ext.data.SimpleStore({
                    data: [
                        [1, 1],
                        [2, 2],
                        [3, 3],
                        [4, 4],
                        [5, 5],
                        [6, 6],
                        [7, 7],
                        [8, 8],
                        [9, 9],
                        [10, 10],
                        [11, 11],
                        [12, 12],
                        [13, 13],
                        [14, 14],
                        [15, 15],
                        [16, 16],
                        [17, 17],
                        [18, 18],
                        [19, 19],
                        [20, 20],
                        [21, 21],
                        [22, 22],
                        [23, 23],
                        [24, 24],
                        [25, 25],
                        [26, 26],
                        [27, 27],
                        [28, 28],
                        ['last', Uni.I18n.translate('schedulefield.last', 'MDC', 'last')]
                    ],
                    id: 0,
                    fields: ['dayIndexKey', 'translation']
                }),
                queryMode: 'local',
                displayField: 'translation',
                valueField: 'dayIndexKey',
                submitValue: false,
                forceSelection: true,
                editable: false,
                hidden: true,
                width: 100,
                listeners: {
                    change: {
                        fn: me.onItemChange,
                        scope: me
                    }
                }
            }, me.unitCfg),
            Ext.apply({
                xtype: 'displayfield',
                itemId: 'dayFieldSeparator',
                submitValue: false,
                hidden: true,
                msgTarget: 'none'
            }),
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'hourField',
                maxValue: 23,
                minValue: 0,
                allowDecimals: false,
                listeners: {
                    blur: me.numberFieldValidation,
                    change: {
                        fn: me.onItemChange,
                        scope: me
                    }
                },
                submitValue: false,
                hidden: true
            }, me.hourCfg),
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'minuteField',
                maxValue: 59,
                minValue: 0,
                allowDecimals: false,
                listeners: {
                    blur: me.numberFieldValidation,
                    change: {
                        fn: me.onItemChange,
                        scope: me
                    }
                },
                submitValue: false,
                hidden: true
            }, me.minuteCfg),
            Ext.apply({
                xtype: 'displayfield',
                itemId: 'minuteLabel',
                submitValue: false,
                hidden: true,
                value: Uni.I18n.translate('schedulefield.minutes','MDC','minute(s)'),
                msgTarget: 'none'
            }),
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'secondField',
                maxValue: 59,
                minValue: 0,
                allowDecimals: false,
                listeners: {
                    blur: me.numberFieldValidation,
                    change: {
                        fn: me.onItemChange,
                        scope: me
                    }
                },
                submitValue: false,
                hidden: true
            }, me.secondCfg),
            Ext.apply({
                xtype: 'displayfield',
                itemId: 'secondLabel',
                submitValue: false,
                hidden: true,
                value: Uni.I18n.translate('schedulefiled.seconds','MDC','second(s)'),
                msgTarget: 'none'
            })

        ]
    },

    numberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        } else if (value > field.maxValue) {
            field.setValue(field.maxValue);
        }
    },

    adjustOffsetGui: function (newValue) {
        var me = this;
        me.hideOffsetGui();
        switch (newValue) {
            case 'minutes':
                me.offSetSeparator.setValue(Uni.I18n.translate('schedulefield.at', 'MDC', 'at'));
                me.offSetSeparator.show();
                me.secondField.show();
                me.secondLabel.show();
                break;
            case 'hours':
                me.offSetSeparator.setValue(Uni.I18n.translate('schedulefield.at', 'MDC', 'at'));
                me.offSetSeparator.show();
                me.minuteField.show();
                me.minuteLabel.show();
                me.secondField.show();
                me.secondLabel.show();
                break;
            case 'days':
                me.offSetSeparator.setValue(Uni.I18n.translate('schedulefield.at', 'MDC', 'at'));
                me.offSetSeparator.show();
                me.hourField.show();
                me.minuteField.show();
                me.secondField.show();
                break;
            case 'weeks':
                me.offSetSeparator.setValue(Uni.I18n.translate('schedulefield.on', 'MDC', 'on'));
                me.offSetSeparator.show();
                me.dayField.show();
                me.dayFieldSeparator.setValue(Uni.I18n.translate('schedulefield.at', 'MDC', 'at'));
                me.dayFieldSeparator.show();
                me.hourField.show();
                me.minuteField.show();
                me.secondField.show();
                break;
            case 'months':
                me.offSetSeparator.setValue(Uni.I18n.translate('schedulefield.onDay', 'MDC', 'on day'));
                me.offSetSeparator.show();
                me.dayIndexField.show();
                me.dayFieldSeparator.setValue(Uni.I18n.translate('schedulefield.at', 'MDC', 'at'));
                me.dayFieldSeparator.show();
                me.hourField.show();
                me.minuteField.show();
                me.secondField.show();
        }
    },

    adjustValueFieldGui: function (newValue) {
        var me = this;
        switch (newValue) {
            case 'minutes':
                me.valueField.getStore().loadData([
                        [1, 1],
                        [2, 2],
                        [3, 3],
                        [4, 4],
                        [5, 5],
                        [6, 6],
                        [10, 10],
                        [12, 12],
                        [15, 15],
                        [20, 20],
                        [30, 30]
                    ]);
                break;
            case 'hours':
                me.valueField.getStore().loadData([
                        [1, 1],
                        [2, 2],
                        [3, 3],
                        [4, 4],
                        [6, 6],
                        [8, 8],
                        [12, 12]
                    ]);
                break;
            case 'days':
                me.valueField.getStore().loadData([
                        [1, 1]
                    ]);
                break;
            case 'weeks':
                me.valueField.getStore().loadData([
                        [1, 1]
                    ]);
                break;
            case 'months':
                me.valueField.getStore().loadData([
                        [1, 1],
                        [2, 2],
                        [3, 3],
                        [4, 4],
                        [6, 6]
                    ]);
        };

        var count = me.valueField.getStore().getCount();

        me.valueField.setHideTrigger(count == 1);
        me.valueField.setReadOnly(count == 1);
    },

    clear: function () {
        var me = this;

        me.clearConnectionScheduleValues();
        me.hideOffsetGui();
        me.clearOffsetValues();
    },

    clearOnlyOffsetValues: function () {
        var me = this;

        me.hideOffsetGui();
        me.clearOffsetValues();
    },

    clearOffsetValues: function () {
        var me = this;

        me.hourField.setValue(0);
        me.minuteField.setValue(0);
        me.secondField.setValue(0);
        me.dayField.setValue(1);
        me.dayIndexField.setValue(1);
    },

    clearValueField: function () {
        var me = this;
        me.setFieldValue(me.valueField);
    },

    clearConnectionScheduleValues: function () {
        var me = this;
        me.valueField.setValue(0);
        me.unitField.setValue(0);
    },

    setFieldValue: function (field) {
        var store = field.getStore(),
            count = store.getCount();

        if (store.find('valueFieldKey', field.getValue()) != -1) {
            field.setValue(field.getValue());
        }
        else{
            if (count == 0) {
                field.clearValue();
            }else {
                field.setValue(store.getAt(0).data.valueFieldKey);
            }
        }
        field.setHideTrigger(count == 1);
        field.setReadOnly(count == 1);
    },


    hideOffsetGui: function () {
        var me = this;

        me.hourField.hide();
        me.minuteField.hide();
        me.secondField.hide();
        me.dayField.hide();
        me.dayIndexField.hide();
        me.offSetSeparator.hide();
        me.secondLabel.hide();
        me.minuteLabel.hide();
        me.dayFieldSeparator.hide();
    },

    getValue: function () {
        var offSet = {timeUnit: 'seconds', count: 0};

        var everyValue = parseInt(this.valueField.getSubmitValue()) || 0;
        var timeUnit = this.unitField.getSubmitValue() || 'seconds';

        switch (timeUnit) {
            case 'minutes':
                offSet.count = this.secondField.getValue();
                break;
            case 'hours':
                offSet.count = this.minuteField.getValue() * 60 + this.secondField.getValue();
                break;
            case 'days':
                offSet.count = this.hourField.getValue() * 3600 + this.minuteField.getValue() * 60 + this.secondField.getValue();
                break;
            case 'weeks':
                offSet.count = (this.dayField.getValue() - 1) * 86400 + this.hourField.getValue() * 3600 + this.minuteField.getValue() * 60 + this.secondField.getValue();
                break;
            case 'months':
                var day = this.dayIndexField.getSubmitValue() === Uni.I18n.translate('schedulefield.last', 'MDC', 'last');
                if (day) {
                    offSet.count = this.hourField.getValue() * 3600 + this.minuteField.getValue() * 60 + this.secondField.getValue();
                } else {
                    offSet.count = (this.dayIndexField.getValue() - 1) * 86400 + this.hourField.getValue() * 3600 + this.minuteField.getValue() * 60 + this.secondField.getValue();
                }
        }


        return {
            every: {
                count: everyValue,
                timeUnit: timeUnit
            },
            offset: offSet,
            lastDay: day
        };
    },

    setValue: function (schedule) {
        var me = this;

        me.eachItem(function (item) {
            item.suspendEvent('change');
        });

        if (schedule) {
            me.valueField.setValue(schedule.every.count);
            me.unitField.setValue(schedule.every.timeUnit);
            me.clearOffsetValues();
            me.adjustOffsetGui(schedule.every.timeUnit);

            var offSet = schedule.offset;
            me.clearOffsetValues();
            switch (schedule.every.timeUnit) {
                case 'minutes':
                    me.secondField.setValue(offSet.count);
                    break;
                case 'hours':
                    me.minuteField.setValue(Math.floor(offSet.count / 60));
                    me.secondField.setValue(offSet.count % 60);
                    break;
                case 'days':
                    me.hourField.setValue(Math.floor(offSet.count / 3600));
                    me.minuteField.setValue(Math.floor((offSet.count % 3600) / 60));
                    me.secondField.setValue((offSet.count % 3600) % 60);
                    break;
                case 'weeks':
                    me.dayField.setValue((Math.floor(offSet.count / 86400)) + 1);
                    me.hourField.setValue(Math.floor((offSet.count % 86400) / 3600));
                    me.minuteField.setValue(Math.floor(((offSet.count % 86400) % 3600) / 60));
                    me.secondField.setValue(((offSet.count % 86400) % 3600) % 60);
                    break;
                case 'months':
                    if (schedule.lastDay === true) {
                        me.dayIndexField.setValue(Uni.I18n.translate('schedulefield.last', 'MDC', 'last'));
                    } else {
                        me.dayIndexField.setValue((Math.floor(offSet.count / 86400)) + 1);
                        me.hourField.setValue(Math.floor((offSet.count % 86400) / 3600));
                        me.minuteField.setValue(Math.floor(((offSet.count % 86400) % 3600) / 60));
                        me.secondField.setValue(((offSet.count % 86400) % 3600) % 60);
                    }
                    break;
            }
            me.adjustOffsetGui(schedule.every.timeUnit);
            me.adjustValueFieldGui(schedule.every.timeUnit);
        }

        me.fireEvent('change', me, schedule);

        me.eachItem(function (item) {
            item.resumeEvent('change');
        });
    },

    getSubmitData: function () {
        var me = this,
            data = null;
        if (!me.disabled && me.submitValue && !me.isFileUpload()) {
            data = {};
            value = me.getValue();
            data[me.getName()] = '' + value ? value : null;
        }
        return data;
    },

    markInvalid: function (fields) {
        this.eachItem(function (field) {
            field.markInvalid('');
        });
        this.items.items[0].markInvalid(fields);
    },

    eachItem: function (fn, scope) {
        if (this.items && this.items.each) {
            this.items.each(fn, scope || this);
        }
    },

    onItemChange: function () {
        this.fireEvent('change', this, this.getValue());
    }
});
