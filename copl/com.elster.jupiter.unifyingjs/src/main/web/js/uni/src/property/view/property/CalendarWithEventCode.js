/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Uni.property.view.property.CalendarWithEventCode', {
    extend: 'Uni.property.view.property.Base',

//TODO: displayCmp
    getEditCmp: function () {
        var me = this;
        me.layout = 'vbox';

        me.calendarsStore = Ext.create('Ext.data.Store', {
            model: 'Uni.model.timeofuse.Calendar',
            proxy: {
                url: '/api/cal/calendars',
                type: 'rest',
                reader: {
                    type: 'json',
                    root: 'calendars'
                }
            }
        });

        return [
            {
                items: [
                    {
                        xtype: 'checkbox',
                        name: this.getName(),
                        fieldLabel: Uni.I18n.translate('general.discardDays', 'UNI', 'Discard specific days'),
                        itemId: me.key + 'checkbox',
                        labelWidth: 260,
                        width: 600,
                        cls: 'check',
                        msgTarget: 'under',
                        readOnly: me.isReadOnly,
                        boxLabel: me.boxLabel ? me.boxLabel : '',
                        listeners: {
                            change: function(fld, newValue){
                                me.getCalendarCombo().setDisabled(!newValue);
                                me.getEventCodeCombo().setDisabled(!me.getCalendarCombo().getValue());
                                if(newValue && !me.getCalendarCombo().getValue()){
                                    me.calendarsStore.load();
                                }
                            }
                        }

                    },
                    {
                        xtype: 'combobox',
                        itemId: 'calendar',
                        fieldLabel: Uni.I18n.translate('general.calendar', 'UNI', 'Calendar'),
                        queryMode: 'local',
                        name: 'calendar',
                        labelWidth: 260,
                        width: 600,
                        emptyText: Uni.I18n.translate('general.calendar.type.empty', 'UNI', 'Select a ToU calendar...'),
                        valueField: 'id',
                        displayField: 'name',
                        disabled: true,
                        required: true,
                        store: me.calendarsStore,
                        msgTarget: 'under',
                        editable: false,
                        listeners: {
                            change: function (combo, newValue, oldValue) {
                                me.getEventCodeCombo().clearValue();
                                var model = Ext.ModelManager.getModel('Uni.model.timeofuse.Calendar');
                                model.getProxy().setUrl('/api/cal/calendars');
                                model.load(newValue,{
                                    callback: function(record){
                                        me.getEventCodeCombo().bindStore(record.events(), true);
                                        me.fireEvent('eventcodestorebound');
                                        me.getEventCodeCombo().enable();
                                        me.un('eventcodestorebound', me.onEventCodeStoreBound, me);
                                    }
                                });
                            }
                        }
                    },
                    {
                        xtype: 'combobox',
                        itemId: 'eventCode',
                        disabled: true,
                        fieldLabel: Uni.I18n.translate('general.eventCode', 'UNI', 'Event code'),
                        queryMode: 'local',
                        required: true,
                        name: this.getName(),
                        labelWidth: 260,
                        width: 600,
                        valueField: 'id',
                        displayField: 'name',
                        editable: false,
                        msgTarget: 'under',
                        emptyText: Uni.I18n.translate('general.eventCode.empty', 'UNI', 'Select event code...'),
                    }
                ]
            }
        ];
    },

    setLocalizedName: function (name) {
    },

    getDiscardCheckbox: function(){
        return this.down('checkbox');
    },

    getCalendarCombo: function(){
        return this.down('#calendar');
    },

    getEventCodeCombo: function(){
        return this.down('#eventCode');
    },

    setValue: function (value) {
        var me = this;

        if(value) {
            me.getDiscardCheckbox().setValue(value.discardDays);
            if(value.discardDays){
                me.on('eventcodestorebound', me.onEventCodeStoreBound, me, value.eventCode);
                me.getCalendarCombo().setValue(value.calendar);
            }
        }
    },

    getValue: function () {
        var me = this;
        return {
            discardDays: me.getDiscardCheckbox().getValue(),
            calendar: me.getCalendarCombo().getValue(),
            eventCode: me.getEventCodeCombo().getValue()
        };
    },

    onEventCodeStoreBound: function(value){
        this.getEventCodeCombo().setValue(value);
    }
});
