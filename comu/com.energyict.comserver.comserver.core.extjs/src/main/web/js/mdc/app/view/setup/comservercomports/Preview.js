Ext.define('Mdc.view.setup.comservercomports.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comServerComPortPreview',
    itemId: 'issues-item',
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
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            itemId: 'actionButton',
            iconCls: 'x-uni-action-iconD',
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