/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.navigation.HelpBtn
 */
Ext.define('Uni.view.navigation.HelpBtn', {
    extend: 'Ext.menu.Item',
    alias: 'widget.online-help-btn',
    text: Uni.I18n.translate('general.help', 'UNI', 'Help'),
    hrefTarget: '_blank',
    href: 'help/index.html'
});