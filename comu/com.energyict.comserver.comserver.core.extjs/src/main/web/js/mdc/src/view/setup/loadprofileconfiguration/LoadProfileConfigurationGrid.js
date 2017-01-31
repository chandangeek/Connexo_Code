/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.loadProfileConfigurationGrid',
    itemId: 'loadProfileConfigurationGrid',
    requires: [
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationActionMenu',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.grid.column.Obis',
        'Mdc.store.Intervals'
    ],
    store: 'Mdc.store.LoadProfileConfigurationsOnDeviceConfiguration',
    router: null,
    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.loadProfileType', 'MDC', 'Load profile type'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    return '<a href="'
                        + me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles/channels').buildUrl(Ext.merge(me.router.arguments, {loadProfileConfigurationId: record.getId()}))
                        + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                xtype: 'obis-column',
                dataIndex: 'overruledObisCode',
                width: 180
            },
            {
                header: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
                dataIndex: 'timeDuration',
                renderer: function (value) {
                    var intervalRecord = Ext.getStore('Mdc.store.Intervals').getById(value.id);
                    return intervalRecord ? Ext.String.htmlEncode(intervalRecord.get('name')) : '';
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.DeviceType.admin,
                menu: {xtype: 'load-profile-configuration-action-menu'}
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('loadProfileConfigurations.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} load profile configurations'),
                displayMoreMsg: Uni.I18n.translate('loadProfileConfigurations.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} load profile configurations'),
                emptyMsg: Uni.I18n.translate('loadProfileConfigurations.pagingtoolbartop.emptyMsg', 'MDC', 'There are no load profile configurations to display'),
                items: [
                    {
                        text: Uni.I18n.translate('loadProfileConfigurations.add', 'MDC', 'Add load profile configuration'),
                        itemId: 'add-load-profile-configuration-to-device-configuration-btn',
                        privileges: Mdc.privileges.DeviceType.admin,
                        action: 'addloadprofileconfiguration',
                        href: me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles/add').buildUrl()
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: me.store,
                params: {
                    sort: 'name'
                },
                itemsPerPageMsg: Uni.I18n.translate('loadProfileConfigurations.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Load profile configurations per page')
            }
        ];

        me.callParent(arguments);
    }
});

