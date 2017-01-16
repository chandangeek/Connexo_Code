Ext.define('Isu.view.issues.SetPriorityForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.util.FormErrorMessage',
        'Isu.model.Issue'
    ],

    alias: 'widget.set-priority-form',
    returnLink: null,
    ui: 'large',
    defaults: {
        labelWidth: 260
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.priority', 'ISU', 'Priority'),
                margin: '0 0 20 0',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'label',
                        itemId: 'priority-label',
                        text: '50',
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',

                fieldLabel: Uni.I18n.translate('general.urgency', 'ISU', 'Urgency'),
                layout: 'hbox',
                items: [
                    {
                        xtype: 'numberfield',
                        itemId: 'num-urgency',
                        width: 100,
                        name: 'urgency',
                        minValue: 0,
                        maxValue: 50,
                        listeners: {
                            change: function (record) {
                                me.changePriority();
                            },
                            blur: me.numberFieldValidation
                        }

                    },
                    {
                        xtype: 'numberfield',
                        itemId: 'num-impact',
                        labelWidth: 50,
                        width: 150,
                        name: 'impact',
                        fieldLabel: Uni.I18n.translate('general.impact', 'ISU', 'Impact'),
                        minValue: 0,
                        maxValue: 50,
                        margin: '0 0 0 20',
                        listeners: {
                            change: function (record) {
                                me.changePriority();
                            },
                            blur: me.numberFieldValidation
                        }

                    }
                ]
            },

            {
                xtype: 'fieldcontainer',
                ui: 'actions',
                fieldLabel: ' ',
                defaultType: 'button',
                items: [
                    {
                        itemId: 'savePriority',
                        ui: 'action',
                        text: Uni.I18n.translate('general.save', 'ISU', 'Save'),
                        action: 'savePriority'
                    },
                    {
                        itemId: 'cancel',
                        text: Uni.I18n.translate('general.cancel', 'ISU', 'Cancel'),
                        ui: 'link',
                        href: me.returnLink
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this;

        me.callParent(arguments);
        me.updateLayout();
        Ext.resumeLayouts(true);
    },

    changePriority: function()
    {
        var me = this,
            labelPriority = me.down('#priority-label'),
            numUrgencyValue = me.down('#num-urgency').value,
            numImpactValue = me.down('#num-impact').value,
            priorityValue = numUrgencyValue + numImpactValue,
            priorityLabel;

        var priority = priorityValue / 10;
        if (priorityValue > 100){
            priority = 10;
            priorityValue = 100;
        }
        priorityLabel = (priority <= 2) ? Uni.I18n.translate('bpm.task.priority.veryLow', 'ISU', 'Very low') :
            (priority <= 4) ? Uni.I18n.translate('bpm.task.priority.low', 'ISU', 'Low') :
                (priority <= 6) ? Uni.I18n.translate('bpm.task.priority.medium', 'ISU', 'Medium') :
                    (priority <= 8) ? Uni.I18n.translate('bpm.task.priority.high', 'ISU', 'High') :
                        Uni.I18n.translate('bpm.task.priority.veryHigh', 'ISU', 'Very high');


        labelPriority.setText(priorityValue + ' - ' + priorityLabel);

    },
    numberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        }

        if (value > field.maxValue) {
            field.setValue(field.maxValue);
        }
    }

});