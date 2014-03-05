Ext.define('Mtr.view.workspace.datacollection.issueassignmentrules.FilterAction', {
    extend: 'Ext.container.Container',
    alias: 'widget.issues-assignment-rules-filter-action',
    defaults: {
        xtype: 'container'
    },
    items: [
        {
            html: '<span class="isu-header-filter">Filter <span class="isu-icon-filter"></span></span>'
        },
        {
            html: "<b>Status</b>",
            margin: '15 0 10 0'
        },
        {
            xtype: 'checkboxfield',
            boxLabel: 'Enabled'
        },
        {
            xtype: 'checkboxfield',
            boxLabel: 'Disabled'
        },
        {
            xtype: 'container',
            margin: '10 0 0',
            items: [
                {
                    xtype: 'button',
                    text: 'Apply',
                    margin: '0 10 0 0'
                },
                {
                    xtype: 'button',
                    text: 'Reset'
                }
            ]
        }
    ]
});