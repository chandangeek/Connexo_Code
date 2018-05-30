/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.estimation.EstimationRulesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.estimationCfgRulesGrid',
    itemId: 'estimationCfgRulesGrid',
    rulesSetId: null,
    title: '',
    requires: [
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop',
        'Imt.store.EstimationRules'
    ],

    store: 'Imt.store.EstimationRules',
    overflowY: 'auto',
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('estimationPurpose.estimationRule', 'IMT', 'Estimation rule'),
                dataIndex: 'name',
                flex: 1,
                renderer: function (value, b, record) {
                    return '<a href="#/administration/estimationrulesets/' + record.get('ruleSet').id
                        + '/rules/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                dataIndex: 'active',
                flex: 1,
                renderer: function (value) {
                    if (value) {
                        return Uni.I18n.translate('general.active', 'IMT', 'Active')
                    } else {
                        return Uni.I18n.translate('general.inactive', 'IMT', 'Inactive')
                    }
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                usesExactCount: true,
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('estimationPurpose.pagingtoolbartop.displayMsgRule', 'IMT', '{0} - {1} of {2} estimation rules'),
                displayMoreMsg: Uni.I18n.translate('estimationPurpose.pagingtoolbartop.displayMoreMsgRule', 'IMT', '{0} - {1} of more than {2} estimation rules'),
                emptyMsg: Uni.I18n.translate('estimationPurpose.pagingtoolbartop.emptyMsgRule', 'IMT', 'There are no estimation rules to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                isSecondPagination: true,
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('estimationPurpose.pagingtoolbarbottom.itemsPerPageRule', 'IMT', 'Estimation rules per page'),
                dock: 'bottom',
                params: {id: me.rulesSetId},
                deferLoading: true
            }
        ];
        me.callParent(arguments);
    }
});