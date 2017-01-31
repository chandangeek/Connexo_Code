/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cps.customattributesets.view.AttributesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.administration-custom-attributes-grid',
    store: 'Cps.customattributesets.store.Attributes',
    requires: [
        'Cps.customattributesets.view.ActionMenu',
        'Uni.grid.column.CustomAttributeType'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.order', 'CPS', 'Order'),
                dataIndex: 'order'
            },
            {
                header: Uni.I18n.translate('customattributesets.attributename', 'CPS', 'Attribute name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.required', 'CPS', 'Required'),
                dataIndex: 'required',
                flex: 1,
                renderer: function (value) {
                    return value ?
                        Uni.I18n.translate('general.yes', 'CPS', 'Yes') :
                        Uni.I18n.translate('general.no', 'CPS', 'No');
                }
            },
            {
                xtype: 'custom-attribute-type-column',
                dataIndex: 'customAttributeType',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.default', 'CPS', 'Default'),
                dataIndex: 'defaultValue',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.description', 'CPS', 'Description'),
                dataIndex: 'description',
                flex: 1
            }
        ];

        me.callParent(arguments);
    }
});

