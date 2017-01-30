Ext.define('Imt.usagepointmanagement.view.transitionexecute.TransitionDateField', {
    extend: 'Ext.form.RadioGroup',
    alias: 'widget.transition-date-field',
    groupName: 'default',
    columns: 1,
    required: true,
    layout: {
        type: 'vbox',
        align: 'stretchmax'
    },

    requires: [
        'Uni.form.field.DateTime'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'radiofield',
                itemId: 'installation-time-now',
                boxLabel: Uni.I18n.translate('general.now', 'IMT', 'Now'),
                name: me.groupName,
                checked: true,
                inputValue: true,
                submitValue: false
            },
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'radiofield',
                        itemId: 'installation-time-at-date',
                        boxLabel: ' ',
                        name: me.groupName,
                        inputValue: false,
                        submitValue: false
                    },
                    {
                        xtype: 'date-time',
                        itemId: 'installation-time-at-date-time-field',
                        layout: 'hbox',
                        disabled: true,
                        valueInMilliseconds: true,
                        dateConfig: {
                            width: 133
                        },
                        dateTimeSeparatorConfig: {
                            html: Uni.I18n.translate('general.at', 'IMT', 'At').toLowerCase(),
                            style: 'color: #686868'
                        },
                        hoursConfig: {
                            width: 64
                        },
                        minutesConfig: {
                            width: 64
                        }
                    }
                ]
            }
        ];

        me.listeners = {
            change: function (field, newValue) {
                var dateField = me.down('date-time');

                Ext.suspendLayouts();
                if (newValue[field.groupName]) {
                    dateField.disable();
                    dateField.setValue(null);
                } else {
                    dateField.enable();
                    dateField.setValue(moment().startOf('day').add('days', 1));
                }
                Ext.resumeLayouts(true);
            }
        };

        me.callParent(arguments);
    }
});