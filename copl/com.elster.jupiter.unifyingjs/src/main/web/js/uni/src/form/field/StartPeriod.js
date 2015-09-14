/**
 * @class Uni.form.field.StartPeriod
 */
Ext.define('Uni.form.field.StartPeriod', {
    extend: 'Ext.form.RadioGroup',
    xtype: 'uni-form-field-startperiod',

    fieldLabel: Uni.I18n.translate('general.from', 'UNI', 'From'),
    columns: 1,
    vertical: true,

    baseRadioName: undefined,

    /**
     * @cfg showOptionNow
     *
     * Determines whether to show the now option, defaults to true.
     */
    showOptionNow: true,

    /**
     * @cfg showOptionDate
     *
     * Determines whether to show the custom date option, defaults to true.
     */
    showOptionDate: true,

    inputValueNow: 'now',
    inputValueAgo: 'ago',
    inputValueDate: 'date',

    lastTask: undefined,
    selectedValue: undefined,

    initComponent: function () {
        var me = this;

        me.buildItems();
        me.callParent(arguments);

        me.on('afterrender', me.initListeners, me);
    },

    buildItems: function () {
        var me = this;

        me.baseRadioName = me.getId() + 'startperiod';

        me.items = [];

        if (me.showOptionNow) {
            me.items.push({
                boxLabel: Uni.I18n.translate('form.field.startPeriod.optionNow.label', 'UNI', 'Now'),
                itemId: 'option-now',
                name: me.baseRadioName,
                inputValue: me.inputValueNow,
                margin: '0 0 6 0',
                value: true
            });
        }

        if (me.showOptionDate) {
            me.items.push({
                xtype: 'container',
                itemId: 'option-date',
                layout: 'hbox',
                margin: '0 0 6 0',
                name: 'rb',
                items: [
                    {
                        xtype: 'radio',
                        name: me.baseRadioName,
                        inputValue: me.inputValueDate
                    },
                    {
                        xtype: 'datefield',
                        name: 'start-date',
                        allowBlank: false,
                        editable: false,
                        value: new Date(),
                        maxValue: new Date(),
                        width: 128,
                        margin: '0 0 0 6',
                        format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                    }
                ]
            });
        }

        me.items.push({
            xtype: 'container',
            itemId: 'option-ago',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'radio',
                    name: me.baseRadioName,
                    inputValue: me.inputValueAgo,
                    value: !me.showOptionNow
                },
                {
                    xtype: 'numberfield',
                    name: 'frequency',
                    hideLabel: true,
                    value: 1,
                    minValue: 0,
                    editable: false,
                    allowBlank: false,
                    width: 64,
                    margin: '0 6 0 6'
                },
                {
                    xtype: 'combobox',
                    name: 'period-interval',
                    itemId: 'period-interval',
                    displayField: 'name',
                    valueField: 'value',
                    queryMode: 'local',
                    editable: false,
                    hideLabel: true,
                    value: 'months',
                    width: 200,
                    margin: '0 6 0 6',
                    store: Ext.create('Uni.store.Periods'),
                    allowBlank: false,
                    forceSelection: true
                },
                {
                    xtype: 'combobox',
                    name: 'period-edge',
                    itemId: 'period-edge',
                    displayField: 'name',
                    valueField: 'value',
                    queryMode: 'local',
                    editable: false,
                    hideLabel: true,
                    value: 'ago',
                    width: 110,
                    margin: '0 6 0 6',
                    store: new Ext.data.Store({
                        fields: ['name', 'value'],
                        data: (function () {
                            return [
                                {name: Uni.I18n.translate('general.period.ago', 'UNI', 'ago'), value: 'ago'},
                                {name: Uni.I18n.translate('general.period.ahead', 'UNI', 'ahead'), value: 'ahead'}
                            ];
                        })()
                    }),
                    allowBlank: false,
                    forceSelection: true
                }
            ]
        });
    },

    initListeners: function () {
        var me = this;

        if (me.showOptionNow) {
            me.selectedValue = 'now';

            me.getOptionNowRadio().on('change', function (scope, newValue, oldValue) {
                if (newValue) {
                    me.selectedValue = 'now';
                    me.fireEvent('periodchange', me.getStartValue());
                }
            }, me);
        } else {
            me.selectedValue = 'ago';
        }

        me.getOptionAgoRadio().on('change', function (scope, newValue, oldValue) {
            if (newValue) {
                me.selectedValue = 'ago';

                if (me.showOptionDate) {
                    me.getOptionDateRadio().suspendEvents();
                    me.getOptionDateRadio().setValue(false);
                    me.getOptionDateRadio().resumeEvents();
                }

                if (me.showOptionNow) {
                    me.getOptionNowRadio().suspendEvents();
                    me.getOptionNowRadio().setValue(false);
                    me.getOptionNowRadio().resumeEvents();
                }

                me.fireEvent('periodchange', me.getStartValue());
            }
        }, me);

        me.getOptionAgoContainer().down('numberfield').on('change', function () {
            if (me.lastTask) {
                me.lastTask.cancel();
            }

            me.lastTask = new Ext.util.DelayedTask(function () {
                me.selectOptionAgo();
            });

            me.lastTask.delay(256);
        }, me);

        me.getOptionAgoContainer().down('#period-interval').on('change', function () {
            me.selectOptionAgo();
        }, me);

        me.getOptionAgoContainer().down('#period-edge').on('change', function () {
            me.selectOptionAgo();
        }, me);

        if (me.showOptionDate) {
            me.getOptionDateRadio().on('change', function (scope, newValue, oldValue) {
                if (newValue) {
                    me.selectedValue = 'date';
                    me.fireEvent('periodchange', me.getStartValue());
                }
            }, me);

            me.getOptionDateContainer().down('datefield').on('change', function () {
                me.selectOptionDate();
            }, me);
        }
    },

    selectOptionNow: function (suspendEvent) {
        this.selectedValue = 'now';

        this.getOptionNowRadio().suspendEvents();
        this.getOptionNowRadio().setValue(true);
        this.getOptionNowRadio().resumeEvents();

        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getStartValue());
        }
    },

    selectOptionAgo: function (suspendEvent) {
        this.selectedValue = 'ago';

        this.getOptionAgoRadio().suspendEvents();
        this.getOptionAgoRadio().setValue(true);
        this.getOptionAgoRadio().resumeEvents();

        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getStartValue());
        }
    },

    selectOptionDate: function (suspendEvent) {
        this.selectedValue = 'date';

        this.getOptionDateRadio().suspendEvents();
        this.getOptionDateRadio().setValue(true);
        this.getOptionDateRadio().resumeEvents();

        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getStartValue());
        }
    },

    getOptionNowRadio: function () {
        return this.down('#option-now');
    },

    getOptionAgoRadio: function () {
        return this.getOptionAgoContainer().down('radio');
    },

    getOptionDateRadio: function () {
        return this.getOptionDateContainer().down('radio');
    },

    getOptionAgoContainer: function () {
        return this.down('#option-ago');
    },

    getOptionDateContainer: function () {
        return this.down('#option-date');
    },

    getStartValue: function () {
        var me = this,
            selectedValue = me.selectedValue,
            amountAgoValue = me.getOptionAgoContainer().down('numberfield').getValue(),
            freqAgoValue = me.getOptionAgoContainer().down('#period-interval').getValue(),
            timeMode = me.getOptionAgoContainer().down('#period-edge').getValue();

        var result = {
            startNow: selectedValue === 'now'
        };

        if (selectedValue === 'date') {
            var dateValue = me.getOptionDateContainer().down('datefield').getValue();

            var fixedDate = {
                startFixedDay: dateValue.getDate(),
                startFixedMonth: dateValue.getMonth() + 1,
                startFixedYear: dateValue.getFullYear()
            };
            Ext.apply(result, fixedDate);
        } else if (selectedValue === 'ago') {
            var shiftDate = {
                startAmountAgo: amountAgoValue,
                startPeriodAgo: freqAgoValue,
                startTimeMode: timeMode
            };
            Ext.apply(result, shiftDate);
        }

        return result;
    }
});