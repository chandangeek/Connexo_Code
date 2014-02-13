Ext.define('ViewDataCollectionIssues.Application', {
    name: 'ViewDataCollectionIssues',

    extend: 'Ext.app.Application',

    views: [ 
        // TODO: add views here
    ],

    controllers: [
        'ViewDataCollectionIssues.controller.IssuesTable',
		'ViewDataCollectionIssues.controller.dataCollectionIssuesFilter',
		'ViewDataCollectionIssues.controller.DataCollectionIssues',
    ],

    stores: [
        'ViewDataCollectionIssues.store.DataCollectionIssuesList',
        'ViewDataCollectionIssues.store.PerPage',
		'ViewDataCollectionIssues.store.GroupStore'
		
    ]
});
