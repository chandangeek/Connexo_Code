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
                itemId: 'suspects-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.informatives', 'INS', 'Informatives'),
                name: 'informatives',
                itemId: 'informatives-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.estimates', 'INS', 'Estimates'),
                name: 'estimates',
                itemId: 'estimates-field'
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
            output: record,
            purpose: me.purpose,
            router: me.router
        }));
        attributesForm = me.add(me.attributesConfig);
        Ext.resumeLayouts(true);
    }
});