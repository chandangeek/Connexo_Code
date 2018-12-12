/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesets.view.Grid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.cas-grid',
    actionColumnConfig: null,
    dockedConfig: {
        showTop: true,
        showBottom: true,
        showAddBtn: true
    },

    initComponent: function () {
        var me = this,
            dockedTop;

        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                dataIndex: 'name',
                flex: 2
            },
            {
                header: Uni.I18n.translate('customattributesets.viewlevels', 'IMT', 'View levels'),
                dataIndex: 'viewPrivilegesString',
                flex: 1
            },
            {
                header: Uni.I18n.translate('customattributesets.editlevels', 'IMT', 'Edit levels'),
                dataIndex: 'editPrivilegesString',
                flex: 1
            },
            {
                header: Uni.I18n.translate('customattributesets.timeSliced', 'IMT', 'Time-sliced'),
                dataIndex: 'isVersioned',
                renderer: function (value) {
                    return value ?
                        Uni.I18n.translate('general.yes', 'IMT', 'Yes') :
                        Uni.I18n.translate('general.no', 'IMT', 'No');
                }
            }
        ];

        if (me.actionColumnConfig) {
            me.columns.push(me.actionColumnConfig);
        }

        me.dockedItems = [];

        if (me.dockedConfig.showTop) {
            dockedTop = {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('customattributesets.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} custom attribute sets'),
                displayMoreMsg: Uni.I18n.translate('customattributesets.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} custom attribute sets'),
                emptyMsg: Uni.I18n.translate('customattributesets.pagingtoolbartop.emptyMsg', 'IMT', 'There are no custom attribute sets to display'),
                noBottomPaging: !me.dockedConfig.showBottom,
                usesExactCount: !me.dockedConfig.showBottom
            };

            if (me.dockedConfig.showAddBtn) {
                var addBtn = {
                    text: Uni.I18n.translate('customattributesets.addattributesets', 'IMT', 'Add custom attribute sets'),
                    itemId: 'add-custom-attribute-set',
                    privileges: Imt.privileges.MetrologyConfig.admin,
                    xtype: 'button',
                    action: 'addAttributeSets'
                };

                if (Ext.isObject(me.dockedConfig.showAddBtn)) {
                    Ext.apply(addBtn, me.dockedConfig.showAddBtn);
                }
                dockedTop.items = [addBtn];
            }

            me.dockedItems.push(dockedTop);
        }

        if (me.dockedConfig.showBottom) {
            me.dockedItems.push({
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                deferLoading: true,
                itemsPerPageMsg: Uni.I18n.translate('customattributesets.pagingtoolbarbottom.itemsPerPage', 'IMT', 'Custom attribute sets per page')
            });
        }

        me.callParent(arguments);
    }
});