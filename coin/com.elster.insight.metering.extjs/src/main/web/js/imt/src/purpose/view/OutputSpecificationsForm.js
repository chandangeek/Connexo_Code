Ext.define('Imt.purpose.view.OutputSpecificationsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.output-specifications-form',
    itemId: 'output-specifications-form',
    requires: [
        'Uni.form.field.ReadingTypeDisplay',
        'Imt.util.CommonFields'
    ],
    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },
    router: null,

    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },
    padding: '10 0 0 0',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'displayfield',
                name: 'name',
                itemId: 'output-name-field',
                fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name')
            },
            {
                xtype: 'reading-type-displayfield',
                itemId: 'output-readingtype'
            },
            {
                xtype: 'displayfield',
                name: 'interval',
                itemId: 'output-interval-field',
                fieldLabel: Uni.I18n.translate('form.output.label.interval', 'IMT', 'Interval'),
                renderer: function (interval, field) {
                    if (Ext.isObject(interval)) {
                        field.show();
                        return interval.count + ' ' + interval.timeUnit;
                    } else {
                        field.hide();
                        return '';
                    }
                }
            },
            {
                xtype: 'displayfield',
                name: 'formula',
                itemId: 'output-formula-field',
                fieldLabel: Uni.I18n.translate('form.output.label.formula', 'IMT', 'Formula'),
                renderer: function (formula) {
                    return formula.description;
                }
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'output-validation-title',
                labelAlign: 'left',
                fieldLabel: Uni.I18n.translate('general.validation', 'IMT', 'Validation')
            },
            {
                xtype: 'output-validation-status-form',
                itemId: 'output-validation-status-form',
                defaults: me.defaults,
                router: me.router
            }
        ];

        me.callParent();
    },

    loadRecord: function (record) {
        var me = this;

        me.down('#output-validation-status-form').loadValidationInfo(record.get('validationInfo'));

        me.callParent(arguments);
    }
});