/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cps.customattributesets.view.AttributeSetsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.custom-attribute-sets-grid',
    store: 'Cps.customattributesets.store.CustomAttributeSets',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Cps.customattributesets.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'CPS', 'Name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.type', 'CPS', 'Type'),
                dataIndex: 'domainName',
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.required', 'CPS', 'Required'),
                dataIndex: 'isRequired',
                flex: 1,
                renderer: function (value) {
                    return value ?
                        Uni.I18n.translate('general.yes', 'CPS', 'Yes') :
                        Uni.I18n.translate('general.no', 'CPS', 'No');
                }
            },
            {
                header: Uni.I18n.translate('customattributesets.viewlevels', 'CPS', 'View levels'),
                dataIndex: 'viewPrivilegesString',
                flex: 2
            },
            {
                header: Uni.I18n.translate('customattributesets.editlevels', 'CPS', 'Edit levels'),
                dataIndex: 'editPrivilegesString',
                flex: 2
            },
            {
                header: Uni.I18n.translate('customattributesets.timeSliced', 'CPS', 'Time-sliced'),
                dataIndex: 'isVersioned',
                flex: 1,
                renderer: function (value) {
                    return value ?
                        Uni.I18n.translate('general.yes', 'CPS', 'Yes') :
                        Uni.I18n.translate('general.no', 'CPS', 'No');
                }
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Cps.privileges.CustomAttributeSets.admin,
                menu: {
                    xtype: 'custom-attribute-sets-action-menu',
                    itemId: 'custom-attribute-sets-action-menu-id'
                },
                isDisabled: me.fnIsDisabled
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('customattributesets.pagingtoolbartop.displayMsg', 'CPS', '{0} - {1} of {2} custom attribute sets'),
                emptyMsg: Uni.I18n.translate('customattributesets.pagingtoolbartop.emptyMsg', 'CPS', 'There are no custom attribute sets to display'),
                usesExactCount: true
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                isFullTotalCount: true,
                itemsPerPageMsg: Uni.I18n.translate('customattributesets.pagingtoolbarbottom.itemsPerPage', 'CPS', 'Custom attribute sets per page')
            }
        ];

        me.callParent(arguments);
    },

    fnIsDisabled: function (view, rowIndex, colIndex, item, record) {
        if(record.get('domainNameUntranslated') === 'com.elster.jupiter.servicecall.ServiceCall') {
            return true;
        } else {
            return false;
        }
    }
});

