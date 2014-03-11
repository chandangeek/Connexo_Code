Ext.define('Isu.view.workspace.issues.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issues-side-filter',
    title: 'Filter',
    cls: 'filter-form',
    width: 180,

    items: [
        {
            xtype: 'form',
            items: [
                {
                    xtype: 'checkboxfield',
                    boxLabel: 'Open'
                },
                {
                    xtype: 'checkboxfield',
                    boxLabel: 'In progress'
                },
                {
                    xtype: 'checkboxfield',
                    boxLabel: 'On hold'
                },
                {
                    xtype: 'checkboxfield',
                    boxLabel: 'Closed'
                },
                {
                    xtype: 'checkboxfield',
                    boxLabel: 'Projected'
                }
            ]
        }
    ],

    buttons: [
        {
            text: 'Apply',
            action: 'filter'
        },
        {
            text: 'Reset',
            action: 'reset'
        }
    ]
});