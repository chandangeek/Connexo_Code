/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.securityaccessors.view.AvailableSecurityAccessorsGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.available-security-accessors-grd',
    requires: [
        'Mdc.securityaccessors.store.UnusedSecurityAccessors'
    ],
    store: 'Mdc.securityaccessors.store.UnusedSecurityAccessors',
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('securityaccessors.nrOfMessageServices.selected', count, 'MDC',
            'No security accessors selected', '{0} security accessors selected', '{0} security accessors selected'
        );
    },

    bottomToolbarHidden: true,
    extraTopToolbarComponent: [
        '->',
        {
            xtype: 'button',
            itemId: 'manageSecurityAccessors',
            text: Uni.I18n.translate('general.manageSecurityAccessors', 'MDC', 'Manage security accessors'),
            action: 'manageSecurityAccessors',
            margin: '0 0 0 8',
            href: '#/administration/securityaccessors',
            ui: 'link',
            region: 'east'
        }
    ],


    columns: [
        {
            header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
            dataIndex: 'name',
            flex: 1
        },
        {
            header: Uni.I18n.translate('general.accessorType', 'MDC', 'Accessor type'),
            dataIndex: 'isKey',
            flex: 1,
            renderer: function(value) {
                return value
                    ? Uni.I18n.translate('general.key', 'MDC', 'Key')
                    : Uni.I18n.translate('general.certificate', 'MDC', 'Certificate');
            }
        },
        {
            header: Uni.I18n.translate('general.type', 'MDC', 'Type'),
            dataIndex: 'keyType',
            flex: 2,
            renderer: function (value) {
                return Ext.isEmpty(value) || Ext.isEmpty(value.name) ? '-' : value.name;
            }
        },
        {
            header: Uni.I18n.translate('general.validityPeriod', 'MDC', 'Validity period'),
            dataIndex: 'duration',
            flex: 1,
            renderer: function (val) {
                return Ext.isEmpty(val) ? '-' : val.count + ' ' + val.localizedTimeUnit;
            }
        }
    ]
});


