/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.TrustStorePreviewForm', {
    extend: 'Ext.form.Panel',
    frame: false,
    layout: 'fit',
    alias: 'widget.truststore-preview-form',

    requires: [
        'Uni.util.FormEmptyMessage'
    ],

    items: [
        {
            defaults: {
                xtype: 'displayfield'
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.name', 'PKJ', 'Name'),
                    name: 'name'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.description', 'PKJ', 'Description'),
                    name: 'description'
                }
            ]
        }
    ]
});
