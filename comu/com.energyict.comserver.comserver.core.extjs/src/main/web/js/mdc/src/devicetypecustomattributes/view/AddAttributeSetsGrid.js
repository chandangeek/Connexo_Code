/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.devicetypecustomattributes.view.AddAttributeSetsGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.device-type-add-custom-attribute-sets-grid',
    store: 'Mdc.devicetypecustomattributes.store.CustomAttributeSetsAvailable',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.grid.SelectionGrid'
    ],

    margin: '0 0 -10 0',
    checkAllButtonPresent: true,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('customattributesets.multiselect.selected', count, 'MDC', 'No custom attribute sets selected', '{0} custom attribute set selected', '{0} custom attribute sets selected');
    },

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
            }
        ];

        me.callParent(arguments);
    }
});