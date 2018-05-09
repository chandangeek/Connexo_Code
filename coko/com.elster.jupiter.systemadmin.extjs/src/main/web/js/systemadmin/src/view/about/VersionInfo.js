/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.about.VersionInfo', {
    extend: 'Ext.view.View',
    alias: 'widget.about-version-info',
    store: 'Sam.store.VersionInfo',
    ui: 'large',
    tpl: '<div id="about">'
    + '<tpl for=".">'
    + '<h2>{connexoVersionInfo}</h2>'
    + '</tpl>'
    + '</div>',
    itemSelector: 'div'

});