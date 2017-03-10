/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.UsagePointGroupsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.usagepointgroups-grid',
    xtype: 'usagepointgroups-grid',    
    requires: [        
        'Imt.usagepointgroups.store.UsagePointGroups',
        'Imt.usagepointgroups.view.UsagePointGroupActionMenu'
    ],    
    store: 'Imt.usagepointgroups.store.UsagePointGroups',
    router: null,

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                dataIndex: 'name',
                renderer: function (value, b, record) {
                    return '<a href="' + me.router.getRoute('usagepoints/usagepointgroups/view').buildUrl({usagePointGroupId: record.get('id')}) + '">' + Ext.String.htmlEncode(value) + '</a>';                    
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.type', 'IMT', 'Type'),
                dataIndex: 'dynamic',
                renderer: function (value) {
                    if (value) {
                        return Uni.I18n.translate('general.dynamic', 'IMT', 'Dynamic')
                    } else {
                        return Uni.I18n.translate('general.static', 'IMT', 'Static')
                    }
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                itemId: 'usagepointgroup-actioncolumn',
                privileges: Imt.privileges.UsagePointGroup.administrateAnyOrStaticGroup,
                isDisabled: function(view, rowIndex, colIndex, item, record) {
                    if (Imt.privileges.UsagePointGroup.canAdministrate()) {
                        return false;
                    } else if (Imt.privileges.UsagePointGroup.canAdministrateUsagePointOfEnumeratedGroup()) {
                        return record.get('dynamic');
                    }
                },
                menu: {
                    xtype: 'usagepointgroup-action-menu',
                    itemId: 'usagepointgroup-action-menu'
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('usagepointgroup.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} usage point groups'),
                displayMoreMsg: Uni.I18n.translate('usagepointgroup.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} usage point groups'),
                emptyMsg: Uni.I18n.translate('usagepointgroup.pagingtoolbartop.emptyMsg', 'IMT', 'There are no usage point groups to display'),
                items: [
                    {
                        text: Uni.I18n.translate('usagepointgroup.add', 'IMT', 'Add usage point group'),
                        privileges: Imt.privileges.UsagePointGroup.administrate,
                        itemId: 'add-usage-point-group-btn',                        
                        action: 'add-usage-point-group'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('usagepointgroup.pagingtoolbarbottom.itemsPerPage', 'IMT', 'Usage point groups per page')
            }
        ];

        me.callParent();
    }
});



