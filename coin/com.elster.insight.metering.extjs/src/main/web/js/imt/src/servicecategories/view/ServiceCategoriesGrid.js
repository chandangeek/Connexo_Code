/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.servicecategories.view.ServiceCategoriesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.service-categories-grid',
    requires: [
        'Uni.view.toolbar.PagingTop'
    ],
    store: 'Imt.servicecategories.store.ServiceCategories',

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.serviceCategory', 'IMT', 'Service category'),
                dataIndex: 'displayName',
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('serviceCategories.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} service categories'),
                displayMoreMsg: Uni.I18n.translate('serviceCategories.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} service categories'),
                emptyMsg: Uni.I18n.translate('serviceCategories.pagingtoolbartop.emptyMsg', 'IMT', 'There are no service categories to display'),
                noBottomPaging: true,
                usesExactCount: true
            }
        ];

        me.callParent(arguments);
    }
});