Ext.define('Isu.view.administration.communicationtasks.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.ext.button.ItemAction',
        'Isu.view.administration.communicationtasks.ActionMenu',
        'Isu.view.administration.communicationtasks.Form'
    ],
    alias: 'widget.communication-tasks-item',
    height: 310,
    hidden: false,
    title: 'Details',
    frame: true,
    tools: [
        {
            xtype: 'button',
            text: 'Actions',
            iconCls: 'x-uni-action-iconA',
            menu: {
                xtype: 'communication-tasks-action-menu'
            }
        }
    ],
    items: {
        xtype: 'communication-tasks-form'
    }
});