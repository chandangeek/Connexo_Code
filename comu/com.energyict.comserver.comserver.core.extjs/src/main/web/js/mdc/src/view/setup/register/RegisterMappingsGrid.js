/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.register.RegisterMappingsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerMappingsGrid',
    overflowY: 'auto',
    itemId: 'registermappinggrid',

    deviceTypeId: null,

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.register.RegisterMappingActionMenu',
        'Uni.grid.column.Obis',
        'Uni.grid.column.ReadingType',
        'Ext.grid.plugin.BufferedRenderer'
    ],

    store: 'Mdc.store.RegisterTypesOfDevicetype',

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                xtype: 'reading-type-column',
                dataIndex: 'readingType',
                flex: 1
            },
            {
                xtype: 'obis-column',
                dataIndex: 'obisCode'
            },

            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.DeviceType.admin,
                menu: {
                    xtype: 'register-mapping-action-menu',
                    itemId: 'register-mapping-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {

                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('registerMappings.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} register types'),
                displayMoreMsg: Uni.I18n.translate('registerMappings.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} register types'),
                emptyMsg: Uni.I18n.translate('registerMappings.pagingtoolbartop.emptyMsg', 'MDC', 'There are no register types to display'),
                items: [
                    {

                        text: Uni.I18n.translate('registerMapping.addRegisterMapping', 'MDC', 'Add register types'),
                        privileges: Mdc.privileges.DeviceType.admin,
                        itemId: 'addRegisterMappingBtn',
                        xtype: 'button',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/registertypes/add',
                        hrefTarget: '_self',
                        action: 'addRegisterMapping'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                params: [
                    {deviceType: me.deviceTypeId}
                ],
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('registerTypes.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Register types per page')
            }
        ];

        me.callParent();
    }
});
