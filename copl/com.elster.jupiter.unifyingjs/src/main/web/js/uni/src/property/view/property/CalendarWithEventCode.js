/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Uni.property.view.property.CalendarWithEventCode', {
    extend: 'Uni.property.view.property.Base',

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
            !me.hasCalendars() ?
            {
                xtype: 'displayfield',
                itemId: 'no-calendars',
                fieldLabel: Uni.I18n.translate('general.discardDays', 'UNI', 'Discard specific days'),
                value: '<div style="color: #eb5642">' + Uni.I18n.translate('general.discardDays.noCalendars', 'UNI', 'No calendars defined yet') + '</div>',
                htmlEncode: false,
                labelWidth: 260,
                width: 600
            } :
            {
                items: [
                    {
                        xtype: 'checkbox',
                        name: this.getName(),
                        fieldLabel: Uni.I18n.translate('general.discardDays', 'UNI', 'Discard specific days'),
                        itemId: 'discard',
                        labelWidth: 260,
                        width: 600,
                        cls: 'check',
                        msgTarget: 'under',
                        readOnly: me.isReadOnly,
                        boxLabel: me.boxLabel ? me.boxLabel : '',
                        listeners: {
                            change: function (fld, newValue) {
                                me.getCalendarCombo().setDisabled(!newValue);
                                me.getEventCodeCombo().setDisabled(!(newValue && me.getCalendarCombo().getValue()));
                                if (newValue && !me.getCalendarCombo().getValue()) {
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
                        name: this.getName() + '.calendar',
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
                                model.load(newValue, {
                                    callback: function (record) {
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
                        name: this.getName() + '.eventCode',
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

    getDisplayCmp: function () {
        var me = this;

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
                        xtype: 'displayfield',
                        name: this.getName(),
                        fieldLabel: Uni.I18n.translate('general.discardDays', 'UNI', 'Discard specific days'),
                        itemId: 'displayfield-discard',
                        labelWidth: me.labelWidth
                    },
                    {
                        xtype: 'displayfield',
                        name: 'calendar',
                        fieldLabel: Uni.I18n.translate('general.calendar', 'UNI', 'Calendar'),
                        itemId: 'displayfield-calendar',
                        hidden: true,
                        labelWidth: me.labelWidth
                    },
                    {
                        xtype: 'displayfield',
                        name: 'eventCode',
                        fieldLabel: Uni.I18n.translate('general.eventCode', 'UNI', 'Event code'),
                        itemId: 'displayfield-eventCode',
                        hidden: true,
                        labelWidth: me.labelWidth
                    },

                ]
            }
        ];
    },

    setLocalizedName: function (name) {
    },

    getDiscardDisplayfield: function () {
        return this.down('#displayfield-discard');
    },

    getCalendarDisplayfield: function () {
        return this.down('#displayfield-calendar');
    },

    getEventCodeDisplayfield: function () {
        return this.down('#displayfield-eventCode');
    },

    getDiscardCheckbox: function () {
        return this.down('#discard');
    },

    getCalendarCombo: function () {
        return this.down('#calendar');
    },

    getEventCodeCombo: function () {
        return this.down('#eventCode');
    },

    setValue: function (value) {
        var me = this,
            dependenciesCounter = 2,
            showFunc = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    me.getCalendarDisplayfield().show();
                    me.getEventCodeDisplayfield().show();
                    Ext.resumeLayouts(true);
                    me.ownerCt.setLoading(false);
                }
            };
        if (me.hasCalendars()) {
            if (this.isEdit) {
                if (value) {
                    me.getDiscardCheckbox().setValue(value.discardDays);
                    if (value.discardDays) {
                        me.on('eventcodestorebound', me.onEventCodeStoreBound, me, value.eventCode);
                        me.getCalendarCombo().setValue(value.calendar);
                    }
                }
            } else {

                var model = Ext.ModelManager.getModel('Uni.model.timeofuse.Calendar');
                if (value && value.discardDays) {
                    me.ownerCt.setLoading(true);
                    Ext.suspendLayouts();
                    me.getDiscardDisplayfield().setValue(Uni.I18n.translate('general.yes', 'UNI', 'Yes'));
                    me.calendarsStore.load({
                        callback: function () {
                            me.getCalendarDisplayfield().setValue(me.calendarsStore.getById(value.calendar).get('name'));
                            showFunc();
                        }
                    });
                    model.getProxy().setUrl('/api/cal/calendars');
                    model.load(value.calendar, {
                        callback: function (record) {
                            me.getEventCodeDisplayfield().setValue(record.events().getById(value.eventCode).get('name'));
                            showFunc();
                        }
                    });

                } else {
                    me.getDiscardDisplayfield().setValue(Uni.I18n.translate('general.no', 'UNI', 'No'));
                }
            }
        }
    },

    getValue: function () {
        var me = this;
        return me.hasCalendars() ? {
            discardDays: me.getDiscardCheckbox().getValue(),
            calendar: me.getCalendarCombo().getValue(),
            eventCode: me.getEventCodeCombo().getValue()
        } :
        {
            discardDays: false,
            calendar: null,
            eventCode: null
        }
    },

    onEventCodeStoreBound: function (value) {
        this.getEventCodeCombo().setValue(value);
    },

    hasCalendars: function () {
        !!this.calendarsStore.count();
    }
});
