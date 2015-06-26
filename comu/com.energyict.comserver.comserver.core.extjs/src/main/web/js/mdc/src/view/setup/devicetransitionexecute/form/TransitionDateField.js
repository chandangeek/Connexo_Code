Ext.define('Mdc.view.setup.devicetransitionexecute.form.TransitionDateField', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.transition-date-field',
    required: true,
    groupName: 'default',
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
                    { itemId: 'newTarget', boxLabel: Uni.I18n.translate('general.now', 'MDC', 'Now'), name: me.groupName, inputValue: true, checked: true },
                    { itemId: 'oldTarget', name: me.groupName, inputValue: false}
                ],
                listeners: {
                    change: function (field, newValue) {
                        var uploadFileDateContainer = me.down('#uploadFileDateContainer');
                        if (newValue[me.groupName]) {
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
                margin: '30 0 0 -20',
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

        me.getValue = function () {
            var radiogroup = me.down('#uploadRadioGroup'),
                dateField = me.down('#uploadFileDateContainer');

            if (radiogroup.getValue()[me.groupName]) {
                return {transitionNow: true, time: new Date().getTime()};
            } else {
                return {transitionNow: false, time: dateField.getValue().getTime()};
            }
        };

        me.setValue = function (value) {
            var radiogroup = me.down('#uploadRadioGroup'),
                dateField = me.down('#uploadFileDateContainer');

            if (Ext.isEmpty(value)) {
                radiogroup.setValue(false);
                dateField.setValue(value);
            } else {
                radiogroup.setValue(true);

            }
        };

        me.callParent(arguments);
    }

});