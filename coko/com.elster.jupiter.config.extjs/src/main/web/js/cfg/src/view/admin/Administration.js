/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.admin.Administration', {
            extend: 'Ext.panel.Panel',
            alias: 'widget.administration',
            itemId: 'administration',
            requires: [
            ],
            layout: {
                type: 'vbox',
                align: 'stretch'
            },

            items: [
                {
                    xtype: 'component',
                    html: '<h3><a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#/administration/validation">Validation rule sets</a></h3>',
                    margin: '30 30 30 30'
                }
            ],

            initComponent: function () {
                this.callParent(arguments);
    }
});
