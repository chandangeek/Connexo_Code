/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeSorting', {
    extend: 'Uni.view.panel.FilterToolbar',
    alias: 'widget.loadProfileTypeSorting',
    itemId: 'LoadProfileTypeSorting',
    title: Uni.I18n.translate('general.sort','MDC','Sort'),
    name: 'sortitemspanel',
    height: 40,
    emptyText: Uni.I18n.translate('general.none','MDC','None'),
    tools: [
        {
            xtype: 'button',
            action: 'addSort',
            text: Uni.I18n.translate('loadprofiletypes.addSort','MDC','Add sort'),
            menu: {
                name: 'addsortitemmenu'
            }
        }
    ]
});