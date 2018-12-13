/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.metrologyconfiguration.PurposesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.purposes-grid',
    requires: [
        'Imt.usagepointmanagement.view.metrologyconfiguration.PurposeActionMenu'
    ],
    store: null,
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                text: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                dataIndex: 'name',
                flex: 4,
                renderer: function (value, metaData, record) {
                    var description = record.get('description');

                    return value + (description
                            ? '<span class="icon-info" style="color: #A9A9A9; margin-left: 10px; font-size: 16px; vertical-align: middle;" data-qtip="'
                        + description + '"></span>'
                            : '');
                }
            },
            {
                text: Uni.I18n.translate('general.required', 'IMT', 'Required'),
                dataIndex: 'required',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.no', 'IMT', 'No');
                }
            },
            {
                text: Uni.I18n.translate('validation.active', 'IMT', 'Active'),
                dataIndex: 'active',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.no', 'IMT', 'No');
                }
            },
            {
                text: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                dataIndex: 'status',
                flex: 1,
                align: 'center',
                renderer: function (value) {
                    var icon = '<span class="'
                            + (value.id === 'incomplete' ? 'icon-warning' : 'icon-checkmark-circle')
                            + '" style="display: inline-block; font-size:16px; color: '
                            + (value.id === 'incomplete' ?  '#eb5642' : '#33cc33')
                            + ';" data-qtip="' + value.name + '"></span>';
                    return value ? icon : '-';
                }
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                privileges: Imt.privileges.UsagePoint.admin,
                itemId: 'usage-point-purpose-action-column',
                menu: {
                    xtype: 'purpose-action-menu',
                    itemId: 'usage-point-purpose-action-menu'
                },
                showCondition: function (record) {
                    return !record.get('required');
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                isFullTotalCount: true,
                noBottomPaging: true,
                displayMsg: Uni.I18n.translate('metrologyConfigurationDetails.purposesCount', 'IMT', '{2} purpose(s)')
            }
        ];
        me.callParent(arguments);
    }
});
