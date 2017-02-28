/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.registerconfig.RegisterConfigGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerConfigGrid',
    overflowY: 'auto',
    itemId: 'registerconfiggrid',

    deviceTypeId: null,
    deviceConfigId: null,

    selModel: {
        mode: 'SINGLE'
    },

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.RegisterConfigsOfDeviceConfig',
        'Mdc.view.setup.registerconfig.RegisterConfigActionMenu',
        'Uni.grid.column.Obis',
        'Uni.grid.column.ReadingType'
    ],

    store: 'RegisterConfigsOfDeviceConfig',

    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('registerConfig.registerType', 'MDC', 'Register type'),
                dataIndex: 'registerTypeName',
                flex: 4
            },
            {
                xtype: 'obis-column',
                dataIndex: 'overruledObisCode',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.DeviceType.admin,
                menu: {xtype: 'register-config-action-menu'}
            }
        ];

        this.dockedItems = [
            {

                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('registerConfigs.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} register configurations'),
                displayMoreMsg: Uni.I18n.translate('registerConfigs.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} register configurations'),
                emptyMsg: Uni.I18n.translate('registerConfigs.pagingtoolbartop.emptyMsg', 'MDC', 'There are no register configurations to display'),
                items: [
                    {

                        text: Uni.I18n.translate('registerConfigs.createRegisterConfig', 'MDC', 'Add register configuration'),
                        privileges: Mdc.privileges.DeviceType.admin,
                        itemId: 'createRegisterConfigBtn',
                        xtype: 'button',
                        action: 'createRegisterConfig'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                params: [
                    {deviceType: this.deviceTypeId},
                    {deviceConfig: this.deviceConfigId}
                ],
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('registerConfigs.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Register configurations per page')
            }
        ];

        this.callParent();
    }
});
