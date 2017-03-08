/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.RaisedEventProps', {
    extend: 'Uni.property.view.property.Base',

    labelWidth: 250,
    controlsWidth: 600,

    getEditCmp: function () {
        var me = this;
        return  [
            {
                items: [
                    {
                        xtype: 'radiogroup',
                        labelWidth: me.labelWidth,
                        width: me.controlsWidth,
                        itemId: 'raiseEventsShould',
                        columns: 1,
                        vertical: true,
                        msgTarget: 'under',
                        fieldLabel: Uni.I18n.translate('general.raiseEvents.name', 'UNI', 'Raise events should'),
                        items: [
                            {boxLabel: Uni.I18n.translate('general.raiseEvents.create', 'UNI', 'Create new alarms'), name: 'raiseEventsCreate', inputValue: '0'},
                            {boxLabel: Uni.I18n.translate('general.raiseEvents.log', 'UNI', 'Log on existing open alarms'), name: 'raiseEventsCreate', inputValue: '1'}
                        ],
                        listeners: {
                            change: function (control, newValue, oldValue, eOpts ) {
                                var eventsAgain = me.down('#raiseEventsAgain');
                                var eventsCleared = me.down('#raiseEventsCleared');
                                if(newValue.raiseEventsCreate == '0') {
                                    eventsAgain.setDisabled(true);
                                    eventsAgain.setValue({raiseEventsAgain : 0});
                                    eventsCleared.setDisabled(true);
                                    eventsCleared.setValue({raiseEventsCleared : 0});
                                }else {
                                    eventsAgain.setDisabled(false);
                                    eventsCleared.setDisabled(false);
                                }
                            }
                        }
                    },
                    {
                        xtype: 'radiogroup',
                        labelWidth: me.labelWidth,
                        width: me.controlsWidth,
                        itemId: 'raiseEventsAgain',
                        columns: 1,
                        vertical: true,
                        msgTarget: 'under',
                        fieldLabel: Uni.I18n.translate('general.raiseEvents.again', 'UNI', 'When raised again'),
                        items: [
                            {boxLabel: Uni.I18n.translate('general.raiseEvents.nothing', 'UNI', 'Do nothing'), name: 'raiseEventsAgain', inputValue: '0'},
                            {boxLabel: Uni.I18n.translate('general.raiseEvents.increase', 'UNI', 'Increase urgency (+1)'), name: 'raiseEventsAgain', inputValue: '1'}
                        ]
                    },
                    {
                        xtype: 'radiogroup',
                        labelWidth: me.labelWidth,
                        width: me.controlsWidth,
                        itemId: 'raiseEventsCleared',
                        columns: 1,
                        vertical: true,
                        msgTarget: 'under',
                        fieldLabel: Uni.I18n.translate('general.raiseEvents.cleared', 'UNI', 'When cleared'),
                        items: [
                            {boxLabel: Uni.I18n.translate('general.raiseEvents.nothing', 'UNI', 'Do nothing'), name: 'raiseEventsCleared', inputValue: '0'},
                            {boxLabel: Uni.I18n.translate('general.raiseEvents.decrease.urgency', 'UNI', 'Decrease urgency (-1)'), name: 'raiseEventsCleared', inputValue: '1'}
                        ]
                    }
                ]
            }
        ]
    },

    setLocalizedName: function(name) {

    },

    setValue: function (value) {
        var me = this;
        if(value && value.length>0){
            var splittedValue = value.split(':');

            (splittedValue.length > 0) && me.down('#raiseEventsShould').setValue({raiseEventsCreate : splittedValue[0]});
            (splittedValue.length > 1) && me.down('#raiseEventsAgain').setValue({raiseEventsAgain : splittedValue[1]});
            (splittedValue.length > 2) && me.down('#raiseEventsCleared').setValue({raiseEventsCleared : splittedValue[2]});
        }
    },

    getValue: function (value) {
        var me = this;
        return me.down('#raiseEventsShould').getValue().raiseEventsCreate + ':' +
            me.down('#raiseEventsAgain').getValue().raiseEventsAgain + ':' +
            me.down('#raiseEventsCleared').getValue().raiseEventsCleared;
    }
});