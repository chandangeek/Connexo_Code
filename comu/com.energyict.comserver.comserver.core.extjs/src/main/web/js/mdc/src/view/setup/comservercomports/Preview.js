/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comservercomports.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comServerComPortPreview',
    itemId: 'comServerComPortPreview',
    requires: [
        'Mdc.view.setup.comservercomports.forms.TCP',
        'Mdc.view.setup.comservercomports.forms.UDP',
        'Mdc.view.setup.comservercomports.forms.SERIAL',
        'Mdc.view.setup.comservercomports.forms.SERVLET',
        'Mdc.view.setup.comservercomports.ActionMenu'
    ],
    title: '',
    frame: true,
    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.Communication.admin,
            menu: {
                xtype: 'comServerComPortsActionMenu'
            }
        }
    ],
    defaults: {
        hidden: true
    },
    items: [
        {
            xtype: 'comPortFormTCP'
        },
        {
            xtype: 'comPortFormUDP'
        },
        {
            xtype: 'comPortFormSERIAL'
        },
        {
            xtype: 'comPortFormSERVLET'
        }
    ]
});