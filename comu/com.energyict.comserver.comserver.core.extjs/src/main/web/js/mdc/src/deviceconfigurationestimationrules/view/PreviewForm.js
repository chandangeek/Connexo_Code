Ext.define('Mdc.deviceconfigurationestimationrules.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.device-configuration-estimation-rules-preview-form',

    requires: [
        'Uni.grid.column.ReadingType'
    ],

    router: null,

    defaults: {
        labelWidth: 250,
        xtype: 'displayfield'
    },

    initComponent: function () {
        var me = this;



        me.items = [
            {
                fieldLabel: Uni.I18n.translate('general.estimationrule', 'MDC', 'Estimation rule'),
                name: 'name',
                renderer: function (value) {
                    var record = me.getRecord();
                    var res = '';
                    if (value && record && record.get('id') && record.get('ruleSet').id) {
                        var url = me.router.getRoute('administration/estimationrulesets/estimationruleset/rules/rule').buildUrl({ruleSetId: record.get('ruleSet').id, ruleId: record.get('id') });
                        res = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>'
                    }
                    return res;
                }
            },
            {
                fieldLabel: Uni.I18n.translate('deviceconfiguration.estimation.rules.estimator', 'MDC', 'Estimator'),
                name: 'displayName'
            },
            {
                fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                name: 'status',
                renderer: function(value) {
                    return value ? Uni.I18n.translate('general.active', 'MDC', 'Active') :  Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                }
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('deviceconfiguration.estimation.rules.readingtypes', 'MDC', 'Reading types'),
                itemId: 'readingTypesArea'
            }

        ];

        me.callParent(arguments);
    },

    fillReadings: function (record) {
        this.down('#readingTypesArea').removeAll();
        for (var i = 0; i < record.get('readingTypes').length; i++) {
            var readingType = record.get('readingTypes')[i];

            this.down('#readingTypesArea').add(
                {
                    xtype: 'reading-type-displayfield',
                    fieldLabel: undefined,
                    value: readingType,
                    margin: '0 0 -10 0'
                }
            );
        }
    }
});
