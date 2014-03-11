Ext.define('Isu.view.workspace.datacollection.issueassignmentrules.Navigation', {
    extend: 'Ext.container.Container',
    alias: 'widget.issues-assignment-rules-navigation',
    requires: [
        'Ext.layout.container.Accordion'
    ],
    layout: {
        type: 'accordion',
        titleCollapse: false,
        animate: true,
        activeOnTop: false
    },
    style: {
        backgroundColor: 'transparent',
        padding: 0
    },
    defaults: {
        bodyStyle: 'background-color: transparent'
    },
    items: [
        {
            title: 'Data collection',
            html: '<ul class="isu-nav">' +
                '<li class="isu-nav-item">' +
                '<a class="isu-nav-item-link" href="#/workspace/datacollection/issues">Issues</a>' +
                '</li>' +
                '<li class="isu-nav-item current">'+
                '<a class="isu-nav-item-link" href="#/workspace/datacollection/issues/assignmentrules">Issue assignment rules</a>' +
                '</li>' +
                '</ul>'
        },
        {
            title: 'Data validation'
        }
    ]
});