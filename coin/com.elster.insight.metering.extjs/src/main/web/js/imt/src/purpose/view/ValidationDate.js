Ext.define('Imt.purpose.view.ValidationDate', {
    extend: 'Ext.form.RadioGroup',
    alias: 'widget.validation-date',
    columns: 1,
    defaults: {
        name: 'validationRun'
    },
    defaultDate: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                boxLabel: Uni.I18n.translate('validationResults.validate.fromLast', 'IMT', 'Validate data from last validation ({0})',
                    Uni.DateTime.formatDateShort(me.defaultDate)),
                inputValue: 'lastValidation',
                itemId: 'purpose-rdo-validate-from-last',
                xtype: 'radiofield',
                checked: true,
                name: 'validation'
            },
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                width: 300,
                items: [
                    {
                        boxLabel: Uni.I18n.translate('validationResults.validate.from', 'IMT', 'Validate data from'),
                        inputValue: 'newValidation',
                        itemId: 'purpose-rdo-validate-from-date',
                        xtype: 'radiofield',
                        name: 'validation'
                    },
                    {
                        xtype: 'datefield',
                        itemId: 'purpose-dtm-validation-from-date',
                        editable: false,
                        showToday: false,
                        value: me.defaultDate,
                        fieldLabel: '  ',
                        labelWidth: 10,
                        width: 150,
                        listeners: {
                            focus: {
                                fn: function () {
                                    me.down('#purpose-rdo-validate-from-date').setValue(true);
                                }
                            }
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});