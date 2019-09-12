/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.PrivilegesInfoPanel', {
    extend: 'Ext.window.Window',
    alias: 'widget.comtask-privileges-info-panel',
    width: 700,
    height: 500,
    closable: true,
    constrain: true,
    autoShow: true,
    modal: true,
    floating: true,
    layout: 'fit',
    closeAction: 'destroy',
    title: Uni.I18n.translate('comtask.privileges.infoPanel.title', 'MDC', 'Privileges'),
    items: {
        xtype: 'dataview',
        store: 'Mdc.store.CommunicationTaskPrivileges',
        emptyText: Uni.I18n.translate('general.notFound', 'MDC', 'Not found'),
        itemSelector: 'table.privileges',
        tpl: new Ext.XTemplate(
            '<table class="privileges" style="width: 100%; border-spacing: 0 35px;">',
            '<tpl for=".">',
            '<tr>',
            '<td style="width: 50%; vertical-align: top;">{name}</td>',
            '<td style="width: 50%; vertical-align: top;">',
            '<tpl for="roles">',
            '<p style="margin-top: 0;">- {.}</p>',
            '</tpl>',
            '</td>',
            '</tr>',
            '</tpl>',
            '</table>'
        )
    }
});