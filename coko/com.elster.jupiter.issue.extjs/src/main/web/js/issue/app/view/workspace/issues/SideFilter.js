Ext.define('Isu.view.workspace.issues.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issues-side-filter',
    title: 'Filter',
    cls: 'filter-form',
    width: 180,

    requires: [
        'Isu.view.workspace.issues.component.AssigneeCombo',
        'Isu.util.FilterCheckboxgroup'
    ],

    items: [
        {
            xtype: 'form',
            items: [
                {
                    xtype: 'filter-checkboxgroup',
                    store: 'Isu.store.IssueStatus',
                    title: '<b>Status</b>'
                }
            ]
        },
        {
            xtype: 'issues-assignee-combo'
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