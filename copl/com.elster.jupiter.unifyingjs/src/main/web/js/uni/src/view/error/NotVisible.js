/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.error.NotVisible', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.errorNotVisible',
    itemId: 'errorNotVisible',
    overflowY: 'auto',
    requires: [
        'Ext.panel.Panel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('error.pageNotVisibleTitle', 'UNI', "Sorry! We can't show you this."),
            layout: {
                type: 'fit',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'no-items-found-panel',
                    title: '<span style="color: #eb5642">' + Uni.I18n.translate('error.pageNotVisible', 'UNI', 'Page not visible') + '</span>',
                    reasons: [
                        Uni.I18n.translate('error.pageNotVisibleAuthorized', 'UNI', 'You are not authorized to access this page.'),
                        Uni.I18n.translate('error.pageNotVisibleLicenseExpired', 'UNI', 'Your license has expired.')
                    ],
                    stepItems: []
                }
            ]
        }
    ]
});