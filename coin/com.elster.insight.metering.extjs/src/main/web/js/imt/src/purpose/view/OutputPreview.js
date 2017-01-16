Ext.define('Imt.purpose.view.OutputPreview', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Imt.usagepointmanagement.view.widget.OutputKpi'
    ],
    alias: 'widget.output-preview',
    layout: 'hbox',
    defaults: {
        flex: 1
    },
    router: null,

    chartConfig: {
        xtype: 'output-kpi-widget',
        itemId: 'output-kpi-widget',
        header: {
            hidden: true
        },
        titleIsPartOfDataView: true
    },

    attributesConfig: {
        xtype: 'form',
        itemId: 'output-attributes',
        defaults: {
            xtype: 'displayfield',
            labelWidth: 200
        },
        items: [
            {
                fieldLabel: Uni.I18n.translate('general.allDataValidated', 'INS', 'All data validated'),
                name: 'allDataValidated',
                itemId: 'all-data-validated-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.lastChecked', 'INS', 'Last checked'),
                name: 'lastChecked',
                itemId: 'last-checked-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.suspects', 'INS', 'Suspects'),
                name: 'suspects',
                itemId: 'suspects-field',
                htmlEncode: false
            },
            {
                fieldLabel: Uni.I18n.translate('general.informatives', 'INS', 'Informatives'),
                name: 'informatives',
                itemId: 'informatives-field',
                htmlEncode: false
            },
            {
                fieldLabel: Uni.I18n.translate('general.estimates', 'INS', 'Estimates'),
                name: 'estimates',
                itemId: 'estimates-field',
                htmlEncode: false
            }
        ]
    },

    loadRecord: function (record) {
        var me = this,
            attributesForm;

        Ext.suspendLayouts();
        me.setTitle(record.get('name'));
        me.removeAll();
        me.add(Ext.apply(me.chartConfig, {
            output: record.getSummary() || record,
            purpose: me.purpose,
            router: me.router
        }));
        attributesForm = me.add(me.attributesConfig);
        attributesForm.getForm().setValues(me.prepareAttributesData(record.get('validationInfo')));
        Ext.resumeLayouts(true);
    },

    prepareAttributesData: function (validationInfo) {
        var me = this,
            data = {
                allDataValidated: validationInfo.allDataValidated
                    ? Uni.I18n.translate('general.yes', 'INS', 'Yes')
                    : Uni.I18n.translate('general.no', 'INS', 'No')
            };

        if (!Ext.isEmpty(validationInfo.lastChecked)) {
            data.lastChecked = Uni.DateTime.formatDateTimeLong(new Date(validationInfo.lastChecked));
        }

        data.suspects = me.prepareRulesInfo(validationInfo.suspectReason);
        data.informatives = me.prepareRulesInfo(validationInfo.informativeReason);
        data.estimates = me.prepareRulesInfo(validationInfo.estimateReason, true);

        return data;
    },

    prepareRulesInfo: function (data, isEstimation) {
        var me = this,
            result;

        if (!Ext.isEmpty(data)) {
            result = '';
            Ext.Array.each(data, function (item, index) {
                var rule = item.key,
                    url;

                if (index > 0) {
                    result += '<br>';
                }

                if (rule.deleted) {
                    result += rule.displayName + ' - ' + item.value;
                } else {
                    if (isEstimation) {
                        url = me.router.getRoute('administration/estimationrulesets/estimationruleset/rules/rule').buildUrl({
                            ruleSetId: rule.ruleSet.id,
                            ruleId: rule.id
                        });
                    } else {
                        url = me.router.getRoute('administration/rulesets/overview/versions/overview/rules/overview').buildUrl({
                            ruleSetId: rule.ruleSetVersion.ruleSet.id,
                            versionId: rule.ruleSetVersion.id,
                            ruleId: rule.id
                        });
                    }

                    result += '<a href="' + url + '">' + rule.displayName + '</a>' + ' - ' + item.value;
                }
            });
        }

        return result;
    }
});