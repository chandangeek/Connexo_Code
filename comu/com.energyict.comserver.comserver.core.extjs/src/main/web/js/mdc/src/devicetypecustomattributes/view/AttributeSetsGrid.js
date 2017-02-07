/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.devicetypecustomattributes.view.AttributeSetsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-type-custom-attribute-sets-grid',
    store: 'Mdc.devicetypecustomattributes.store.CustomAttributeSets',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.devicetypecustomattributes.view.ActionMenu',
        'Uni.grid.column.CustomAttributeSet'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                xtype: 'custom-attribute-set-column',
                dataIndex: 'fullsetinfo',
                flex: 3
            },
            {
                header: Uni.I18n.translate('customattributesets.viewlevels', 'MDC', 'View levels'),
                dataIndex: 'viewPrivilegesString',
                flex: 2
            },
            {
                header: Uni.I18n.translate('customattributesets.editlevels', 'MDC', 'Edit levels'),
                dataIndex: 'editPrivilegesString',
                flex: 2
            },
            {
                header: Uni.I18n.translate('customattributesets.timeSliced', 'MDC', 'Time-sliced'),
                dataIndex: 'isVersioned',
                renderer: function (value) {
                    return value ?
                        Uni.I18n.translate('general.yes', 'MDC', 'Yes') :
                        Uni.I18n.translate('general.no', 'MDC', 'No');
                }
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.DeviceType.admin,
                menu: {
                    xtype: 'device-type-custom-attribute-sets-action-menu',
                    itemId: 'device-type-custom-attribute-sets-action-menu-id'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('customattributesets.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} custom attribute sets'),
                displayMoreMsg: Uni.I18n.translate('customattributesets.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} custom attribute sets'),
                emptyMsg: Uni.I18n.translate('customattributesets.pagingtoolbartop.emptyMsg', 'MDC', 'There are no custom attribute sets to display'),
                items: [
                    {
                        text: Uni.I18n.translate('customattributesets.addattributesets', 'MDC', 'Add custom attribute sets'),
                        itemId: 'device-type-custom-attribute-sets-add-button',
                        privileges: Mdc.privileges.DeviceType.admin,
                        xtype: 'button',
                        action: 'addAttributeSets'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('customattributesets.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Custom attribute sets per page')
            }
        ];

        me.callParent(arguments);
    }
});

