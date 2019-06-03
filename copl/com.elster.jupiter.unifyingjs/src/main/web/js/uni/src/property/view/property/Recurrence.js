/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.Recurrence', {
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
                        itemId: 'onRecurrence',
                        columns: 1,
                        vertical: true,
                        msgTarget: 'under',
                        fieldLabel: Uni.I18n.translate('general.recurrence.name', 'UNI', 'On recurrence'),
                        items: [
                            {boxLabel: Uni.I18n.translate('general.recurrence.create', 'UNI', 'Create new issues'), name: 'recurrenceCreate', inputValue: '0'},
                            {boxLabel: Uni.I18n.translate('general.recurrence.log', 'UNI', 'Log on existing open issues'), name: 'recurrenceCreate', inputValue: '1'}
                        ],
                        listeners: {
                            change: function (control, newValue, oldValue, eOpts ) {
                                var recur = me.down('#increaseUrgency');
                                if(newValue.recurrenceCreate == '0') {
                                    recur.setDisabled(true);
                                    recur.setValue({increaseUrgency : 0});
                                }else {
                                    recur.setDisabled(false);
                                }
                            }
                        }
                    },
                    {
                        xtype: 'radiogroup',
                        labelWidth: me.labelWidth,
                        width: me.controlsWidth,
                        itemId: 'increaseUrgency',
                        columns: 1,
                        vertical: true,
                        msgTarget: 'under',
                        fieldLabel: Uni.I18n.translate('general.recurrence.again', 'UNI', 'When raised again'),
                        items: [
                            {boxLabel: Uni.I18n.translate('general.recurrence.nothing', 'UNI', 'Do nothing'), name: 'increaseUrgency', inputValue: '0'},
                            {boxLabel: Uni.I18n.translate('general.recurrence.increase', 'UNI', 'Increase urgency (+1)'), name: 'increaseUrgency', inputValue: '1'}
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
            var splitValue = value.split(':');

            (splitValue.length > 0) && me.down('#onRecurrence').setValue({recurrenceCreate : splitValue[0]});
            (splitValue.length > 1) && me.down('#increaseUrgency').setValue({increaseUrgency : splitValue[1]});
        }
    },

    getValue: function (value) {
        var me = this;
        return me.down('#onRecurrence').getValue().recurrenceCreate + ':' +
            me.down('#increaseUrgency').getValue().increaseUrgency
    }
});