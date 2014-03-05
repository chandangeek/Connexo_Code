Ext.define('Mtr.view.workspace.datacollection.issueassignmentrules.FilterView', {
    extend: 'Ext.container.Container',
    alias: 'widget.issues-assignment-rules-filter-view',
    layout: 'hbox',
    defaults: {
        xtype: 'container'
    },
    items: [
        {
            html: '<b>Filter</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;None',
            flex: 1
        },
        {
            xtype: 'button',
            text: 'Clear all',
            disabled: true
        }
    ]
});