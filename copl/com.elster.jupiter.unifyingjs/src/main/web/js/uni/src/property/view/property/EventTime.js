/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Uni.property.view.property.EventTime', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Uni.property.store.RelativePeriods'
    ],
    getEditCmp: function () {
        var me = this;

        return [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'numberfield',
                        itemId: 'count-events',
                        name: 'countEvents',
                        minValue: 1,
                        maxValue: 20,
                        labelWidth: 260,
                        width: 335,
                        fieldLabel: Uni.I18n.translate('general.create.alarms.when','UNI','Create alarms when at least '),
                        listeners: {
                            blur: function (field) {
                                var value = field.getValue();
                                (Ext.isEmpty(value) || value < field.minValue) && field.setValue(field.minValue);
                                (value > field.maxValue) && field.setValue(field.maxValue);
                            }
                        }
                    },
                    {
                        xtype: 'combobox',
                        disabled: false,
                        itemId: 'relative-period-combo',
                        width: 259,
                        fieldLabel: Uni.I18n.translate('general.events.per','UNI','event(s) per '),
                        displayField: 'name',
                        valueField: 'id',
                        store: 'Uni.property.store.RelativePeriods'
                    }
                ]
            }
        ]
    },

    setLocalizedName: function (name) {
    },

    initComponent: function(){
        var periods = Ext.getStore('Uni.property.store.RelativePeriods');
        periods.load();
        this.callParent(arguments);
    },

    setValue: function (value) {
        var me = this,
            relativePeriodCombo = me.down('#relative-period-combo'),
            eventsCounter = me.down('#count-events');

        if(value && value.length > 0){
            eventsCounter.setValue(value.split(':')[0] < 1 ? 1 : value.split(':')[0]);
            relativePeriodCombo.setValue(Number(value.split(':')[1]) < 1 ? 1 : Number(value.split(':')[1]));
        }
    },

    getValue: function () {
        var me = this,
            relativePeriodCombo = me.down('#relative-period-combo'),
            eventsCounter = me.down('#count-events');

        return eventsCounter.getValue() + ':' + relativePeriodCombo.getValue();

    }
});