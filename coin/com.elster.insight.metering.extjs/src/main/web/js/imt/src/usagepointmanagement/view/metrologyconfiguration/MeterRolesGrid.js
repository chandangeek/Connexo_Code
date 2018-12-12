/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.metrologyconfiguration.MeterRolesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.meter-roles-grid',
    store: null,
    router: null,
    hasLinkMetersButton: true,
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                text: Uni.I18n.translate('general.meterRole', 'IMT', 'Meter role'),
                dataIndex: 'name',
                flex: 1
            },
            {
                text: Uni.I18n.translate('general.required', 'IMT', 'Required'),
                dataIndex: 'required',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : '-';
                }
            },
            {
                text: Uni.I18n.translate('general.meter', 'IMT', 'Meter'),
                dataIndex: 'meter',
                flex: 1,
                renderer: function (value, metaData, record) {
                    var deviceLink;
                    if (value) {
                        if (record.get('url')) {
                            deviceLink = Ext.String.format('<a href="{0}" target="_blank">{1}</a>', record.get('url'), Ext.String.htmlEncode(value));
                        } else {
                            deviceLink = Ext.String.htmlEncode(value);
                        }
                    } else {
                        deviceLink = '-';
                    }
                    return deviceLink;
                }
            },
            {
                text: Uni.I18n.translate('general.activationDate', 'IMT', 'Activation date'),
                dataIndex: 'activationTime',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('metrologyConfigurationDetails.meterRolesCount', 'IMT', '{2} meter role(s)'),
                isFullTotalCount: true,
                noBottomPaging: true,
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.linkMeters', 'IMT', 'Link meters'),
                        itemId: 'edit-meters',
                        privileges: me.hasLinkMetersButton && Imt.privileges.UsagePoint.canAdministrate(),
                        href: me.router.getRoute('usagepoints/view/metrologyconfiguration/activatemeters').buildUrl()
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});

