/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.about.Dynamic', {
    extend: 'Ext.view.View',
    alias: 'widget.about-dynamic-info',
    store: 'Sam.store.AvailableAndLicensedApplications',
    html: '<p>' + Uni.I18n.translate('about.dynamic.licensedApplications', 'SAM', 'Licensed applications', false) + ':</p>',
    tpl: '<ul>'
    + '<tpl for=".">'
    + '<li>{name} {version}</li>'
    + '</tpl>'
    + '</ul>',
    itemSelector: 'li'
});