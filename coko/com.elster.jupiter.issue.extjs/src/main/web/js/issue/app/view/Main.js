Ext.define('ViewDataCollectionIssues.view.Main', {
    extend: 'Ext.container.Container',
    requires:[
        'Ext.layout.container.Border',
        'ViewDataCollectionIssues.view.ViewDataCollectionIssues',
        'ViewDataCollectionIssues.view.ViewDataCollectionIssuesBlank',
		'ViewDataCollectionIssues.view.dataCollectionIssuesFilter',
        'ViewDataCollectionIssues.view.DataCollectionIssues',
		'ViewDataCollectionIssues.view.ViewDataCollectionIssuesItem'
    ],
    
    xtype: 'app-main',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    defaults: {
        border: false,
        margin: '5 5 5 0',
    },

    items: [{
        xtype: 'data-collection-issues-panel'
    },{
        xtype: 'view-data-collection-issues-blank',
        bodyPadding: 10
    },{
        xtype: 'view-data-collection-issues'
    },{
        xtype: 'view-data-collection-issues-item',
        bodyPadding: 10
    }]
});