Ext.define('Mdc.view.setup.comtasks.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Mdc.view.setup.comtasks.ActionMenu',
        'Mdc.view.setup.comtasks.Form'
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
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'communication-tasks-action-menu'
            }
        }
    ],
    items: {
        xtype: 'communication-tasks-form'
    }
});