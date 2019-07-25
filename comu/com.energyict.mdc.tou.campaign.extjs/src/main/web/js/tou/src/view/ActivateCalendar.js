/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
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
        align: 'left'
    },

    requires: [
        'Uni.form.field.DateTime'
    ],

    initComponent: function () {
        var me = this;

        me.getOptionText = function(optionKey){
            switch (optionKey){
                case 'withoutActivation':
                   return Uni.I18n.translate('tou.campaigns.activate.calendar.wActivationLbl', 'TOU', 'Send without activation');
                case 'immediately':
                   return Uni.I18n.translate('tou.campaigns.activate.calendar.immediatelyLbl', 'TOU', 'Immediately');
                case 'onDate':
                   return Uni.I18n.translate('tou.campaigns.activate.calendar.byDateLbl', 'TOU', 'On');
                default:
                   break;
            }
        }

        me.items = [
            {
                xtype: 'displayfield',
                itemId: 'uploadLabel',
                fieldLabel: '',
                hidden: true
            },
            {
                itemId: 'uploadRadioGroup',
                xtype: 'radiogroup',
                columns: 1,
                required: true,
                vertical: true,
                allowBlank : true,
                padding: '0 0 10 0',
                defaults: {
                    name: me.groupName,
                    submitValue: false
                },
                items: [{
                        itemId: 'wActivation',
                        id: 'withoutActivationRg',
                        boxLabel: me.getOptionText('withoutActivation'),
                        name: me.groupName,
                        inputValue: 'withoutActivation',
                        checked: false,
                        margin: '5 0 5 0'
                    }, {
                        itemId: 'Immediately',
                        id: 'immediatelyRg',
                        boxLabel: me.getOptionText('immediately'),
                        name: me.groupName,
                        inputValue: 'immediately',
                        checked: false,
                        margin: '5 0 5 0'
                    }, {
                        itemId: 'ByDate',
                        id: 'onDateRg',
                        name: me.groupName,
                        boxLabel: me.getOptionText('onDate'),
                        inputValue: 'onDate',
                        margin: '5 0 5 0'
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
                        if (newValue[me.groupName] === 'immediately'
                         || newValue[me.groupName] === 'onDate') {
                            timeoutFld.show();
                        } else {
                            timeoutFld.hide();
                        }
                        me.fireEvent('change', field, newValue);
                    }
                }
            }, {
                xtype: 'container',
                height: '100%',
                style: {
                    margin: '0 0 -20 0'
                },
                layout: {
                    type: 'vbox',
                    pack: 'end'
                },
                items : [{
                    xtype: 'date-time',
                    itemId: 'uploadFileDateContainer',
                    disabled: true,
                    layout: 'hbox',
                    dateConfig: {
                        width: 155
                    },
                    hoursConfig: {
                        width: 60
                    },
                    minutesConfig: {
                        width: 60
                    }
                }]
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

        me.setOptions = function(optionsArr){
           if (!optionsArr || !optionsArr.length) {
               this.hide();
               return;
           }

           var radiogroup = me.down('#uploadRadioGroup'),
               dateField = me.down('#uploadFileDateContainer'),
               updateLabel = me.down('#uploadLabel');

           if (optionsArr.length === 1) {
              radiogroup.hide();
              updateLabel.setValue(me.getOptionText(optionsArr[0]));
              var value = {};
              value[me.groupName] = optionsArr[0];
              radiogroup.setValue(value);
              updateLabel.show();
           }else{
              radiogroup.show();
              updateLabel.hide();
              radiogroup.setValue({});
              var allOptions = ['withoutActivation', 'immediately', 'onDate'];
              Ext.Array.forEach(allOptions, function(option){
                  var cmp = Ext.getCmp(option + 'Rg');
                  if (cmp){
                      (Ext.Array.indexOf(optionsArr, option) !== -1) ? cmp.show() : cmp.hide()
                  }
              })
           }

           if (optionsArr.indexOf("onDate") === -1){
              dateField.hide();
           }
        }

        me.callParent(arguments);
        // if (me.listeners && me.listeners.change) {
        //     debugger;
        //     me.down('#uploadRadioGroup').addListener("change", me.listeners.change, me.listeners.scope);
        // }
    }

});