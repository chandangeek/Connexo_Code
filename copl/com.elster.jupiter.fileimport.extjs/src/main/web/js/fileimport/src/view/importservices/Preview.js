/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.view.importservices.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.fim-import-service-preview',
    requires: [
        'Fim.view.importservices.PreviewForm'
    ],
    tools: [
        {
            xtype: 'uni-button-action',
            menu: {
                xtype: 'fim-import-service-action-menu'
            }
        }
    ],
    items: {
        xtype: 'fim-import-service-preview-form',
        itemId: 'pnl-import-service-preview-form'
    }
});
