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
                        boxLabel: 'Send without activation',
                        name: me.groupName,
                        inputValue: 'Without Activation',
                        checked: true
                    },
                    {
                         itemId: 'Immediately',
                         boxLabel: 'Immediately',
                         name: me.groupName,
                         inputValue: 'Immediately',
                         checked: false
                    },
                    {
                        itemId: 'ByDate',
                        id: 'TouByDate',
                        name: me.groupName,
                        boxLabel: 'On',
                        inputValue: 'ByDate',
                        margin: '7 0 0 0'
                    }
                ],
                listeners: {
                    change: function (field, newValue) {
                        var uploadFileDateContainer = me.down('#uploadFileDateContainer');
                        if (newValue[me.groupName] == 'Without Activation' || newValue[me.groupName] == 'Immediately') {
                            uploadFileDateContainer.disable();
                            uploadFileDateContainer.setValue(null);
                         } else {
                             uploadFileDateContainer.enable();
                             uploadFileDateContainer.setValue(moment().startOf('day').add('days', 1));
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

            var activationDateValue = radiogroup.getValue();

            if (activationDateValue && activationDateValue[me.groupName] == 'ByDate') {
                return dateField.getValue().getTime().toString();
            }
            return activationDateValue[me.groupName];
        };

        me.getDateValue = function () {
            var dateField = me.down('#uploadFileDateContainer');
            return dateField.getValue().getTime().toString();
        };


        me.callParent(arguments);
    }

});