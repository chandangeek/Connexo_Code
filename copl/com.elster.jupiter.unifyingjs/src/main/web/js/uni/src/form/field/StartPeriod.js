/**
 * @class Uni.form.field.StartPeriod
 */
Ext.define('Uni.form.field.StartPeriod', {
    extend: 'Ext.form.RadioGroup',
    xtype: 'uni-form-field-startperiod',

    fieldLabel: 'From',
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
                boxLabel: 'Now',
                itemId: 'option-now',
                name: me.baseRadioName,
                inputValue: me.inputValueNow,
                margin: '0 0 6 0',
                value: true
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
                    minValue: 1,
                    allowBlank: false,
                    width: 64,
                    margin: '0 6 0 6'
                },
                {
                    xtype: 'combobox',
                    name: 'period-interval',
                    displayField: 'name',
                    valueField: 'value',
                    queryMode: 'local',
                    hideLabel: true,
                    value: 'month',
                    width: 200,
                    margin: '0 6 0 0',
                    store: new Ext.data.Store({
                        fields: ['name', 'value'],
                        data: (function () {
                            return [
                                {name: 'Month(s)', value: 'month'},
                                {name: 'Week(s)', value: 'week'},
                                {name: 'Day(s)', value: 'day'},
                                {name: 'Hour(s)', value: 'hour'},
                                {name: 'Minute(s)', value: 'minute'}
                            ];
                        })()
                    }),
                    allowBlank: false,
                    forceSelection: true
                },
                {
                    xtype: 'label',
                    text: 'ago',
                    cls: Ext.baseCSSPrefix + 'form-cb-label'
                }
            ]
        });

        if (me.showOptionDate) {
            me.items.push({
                xtype: 'container',
                itemId: 'option-date',
                layout: 'hbox',
                margin: '6 0 0 0',
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
                        value: new Date(),
                        maxValue: new Date(),
                        width: 128,
                        margin: '0 0 0 6'
                    }
                ]
            });
        }
    },

    initListeners: function () {
        var me = this;

        if (me.showOptionNow) {
            me.getOptionNowRadio().on('change', function () {
                me.fireEvent('periodchange', me.getValue());
            }, me);
        }

        me.getOptionAgoContainer().down('numberfield').on('change', function () {
            me.selectOptionAgo();
        }, me);

        me.getOptionAgoContainer().down('combobox').on('change', function () {
            me.selectOptionAgo();
        }, me);

        me.getOptionAgoContainer().down('label').getEl().on('click', function () {
            me.selectOptionAgo();
        }, me);

        if (me.showOptionDate) {
            me.getOptionDateContainer().down('datefield').on('change', function () {
                me.selectOptionDate();
            }, me);
        }
    },

    selectOptionNow: function (suspendEvent) {
        this.getOptionNowRadio().setValue(true);

        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getValue());
        }
    },

    selectOptionAgo: function (suspendEvent) {
        this.getOptionAgoRadio().setValue(true);

        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getValue());
        }
    },

    selectOptionDate: function (suspendEvent) {
        this.getOptionDateRadio().setValue(true);

        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getValue());
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

    getValue: function () {
        var me = this,
            selectedRadio = me.callParent(arguments),
            selectedValue = selectedRadio[me.baseRadioName],
            amountAgoValue = me.getOptionAgoContainer().down('numberfield').getValue(),
            freqAgoValue = me.getOptionAgoContainer().down('combobox').getValue();

        var result = {
            startAmountAgo: amountAgoValue,
            startPeriodAgo: freqAgoValue,
            startNow: selectedValue === 'now'
        };

        if (me.showOptionDate) {
            var dateValue = me.getOptionDateContainer().down('datefield').getValue();

            Ext.apply(result, {
                startFixedDay: dateValue.getDay(),
                startFixedMonth: dateValue.getMonth(),
                startFixedYear: dateValue.getFullYear()
            });
        }

        return result;
    }
});