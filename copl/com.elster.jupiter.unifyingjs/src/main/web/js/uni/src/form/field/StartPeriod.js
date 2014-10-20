/**
 * @class Uni.form.field.StartPeriod
 */
Ext.define('Uni.form.field.StartPeriod', {
    extend: 'Ext.form.RadioGroup',
    xtype: 'uni-form-field-startperiod',

    fieldLabel: 'Start',
    columns: 1,
    vertical: true,

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

    initComponent: function () {
        var me = this;

        me.buildItems();
        me.callParent(arguments);
        me.initListeners();
    },

    buildItems: function () {
        var me = this,
            baseRadioName = me.getId() + 'startperiod';

        me.items = [];

        if (me.showOptionNow) {
            me.items.push({
                boxLabel: 'Now',
                itemId: 'option-now',
                name: baseRadioName,
                inputValue: '1',
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
                    name: baseRadioName,
                    inputValue: '2',
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
                    value: 1,
                    width: 200,
                    margin: '0 6 0 0',
                    store: new Ext.data.Store({
                        fields: ['name', 'value'],
                        data: (function () {
                            return [
                                {name: 'Month(s)', value: 1},
                                {name: 'Week(s)', value: 2},
                                {name: 'Day(s)', value: 3},
                                {name: 'Hour(s)', value: 4},
                                {name: 'Minute(s)', value: 5}
                            ];
                        })()
                    }),
                    allowBlank: false,
                    forceSelection: true
                },
                {
                    xtype: 'label',
                    text: 'ago'
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
                        name: baseRadioName,
                        inputValue: '3'
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

        me.getOptionAgoContainer().down('numberfield').on('change', me.selectOptionAgo, me);
        me.getOptionAgoContainer().down('combobox').on('change', me.selectOptionAgo, me);

        if (me.showOptionDate) {
            me.getOptionDateContainer().down('datefield').on('change', me.selectOptionDate, me);
        }
    },

    selectOptionNow: function () {
        this.getOptionNowRadio().setValue(true);
    },

    selectOptionAgo: function () {
        this.getOptionAgoRadio().setValue(true);
    },

    selectOptionDate: function () {
        this.getOptionDateRadio().setValue(true);
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
        // TODO Return the value as the selected type and a date.
        return new Date();
    }
});