/**
 * @class Uni.form.field.OnPeriod
 */
Ext.define('Uni.form.field.OnPeriod', {
    extend: 'Ext.form.RadioGroup',
    xtype: 'uni-form-field-onperiod',

    fieldLabel: 'On',
    columns: 1,
    vertical: true,

    baseRadioName: undefined,

    initComponent: function () {
        var me = this;

        me.buildItems();
        me.callParent(arguments);

        me.on('afterrender', me.initListeners, me);
    },

    buildItems: function () {
        var me = this;

        me.baseRadioName = me.getId() + 'onperiod';

        me.items = [
            {
                boxLabel: 'Current day of the month',
                itemId: 'option-current',
                name: me.baseRadioName,
                inputValue: 'currentday',
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
                        name: me.baseRadioName,
                        inputValue: 'dayofmonth',
                        margin: '0 6 0 0'
                    },
                    {
                        xtype: 'label',
                        text: 'Day',
                        cls: Ext.baseCSSPrefix + 'form-cb-label'
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
                                    value: 31
                                });

                                return data;
                            })()
                        }),
                        allowBlank: false,
                        forceSelection: true
                    },
                    {
                        xtype: 'label',
                        text: 'of the month',
                        cls: Ext.baseCSSPrefix + 'form-cb-label'
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
                        name: me.baseRadioName,
                        inputValue: 'dayofweek'
                    },
                    {
                        xtype: 'combobox',
                        name: 'period-interval',
                        displayField: 'name',
                        valueField: 'value',
                        queryMode: 'local',
                        hideLabel: true,
                        value: 1,
                        width: 128,
                        margin: '0 6 0 6',
                        store: new Ext.data.Store({
                            fields: ['name', 'value'],
                            data: (function () {
                                // TODO Create a days of week store.
                                return [
                                    {name: 'Monday', value: 1},
                                    {name: 'Tuesday', value: 2},
                                    {name: 'Wednesday', value: 3},
                                    {name: 'Thursday', value: 4},
                                    {name: 'Friday', value: 5},
                                    {name: 'Saturday', value: 6},
                                    {name: 'Sunday', value: 7}
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

        me.getOptionCurrentRadio().on('change', function () {
            me.fireEvent('periodchange', me.getValue());
        }, me);

        me.getOptionDayOfMonthContainer().down('label:first').getEl().on('click', function () {
            if (!me.getOptionDayOfMonthRadio().isDisabled()) {
                me.selectOptionDayOfMonth();
            }
        }, me);

        me.getOptionDayOfMonthContainer().down('combobox').on('change', function () {
            me.selectOptionDayOfMonth();
        }, me);

        me.getOptionDayOfMonthContainer().down('label:last').getEl().on('click', function () {
            if (!me.getOptionDayOfMonthRadio().isDisabled()) {
                me.selectOptionDayOfMonth();
            }
        }, me);

        me.getOptionDayOfWeekContainer().down('combobox').on('change', function () {
            me.selectOptionDayOfWeek();
        }, me);
    },

    selectOptionCurrent: function (suspendEvent) {
        this.getOptionCurrentRadio().setValue(true);

        if (!suspendEvent) {
            this.fireEvent('change', this.getValue());
        }
    },

    selectOptionDayOfMonth: function (suspendEvent) {
        this.getOptionDayOfMonthRadio().setValue(true);

        if (!suspendEvent) {
            this.fireEvent('change', this.getValue());
        }
    },

    selectOptionDayOfWeek: function (suspendEvent) {
        this.getOptionDayOfWeekRadio().setValue(true);

        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getValue());
        }
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

    setOptionCurrentDisabled: function (disabled) {
        var me = this;

        me.getOptionCurrentRadio().setDisabled(disabled);

        me.selectAvailableOption();
    },

    setOptionDayOfMonthDisabled: function (disabled) {
        var me = this,
            radio = me.getOptionDayOfMonthRadio(),
            combo = me.getOptionDayOfMonthContainer().down('combobox');

        radio.setDisabled(disabled);
        combo.setDisabled(disabled);

        if (disabled) {
            me.getOptionDayOfMonthContainer().addCls(Ext.baseCSSPrefix + 'item-disabled');
        } else {
            me.getOptionDayOfMonthContainer().removeCls(Ext.baseCSSPrefix + 'item-disabled');
        }

        me.selectAvailableOption();
    },

    setOptionDayOfWeekDisabled: function (disabled) {
        var me = this,
            radio = me.getOptionDayOfWeekRadio(),
            combo = me.getOptionDayOfWeekContainer().down('combobox');

        radio.setDisabled(disabled);
        combo.setDisabled(disabled);

        me.selectAvailableOption();
    },

    selectAvailableOption: function () {
        var me = this,
            dayRadio = me.getOptionCurrentRadio(),
            monthRadio = me.getOptionDayOfMonthRadio(),
            weekRadio = me.getOptionDayOfWeekRadio();

        if (dayRadio.getValue() && dayRadio.isDisabled()) {
            monthRadio.setValue(true);
        }

        if (monthRadio.getValue() && monthRadio.isDisabled()) {
            weekRadio.setValue(true);
        }

        if (weekRadio.getValue() && weekRadio.isDisabled()) {
            dayRadio.setValue(true);
        }
    },

    getValue: function () {
        var me = this,
            selectedRadio = me.callParent(arguments),
            selectedValue = selectedRadio[me.baseRadioName],
            dayOfMonthValue = me.getOptionDayOfMonthContainer().down('combobox').getValue(),
            dayOfWeekValue = me.getOptionDayOfWeekContainer().down('combobox').getValue();

        return {
            onCurrentDay: selectedValue === 'currentday',
            onDayOfMonth: dayOfMonthValue,
            onDayOfWeek: dayOfWeekValue
        };
    }
});