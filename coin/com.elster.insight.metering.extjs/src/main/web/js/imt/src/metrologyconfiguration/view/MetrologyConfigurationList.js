/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.metrologyConfigurationList',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationActionMenu'
    ],
    store: 'Imt.metrologyconfiguration.store.MetrologyConfiguration',
    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                flex: 2,
                dataIndex: 'name',
                renderer: function (value, b, record) {
                    var url = me.router.getRoute('administration/metrologyconfiguration/view').buildUrl({mcid: record.getId()});

                    return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value));
                }
            },
            {
                header: Uni.I18n.translate('general.label.description', 'IMT', 'Description'),
                flex: 4,
                dataIndex: 'description'
            },
            {
                header: Uni.I18n.translate('general.label.status', 'IMT', 'Status'),
                flex: 1,
                dataIndex: 'status',
                renderer: function (value) {
                    return value ? value.name : '';
                }
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Imt.privileges.MetrologyConfig.admin,
                menu: {
                    xtype: 'metrology-configuration-action-menu',
                    itemId: 'metrology-configuration-list-action-menu'
                },
                isDisabled: function(view, rowIndex, callIndex, item, record){
                    return record.get('status').id == 'deprecated';
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('metrologyconfiguration.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} metrology configurations'),
                displayMoreMsg: Uni.I18n.translate('metrologyconfiguration.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} metrologyconfigurations'),
                emptyMsg: Uni.I18n.translate('metrologyconfiguration.pagingtoolbartop.emptyMsg', 'IMT', 'There are no metrology configurations to display'),
                noBottomPaging: true,
                usesExactCount: true
                // out of scope CXO-517
                //items: [
                //    {
                //        text: Uni.I18n.translate('metrologyconfiguration.button.add', 'IMT', 'Add metrology configuration'),
                //        itemId: 'createMetrologyConfiguration',
                //        xtype: 'button',
                //        privileges: Imt.privileges.MetrologyConfig.admin,
                //        action: 'createMetrologyConfiguration'
                //    }
                //]
            }
        ];
        me.callParent(arguments);
    }
});