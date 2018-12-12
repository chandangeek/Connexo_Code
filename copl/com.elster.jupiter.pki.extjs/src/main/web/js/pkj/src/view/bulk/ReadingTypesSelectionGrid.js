/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pkj.view.bulk.ReadingTypesSelectionGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'certificates-selection-grid',
    store: 'Pkj.store.CertificatesBulk',

    allLabel: Uni.I18n.translate('certificates.bulk.allcertificatestitle', 'PKJ', 'All certificates'),
    allDescription: Uni.I18n.translate('certificates.bulk.allcertificatesMsg', 'PKJ', 'Select all certificates (related to filters on previous screen)'),

    selectedLabel: Uni.I18n.translate('readingtypesmanagment.bulk.selectedreadingtypes', 'PKJ', 'Selected certificates'),
    selectedDescription: Uni.I18n.translate('readingtypesmanagment.bulk.selectedreadingtypesMsg', 'PKJ', 'Select certificates in table below'),

    cancelHref: '#/administration/certificates',

    radioGroupName: 'certificates-selection-grid-step1',

    columns: [
        {
            header: Uni.I18n.translate('general.alias', 'PKJ', 'Alias'),
            dataIndex: 'alias',
            flex: 2
        },
        {
            header: Uni.I18n.translate('general.issuer', 'PKJ', 'Issuer'),
            dataIndex: 'issuer',
            flex: 3
        },
        {
            header: Uni.I18n.translate('general.subject', 'PKJ', 'Subject'),
            dataIndex: 'subject',
            flex: 3
        },
        {
            header: Uni.I18n.translate('general.status', 'PKJ', 'Status'),
            dataIndex: 'status',
            flex: 1,
            renderer: function(value) {
                return value ? value.name : value;
            }
        },
        {
            header: Uni.I18n.translate('general.expirationDate', 'PKJ', 'Expiration date'),
            dataIndex: 'expirationDate',
            flex: 1,
            renderer: function(value) {
                if (Ext.isEmpty(value)) {
                    return '-';
                }
                return Uni.DateTime.formatDateShort(new Date(value));
            }
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
        this.onChangeSelectionGroupType();
        this.getBottomToolbar().setVisible(false);
    }
});
