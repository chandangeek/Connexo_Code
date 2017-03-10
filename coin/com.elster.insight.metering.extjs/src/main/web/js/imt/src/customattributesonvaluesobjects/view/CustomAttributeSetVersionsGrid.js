/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesonvaluesobjects.view.CustomAttributeSetVersionsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.custom-attribute-set-versions-grid',
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    initComponent: function() {
        var me = this;

        me.columns = {
            items: [
                {
                    header: Uni.I18n.translate('general.period','IMT','Period'),
                    dataIndex: 'period',
                    flex: 1
                },
                {
                    xtype: 'uni-actioncolumn',
                    privileges: Imt.privileges.UsagePoint.admin,
                    hidden: true,
                    itemId: 'custom-attribute-set-versions-grid-action-column',
                    menu: {
                        xtype: 'time-sliced-custom-attribute-set-action-menu',
                        type: me.type,
                        itemId: 'time-sliced-custom-attribute-set-action-menu-id'
                    }
                }
            ]
        };

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('customattributesets.versions.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} versions'),
                displayMoreMsg: Uni.I18n.translate('customattributesets.versions.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} versions'),
                emptyMsg: Uni.I18n.translate('customattributesets.versions.pagingtoolbartop.emptyMsg', 'IMT', 'There are no versions to display'),
                usesExactCount: true,
                items: [
                    {
                        text: Uni.I18n.translate('customattributesets.versions.add', 'IMT', 'Add version'),
                        itemId: 'custom-attribute-set-add-version-btn-top',
                        hidden: true,
                        xtype: 'button',
                        action: 'moveToAddVersionPage',
                        type: me.type,
                        privileges: Imt.privileges.UsagePoint.hasFullAdministrateTimeSlicedCps()
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('general.versionsPerPage', 'IMT', 'Versions per page'),
            }
        ];

        me.callParent(arguments);
    }
});