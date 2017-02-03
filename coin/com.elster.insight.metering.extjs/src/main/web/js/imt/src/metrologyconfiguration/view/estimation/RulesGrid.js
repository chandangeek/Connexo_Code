/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.estimation.RulesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.est-purpose-rules-grid',

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                text: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                dataIndex: 'name',
                flex: 4,
                renderer: function (value, metaData, record) {
                    return '<a href="' + me.router.getRoute('administration/estimationrulesets/estimationruleset/rules/rule').buildUrl({
                            ruleSetId: record.get('ruleSet').id,
                            ruleId: record.getId()
                        }) + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
                text: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                dataIndex: 'active',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.active', 'IMT', 'Active') : Uni.I18n.translate('general.inactive', 'IMT', 'Inactive');
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                itemId: 'est-purpose-rules-grid-paging-top',
                store: me.store,
                dock: 'top',
                noBottomPaging: true,
                usesExactCount: true,
                isFullTotalCount: true,
                displayMsg: Uni.I18n.translate('metrologyConfiguration.estimation.rulesCount', 'IMT', '{0} estimation rule(s)', me.store.getCount())
            }
        ];

        me.callParent(arguments);
    }
});

