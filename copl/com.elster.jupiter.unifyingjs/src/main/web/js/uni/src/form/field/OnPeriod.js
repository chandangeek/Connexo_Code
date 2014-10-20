/**
 * @class Uni.form.field.OnPeriod
 */
Ext.define('Uni.form.field.OnPeriod', {
    extend: 'Ext.form.RadioGroup',
    xtype: 'uni-form-field-onperiod',

    fieldLabel: 'On',
    columns: 1,
    vertical: true,

    initComponent: function () {
        var me = this;

        me.buildItems();
        me.callParent(arguments);
        me.initListeners();
    },

    buildItems: function () {
        var me = this,
            baseRadioName = me.getId() + 'onperiod';

        me.defaults = {
            margin: '6 0 6 0'
        };

        me.items = [
            {
                boxLabel: 'Current day of the month',
                itemId: 'option-current',
                name: baseRadioName,
                inputValue: '1',
                margin: '0 0 6 0',
                value: true
            },
            {
                xtype: 'container',
                itemId: 'option-dom',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'radio',
                        name: baseRadioName,
                        inputValue: '2',
                        margin: '0 6 0 0'
                    },
                    {
                        xtype: 'label',
                        text: 'Day'
                    },
                    {
                        xtype: 'combobox',
                        name: 'period-interval',
                        displayField: 'name',
                        valueField: 'value',
                        queryMode: 'local',
                        hideLabel: true,
                        value: 1,
                        width: 64,
                        margin: '0 6 0 6',
                        store: new Ext.data.Store({
                            fields: ['name', 'value'],
                            data: (function () {
                                var data = [];

                                for (var i = 1; i < 29; i++) {
                                    data.push({
                                        name: i,
                                        value: i
                                    });
                                }

                                data.push({
                                    name: 'Last',
                                    value: 'last'
                                });

                                return data;
                            })()
                        }),
                        allowBlank: false,
                        forceSelection: true
                    },
                    {
                        xtype: 'label',
                        text: 'of the month'
                    }
                ]
            },
            {
                xtype: 'container',
                itemId: 'option-dow',
                layout: 'hbox',
                margin: '6 0 0 0',
                items: [
                    {
                        xtype: 'radio',
                        name: baseRadioName,
                        inputValue: '3'
                    },
                    {
                        xtype: 'combobox',
                        name: 'period-interval',
                        displayField: 'name',
                        valueField: 'value',
                        queryMode: 'local',
                        hideLabel: true,
                        value: 0,
                        width: 128,
                        margin: '0 6 0 6',
                        store: new Ext.data.Store({
                            fields: ['name', 'value'],
                            data: (function () {
                                // TODO Create a days of week store.
                                return [
                                    {name: 'Monday', value: 0},
                                    {name: 'Tuesday', value: 1},
                                    {name: 'Wednesday', value: 2},
                                    {name: 'Thursday', value: 3},
                                    {name: 'Friday', value: 4},
                                    {name: 'Saturday', value: 5},
                                    {name: 'Sunday', value: 6}
                                ];
                            })()
                        }),
                        allowBlank: false,
                        forceSelection: true
                    }
                ]
            }
        ];
    },

    initListeners: function () {
        var me = this;

        me.getOptionDayOfMonthContainer().down('combobox').on('change', me.selectOptionDayOfMonth, me);
        me.getOptionDayOfWeekContainer().down('combobox').on('change', me.selectOptionDayOfWeek, me);
    },

    selectOptionCurrent: function () {
        this.getOptionCurrentRadio().setValue(true);
    },

    selectOptionDayOfMonth: function () {
        this.getOptionDayOfMonthRadio().setValue(true);
    },

    selectOptionDayOfWeek: function () {
        this.getOptionDayOfWeekRadio().setValue(true);
    },

    getOptionCurrentRadio: function () {
        return this.down('#option-current');
    },

    getOptionDayOfMonthRadio: function () {
        return this.getOptionDayOfMonthContainer().down('radio');
    },

    getOptionDayOfWeekRadio: function () {
        return this.getOptionDayOfWeekContainer().down('radio');
    },

    getOptionDayOfMonthContainer: function () {
        return this.down('#option-dom');
    },

    getOptionDayOfWeekContainer: function () {
        return this.down('#option-dow');
    },

    getValue: function () {
        // TODO Return the value as the selected type and a date.
        return new Date();
    }
});