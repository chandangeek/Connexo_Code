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
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            hidden: true,
            privileges: Mdc.privileges.Communication.admin,
            itemId: 'actionButton',
            iconCls: 'x-uni-action-iconD',
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