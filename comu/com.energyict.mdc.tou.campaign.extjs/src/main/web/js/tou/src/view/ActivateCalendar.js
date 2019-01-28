/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.ActivateCalendar', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.activate-calendar-field',
    required: true,
    groupName: 'activateCal',
    width: 800,
    labelWidth: 150,
    layout: {
        type: 'hbox',
        align: 'right'
    },

    requires: [
       'Uni.form.field.DateTime'
    ],

    initComponent: function () {
        var me = this;

        me.items = [

            {
                itemId: 'uploadRadioGroup',
                xtype: 'radiogroup',
                columns: 1,
                required: true,
                vertical: true,
                defaults: {
                    name: me.groupName,
                    submitValue: false
                },
                items: [
                    {
                        itemId: 'wActivation',
                        boxLabel: Uni.I18n.translate('tou.campaigns.activate.calendar.wActivationLbl', 'TOU', 'Send without activation'),
                        name: me.groupName,
                        inputValue: 'withoutActivation',
                        checked: true
                    },
                    {
                         itemId: 'Immediately',
                         boxLabel: Uni.I18n.translate('tou.campaigns.activate.calendar.immediatelyLbl', 'TOU', 'Immediately'),
                         name: me.groupName,
                         inputValue: 'immediately',
                         checked: false
                    },
                    {
                        itemId: 'ByDate',
                        id: 'TouByDate',
                        name: me.groupName,
                        boxLabel: Uni.I18n.translate('tou.campaigns.activate.calendar.byDateLbl', 'TOU', 'On'),
                        inputValue: 'onDate',
                        margin: '7 0 0 0'
                    }
                ],
                listeners: {
                    change: function (field, newValue) {
                        var timeoutFld = Ext.getCmp('tou-period-values');
                        var uploadFileDateContainer = me.down('#uploadFileDateContainer');
                        if (newValue[me.groupName] == 'withoutActivation' || newValue[me.groupName] == 'immediately') {
                            uploadFileDateContainer.disable();
                            uploadFileDateContainer.setValue(null);
                         } else {
                             uploadFileDateContainer.enable();
                             uploadFileDateContainer.setValue(moment().startOf('day').add('days', 1));
                         }
                         if (newValue[me.groupName] == 'immediately'){
                             timeoutFld.show();
                         }else{
                             timeoutFld.hide();
                         }

                    }
                }
            },
            {
                xtype: 'date-time',
                itemId: 'uploadFileDateContainer',
                layout: 'hbox',
                disabled: true,
                margin: '50 0 0 0',
                dateConfig: {
                    width: 155
                },
                hoursConfig: {
                    width: 60
                },
                minutesConfig: {
                    width: 60
                }
            }
        ];

        me.getOptionValue = function () {
            var radiogroup = me.down('#uploadRadioGroup'),
                dateField = me.down('#uploadFileDateContainer');

            if (radiogroup) return radiogroup.getValue()[me.groupName];
        };

        me.getDateValue = function () {
            var dateField = me.down('#uploadFileDateContainer');
            return dateField.getValue().getTime().toString();
        };


        me.callParent(arguments);
    }

});