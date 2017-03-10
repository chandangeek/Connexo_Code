/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comportpoolcomports.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comPortPoolComPortPreview',
    itemId: 'comPortPoolComPortPreview',
    requires: [
        'Mdc.view.setup.comservercomports.forms.TCP',
        'Mdc.view.setup.comservercomports.forms.UDP',
        'Mdc.view.setup.comservercomports.forms.SERIAL',
        'Mdc.view.setup.comservercomports.forms.SERVLET',
        'Mdc.view.setup.comportpoolcomports.ActionMenu'
    ],
    title: '',
    frame: true,
    tools: [
        {
            xtype: 'uni-button-action',
            hidden: true,
            privileges: Mdc.privileges.Communication.admin,
            menu: {
                xtype: 'comPortPoolComPortsActionMenu'
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