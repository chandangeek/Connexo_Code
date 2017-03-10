/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailChannelGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.loadProfileConfigurationDetailChannelGrid',
    itemId: 'loadProfileConfigurationDetailChannelGrid',
    store: 'LoadProfileConfigurationDetailChannels',
    maxHeight: 395,
    scroll: false,
    editActionName: null,
    deleteActionName: null,

    requires: [
        'Uni.grid.column.Obis',
        'Uni.grid.column.ReadingType'
    ],
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' },
        enableTextSelection: true
    },

    initComponent: function () {
        this.columns = [
            {
                header: Uni.I18n.translate('registerConfig.registerType', 'MDC', 'Register type'),
                dataIndex: 'registerTypeName',
                flex: 3
            },
            {
                xtype: 'obis-column',
                dataIndex: 'overruledObisCode',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                items: [

                    {
                        text: Uni.I18n.translate('general.edit','MDC','Edit'),
                        action: this.editActionName
                    },
                    {
                        text: Uni.I18n.translate('general.remove','MDC','Remove'),
                        action: this.deleteActionName
                    }
                ]
            }
        ];
        this.callParent();
    }
});