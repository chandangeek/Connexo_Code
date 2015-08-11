Ext.define('Fwc.devicefirmware.view.form.UploadFieldContainer', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.upload-field-container',
    required: true,
    groupName: 'default',
    width: 600,
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
                    { itemId: 'newTarget', boxLabel: Uni.I18n.translate('general.now', 'FWC', 'Now'), name: me.groupName, inputValue: true, checked: true },
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
                            uploadFileDateContainer.setValue(new Date());
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
                return null;
            } else {
                return dateField.getValue();
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