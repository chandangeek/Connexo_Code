Ext.define('Imt.purpose.view.OutputSpecificationsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.output-specifications-form',
    itemId: 'output-specifications-form',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    requires: [
        'Uni.form.field.ReadingTypeDisplay'
    ],

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
            }
            //{
            //    xtype: 'fieldcontainer',
            //    fieldLabel: Uni.I18n.translate('form.output.section.formulaComponents', 'IMT', 'Formula components'),
            //    layout: 'vbox',
            //    labelAlign: 'top',
            //    items: [
            //        {
            //            xtype: 'displayfield',
            //            fieldLabel: 'Field 1',
            //            name: 'formula'
            //        }, {
            //            xtype: 'displayfield',
            //            fieldLabel: 'Field 2',
            //            name: 'formula'
            //        }
            //    ]
            //}
        ];

        me.callParent();
    }
});