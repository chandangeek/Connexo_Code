/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.form.field.OnPeriod
 */
Ext.define('Uni.form.field.OnPeriod', {
    extend: 'Ext.form.RadioGroup',
    xtype: 'uni-form-field-onperiod',

    fieldLabel: Uni.I18n.translate('form.field.onPeriod.label', 'UNI', 'On'),
    columns: 1,
    vertical: true,

    baseRadioName: undefined,
    selectedValue: undefined,
    dom: false,

    initComponent: function () {
        var me = this;

        me.buildItems();
        me.callParent(arguments);

        me.on('afterrender', me.initListeners, me);
    },

    buildItems: function () {
        var me = this;

        me.baseRadioName = me.getId() + 'onperiod';
        me.selectedValue = 'currentday';

        me.items = [
            {
                boxLabel: Uni.I18n.translate('form.field.onPeriod.optionCurrent.label', 'UNI', 'Current day of the month'),
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
                        text: Uni.I18n.translate('form.field.onPeriod.optionDayOfMonth.day', 'UNI', 'Day'),
                        cls: Ext.baseCSSPrefix + 'form-item-label',
                        style: {
                            fontWeight: 'normal'
                        }
                    },
                    {
                        xtype: 'combobox',
                        name: 'period-interval',
                        displayField: 'name',
                        valueField: 'value',
                        queryMode: 'local',
                        editable: false,
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
                                    name: Uni.I18n.translate('form.field.onPeriod.optionDayOfMonth.day.last', 'UNI', 'Last'),
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
                        text: Uni.I18n.translate('form.field.onPeriod.optionDayOfMonth.month', 'UNI', 'of the month'),
                        cls: Ext.baseCSSPrefix + 'form-item-label',
                        style: {
                            fontWeight: 'normal'
                        }
                    }
                ]
            },
            {
                xtype: 'container',
                itemId: 'option-dow',
                layout: 'hbox',
                margin: '0 0 0 0',
                items: [
                    {
                        xtype: 'radio',
                        name: me.baseRadioName,
                        inputValue: 'dayofweek'
                    },
                    {
                        xtype: 'combobox',
                        itemId: 'option-dow-combo',
                        name: 'period-interval',
                        displayField: 'name',
                        valueField: 'value',
                        queryMode: 'local',
                        editable: false,
                        hideLabel: true,
                        value: 1,
                        width: 128,
                        margin: '0 6 0 6',
                        store: new Ext.data.Store({
                            fields: ['name', 'value'],
                            data: (function () {
                                // TODO Create a days of week store.
                                return [
                                    {name: Uni.I18n.translate('general.day.monday', 'UNI', 'Monday'), value: 1},
                                    {name: Uni.I18n.translate('general.day.tuesday', 'UNI', 'Tuesday'), value: 2},
                                    {name: Uni.I18n.translate('general.day.wednesday', 'UNI', 'Wednesday'), value: 3},
                                    {name: Uni.I18n.translate('general.day.thursday', 'UNI', 'Thursday'), value: 4},
                                    {name: Uni.I18n.translate('general.day.friday', 'UNI', 'Friday'), value: 5},
                                    {name: Uni.I18n.translate('general.day.saturday', 'UNI', 'Saturday'), value: 6},
                                    {name: Uni.I18n.translate('general.day.sunday', 'UNI', 'Sunday'), value: 7}
                                ];
                            })()
                        }),
                        allowBlank: false,
                        forceSelection: true
                    }
                ]
            },
            {
                boxLabel: Uni.I18n.translate('form.field.onPeriod.optionCurrentOfYear.label', 'UNI', 'Current day of the year'),
                itemId: 'option-current-of-year',
                name: me.baseRadioName,
                inputValue: 'currentdayofyear',
                margin: '0 0 6 0'
            },
            {
                xtype: 'container',
                itemId: 'option-doy',
                layout: {
                    type: 'hbox'
                },
                items: [
                    {
                        xtype: 'radio',
                        name: me.baseRadioName,
                        inputValue: 'dayofyear',
                        margin: '0 6 0 0'
                    },
                    {
                        xtype: 'label',
                        text: Uni.I18n.translate('form.field.onPeriod.optionDayOfMonth.day', 'UNI', 'Day'),
                        cls: Ext.baseCSSPrefix + 'form-item-label',
                        style: {
                            fontWeight: 'normal'
                        }
                    },
                    {
                        xtype: 'numberfield',
                        name: 'period-day-interval',
                        hideLabel: true,
                        value: 1,
                        minValue: 1,
                        maxValue: 31,
                        width: 64,
                        allowBlank: false,
                        margin: '0 6 0 6'
                    },
                    {
                        xtype: 'label',
                        text: Uni.I18n.translate('form.field.onPeriod.optionDayOfMonth.month', 'UNI', 'of the month'),
                        cls: Ext.baseCSSPrefix + 'form-item-label',
                        style: {
                            fontWeight: 'normal'
                        }
                    },
                    {
                        xtype: 'combobox',
                        name: 'period-month-interval',
                        displayField: 'name',
                        valueField: 'value',
                        queryMode: 'local',
                        editable: false,
                        hideLabel: true,
                        value: 1,
                        width: 110,
                        margin: '0 6 0 6',
                        store: new Ext.data.Store({
                            fields: ['name', 'value'],
                            data: (function () {
                                return [
                                    {name: Uni.I18n.translate('general.month.january', 'UNI', 'January'), value: 1},
                                    {name: Uni.I18n.translate('general.month.february', 'UNI', 'February'), value: 2},
                                    {name: Uni.I18n.translate('general.month.march', 'UNI', 'March'), value: 3},
                                    {name: Uni.I18n.translate('general.month.april', 'UNI', 'April'), value: 4},
                                    {name: Uni.I18n.translate('general.month.may', 'UNI', 'May'), value: 5},
                                    {name: Uni.I18n.translate('general.month.june', 'UNI', 'June'), value: 6},
                                    {name: Uni.I18n.translate('general.month.july', 'UNI', 'July'), value: 7},
                                    {name: Uni.I18n.translate('general.month.august', 'UNI', 'August'), value: 8},
                                    {name: Uni.I18n.translate('general.month.september', 'UNI', 'September'), value: 9},
                                    {name: Uni.I18n.translate('general.month.october', 'UNI', 'October'), value: 10},
                                    {name: Uni.I18n.translate('general.month.november', 'UNI', 'November'), value: 11},
                                    {name: Uni.I18n.translate('general.month.december', 'UNI', 'December'), value: 12}
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

        me.getOptionCurrentRadio().on('change', function (scope, newValue, oldValue) {
            if (newValue) {
                me.selectedValue = 'currentday';
                me.fireEvent('periodchange', me.getOnValue());
            }
        }, me);

        me.getOptionCurrentOfYearRadio().on('change', function (scope, newValue, oldValue) {
            if (newValue) {
                me.selectedValue = 'currentdayofyear';
                me.fireEvent('periodchange', me.getOnValue());
            }
        }, me);

        me.getOptionDayOfMonthRadio().on('change', function (scope, newValue, oldValue) {
            if (newValue) {
                me.selectedValue = 'dayofmonth';
                me.fireEvent('periodchange', me.getOnValue());
            }
        }, me);

        me.getOptionDayOfMonthContainer().down('combobox').on('change', function () {
            me.selectOptionDayOfMonth();
        }, me);

        me.getOptionDayOfYearRadio().on('change', function (scope, newValue, oldValue) {
            if (newValue) {
                me.selectedValue = 'dayofyear';
                me.fireEvent('periodchange', me.getOnValue());
            }
        }, me);

        me.getOptionDayOfYearContainer().down('[name=period-day-interval]').on('change', function (scope, newValue, oldValue) {
            if ((newValue >= scope.minValue) && (newValue <= scope.maxValue)) {
                me.selectOptionDayOfYear();
            }
        }, me);

        me.getOptionDayOfYearContainer().down('[name=period-day-interval]').on('blur', function (field) {
            var value = field.getValue();

            if (value < field.minValue) {
                field.setValue(field.minValue);
                me.selectOptionDayOfYear();
            } else if (value > field.maxValue) {
                field.setValue(field.maxValue);
                me.selectOptionDayOfYear();
            }
        }, me);

        me.getOptionDayOfYearContainer().down('[name=period-month-interval]').on('change', function () {
            me.selectOptionDayOfYear();
        }, me);

        me.getOptionDayOfWeekRadio().on('change', function (scope, newValue, oldValue) {
            if (newValue) {
                me.selectedValue = 'dayofweek';
                me.fireEvent('periodchange', me.getOnValue());
            }
        }, me);

        me.getOptionDayOfWeekContainer().down('combobox').on('change', function () {
            me.selectOptionDayOfWeek();
        }, me);
    },

    selectOptionCurrent: function (suspendEvent) {
        this.getOptionCurrentRadio().suspendEvents();
        this.getOptionCurrentRadio().setValue(true);
        this.getOptionCurrentRadio().resumeEvents();
        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getValue());
        }
    },

    selectOptionDayOfMonth: function (suspendEvent) {
        this.selectedValue = 'dayofmonth';
        this.getOptionDayOfMonthRadio().suspendEvents();
        this.getOptionDayOfMonthRadio().setValue(true);
        this.getOptionDayOfMonthRadio().resumeEvents();

        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getValue());
        }
    },

    selectOptionDayOfYear: function (suspendEvent) {
        this.selectedValue = 'dayofyear';

        this.getOptionDayOfYearRadio().suspendEvents();
        this.getOptionDayOfYearRadio().setValue(true);
        this.getOptionDayOfYearRadio().resumeEvents();

        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getValue());
        }
    },

    selectOptionDayOfWeek: function (suspendEvent) {
        this.selectedValue = 'dayofweek';

        this.getOptionDayOfWeekRadio().suspendEvents();
        this.getOptionDayOfWeekRadio().setValue(true);
        this.getOptionDayOfWeekRadio().resumeEvents();

        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getValue());
        }
    },

    getOptionCurrentRadio: function () {
        return this.down('#option-current');
    },

    getOptionCurrentOfYearRadio: function () {
        return this.down('#option-current-of-year');
    },

    getOptionDayOfMonthRadio: function () {
        return this.getOptionDayOfMonthContainer().down('radio');
    },

    getOptionDayOfYearRadio: function () {
        return this.getOptionDayOfYearContainer().down('radio');
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

    getOptionDayOfYearContainer: function () {
        return this.down('#option-doy');
    },

    setOptionCurrentDisabled: function (disabled) {
        var me = this;

        me.getOptionCurrentRadio().setVisible(!disabled);
        me.getOptionCurrentRadio().setDisabled(disabled);

        me.selectAvailableOption();
    },

    setOptionCurrentOfYearDisabled: function (disabled) {
        var me = this;

        me.getOptionCurrentOfYearRadio().setVisible(!disabled);
        me.getOptionCurrentOfYearRadio().setDisabled(disabled);

        me.selectAvailableOption();
    },

    setOptionDayOfMonthDisabled: function (disabled) {
        var me = this,
            radio = me.getOptionDayOfMonthRadio(),
            combo = me.getOptionDayOfMonthContainer();

        radio.setVisible(!disabled);
        radio.setDisabled(disabled);
        combo.setVisible(!disabled);
        combo.setDisabled(disabled);

        if (disabled) {
            me.getOptionDayOfMonthContainer().addCls(Ext.baseCSSPrefix + 'item-disabled');
        } else {
            me.getOptionDayOfMonthContainer().removeCls(Ext.baseCSSPrefix + 'item-disabled');
        }

        me.selectAvailableOption();
    },

    setOptionDayOfYearDisabled: function (disabled) {
        var me = this,
            radio = me.getOptionDayOfYearRadio(),
            combo = me.getOptionDayOfYearContainer();
        radio.setVisible(!disabled);
        radio.setDisabled(disabled);
        combo.setVisible(!disabled);
        combo.setDisabled(disabled);

        if (disabled) {
            me.getOptionDayOfYearContainer().addCls(Ext.baseCSSPrefix + 'item-disabled');
        } else {
            me.getOptionDayOfYearContainer().removeCls(Ext.baseCSSPrefix + 'item-disabled');
        }

        me.selectAvailableOption();
    },

    setOptionDayOfWeekDisabled: function (disabled) {
        var me = this,
            radio = me.getOptionDayOfWeekRadio(),
            combo = me.getOptionDayOfWeekContainer().down('combobox');

        radio.setVisible(!disabled);
        radio.setDisabled(disabled);
        combo.setVisible(!disabled);
        combo.setDisabled(disabled);

        if(Ext.ComponentQuery.query('#period-interval')[0].getValue() === 'weeks') {
            me.selectedValue = 'dayofweek';
            me.fireEvent('periodchange', me.getOnValue());
        }
        me.selectAvailableOption();
    },

    selectAvailableOption: function () {
        var me = this,
            dayRadio = me.getOptionCurrentRadio(),
            monthRadio = me.getOptionDayOfMonthRadio(),
            weekRadio = me.getOptionDayOfWeekRadio(),
            dayOfYearRadio = me.getOptionCurrentOfYearRadio(),
            yearRadio = me.getOptionDayOfYearRadio();

        if (!monthRadio.getValue() && dayRadio.getValue() && dayRadio.isDisabled()) {
            monthRadio.suspendEvents();
            monthRadio.setValue(true);
            monthRadio.resumeEvents();
        }

        if (!weekRadio.getValue() && monthRadio.getValue() && monthRadio.isDisabled()) {
            weekRadio.suspendEvents();
            weekRadio.setValue(true);
            weekRadio.resumeEvents();
        }

        if (!dayRadio.getValue() && weekRadio.getValue() && weekRadio.isDisabled()) {
            dayRadio.suspendEvents();
            dayRadio.setValue(true);
            dayRadio.resumeEvents();
        }
    },

    refreshControls: function () {
        var me = this,
            dayRadio = me.getOptionCurrentRadio(),
            monthRadio = me.getOptionDayOfMonthRadio(),
            weekRadio = me.getOptionDayOfWeekRadio(),
            dayOfYearRadio = me.getOptionCurrentOfYearRadio(),
            yearRadio = me.getOptionDayOfYearRadio(),
            control;

        if ((!monthRadio.getValue() || !dayRadio.getValue()) && (dayRadio.isVisible() || monthRadio.isVisible())) {
            control = me.dom ? monthRadio : dayRadio;
            control.suspendEvents();
            control.setValue(true);
            control.resumeEvents();
        }

        if (!weekRadio.getValue() && weekRadio.isVisible()) {
            weekRadio.suspendEvents();
            weekRadio.setValue(true);
            weekRadio.resumeEvents();
        }

        if ((!yearRadio.getValue() || !dayOfYearRadio.getValue()) && (dayOfYearRadio.isVisible() || yearRadio.isVisible())) {
            dayOfYearRadio.suspendEvents();
            dayOfYearRadio.setValue(true);
            dayOfYearRadio.resumeEvents();
        }
    },

    getOnValue: function () {
        var me = this,
            selectedValue = me.selectedValue,
            dayOfYearValue = me.getOptionDayOfYearContainer().down('[name=period-day-interval]').disabled ? null : me.getOptionDayOfYearContainer().down('[name=period-day-interval]').getValue(),
            monthOfYearValue = me.getOptionDayOfYearContainer().down('[name=period-month-interval]').disabled ? null : me.getOptionDayOfYearContainer().down('[name=period-month-interval]').getValue(),
            dayOfMonthValue = me.getOptionDayOfMonthContainer().down('combobox').disabled ? null : me.getOptionDayOfMonthContainer().down('combobox').getValue(),
            dayOfWeekValue = me.getOptionDayOfWeekContainer().down('combobox').disabled ? null : me.getOptionDayOfWeekContainer().down('combobox').getValue();

        var result = {
            onCurrentDay: selectedValue === 'currentday',
            onCurrentDayOfYear: selectedValue === 'currentdayofyear'
        };

        if (selectedValue === 'dayofmonth') {
            Ext.apply(result, {
                onDayOfMonth: dayOfMonthValue
            });
        } else if (selectedValue === 'dayofweek') {
            Ext.apply(result, {
                onDayOfWeek: dayOfWeekValue
            });
        } else if (selectedValue === 'dayofyear') {
            Ext.apply(result, {
                onDayOfYear: dayOfYearValue,
                onMonthOfYear: monthOfYearValue
            });
        }

        return result;
    }
});
