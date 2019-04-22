/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.RecurrenceSelectionProps', {
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
                        itemId: 'onTransitionFailureShould',
                        columns: 1,
                        vertical: true,
                        msgTarget: 'under',
                        fieldLabel: Uni.I18n.translate('general.transitionFailureShould.name', 'UNI', 'On failure'),
                        items: [
                            {boxLabel: Uni.I18n.translate('general.transitionFailureShould.create', 'UNI', 'Create new device issue'), name: 'transitionFailureCreate', inputValue: '0'},
                            {boxLabel: Uni.I18n.translate('general.transitionFailureShould.log', 'UNI', 'Log on existing open issue'), name: 'transitionFailureCreate', inputValue: '1'}
                        ],
                        listeners: {
                            change: function (control, newValue, oldValue, eOpts ) {
                                var whenTransitionFailureReoccurs = me.down('#whenTransitionFailureReoccurs');
                                if(newValue.transitionFailureCreate == '0') {
                                    whenTransitionFailureReoccurs.setDisabled(true);
                                    whenTransitionFailureReoccurs.setValue({transitionFailureReoccurs : 0});
                                }else {
                                    whenTransitionFailureReoccurs.setDisabled(false);
                                }
                            }
                        }
                    },
                    {
                        xtype: 'radiogroup',
                        labelWidth: me.labelWidth,
                        width: me.controlsWidth,
                        itemId: 'whenTransitionFailureReoccurs',
                        columns: 1,
                        vertical: true,
                        msgTarget: 'under',
                        fieldLabel: Uni.I18n.translate('general.transitionReoccurs.name', 'UNI', 'On failure recurrence'),
                        items: [
                            {boxLabel: Uni.I18n.translate('general.raiseEvents.nothing', 'UNI', 'Do nothing'), name: 'transitionFailureReoccurs', inputValue: '0'},
                            {boxLabel: Uni.I18n.translate('general.raiseEvents.increase', 'UNI', 'Increase urgency (+1)'), name: 'transitionFailureReoccurs', inputValue: '1'}
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

            (splittedValue.length > 0) && me.down('#onTransitionFailureShould').setValue({transitionFailureCreate : splittedValue[0]});
            (splittedValue.length > 1) && me.down('#whenTransitionFailureReoccurs').setValue({transitionFailureReoccurs : splittedValue[1]});
        }
    },

    getValue: function (value) {
        var me = this;
        return me.down('#onTransitionFailureShould').getValue().transitionFailureCreate + ':' +
            me.down('#whenTransitionFailureReoccurs').getValue().transitionFailureReoccurs;
    }
});