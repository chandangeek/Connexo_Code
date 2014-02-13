Ext.define('ViewDataCollectionIssues.view.DataCollectionIssues', {
    extend: 'Ext.panel.Panel',
	requires:[
        'ViewDataCollectionIssues.utility.Paging',
        'Ext.form.Label'
    ],
    xtype: 'data-collection-issues-panel',
    store: 'ViewDataCollectionIssues.store.DataCollectionIssuesList',
    border: true,
	header: false,
	collapsible: false,
	border: 1,

	layout: {	
				type: 'vbox',
				align: 'stretch',				
			},
	items: [
				{
					xtype: 'panel',
					header: false,
					border: 0,
					margin: '20 10 10 10',
					items: {
								xtype: 'label',
								style: 'font: normal 20px Calibri',
								html: 'Data Collection Issues',
								margin: '20 10 10 10',							
							}
				},{
						xtype: 'label',
						html: '<hr></hr>',
						margin: '0 10 0 10'
				},{
					xtype: 'panel',
					header: false,
					border: false,
					layout: {
								type: 'hbox',
								align: 'left'
							},
					items: [{
								xtype: 'label',
								style: 'font: normal 16px Calibri',
								html: 'Filters',
								margin: '10 10 10 20'
							}]
				},{
						xtype: 'label',
						html: '<B><hr></hr></B>',
						margin: '0 10 0 10'
				},{
					xtype: 'panel',
					header: false,
					border: false,
					margin: '10 10 10 20',
					minHeight: 60,
					maxHeight: 200,
					layout: {
											type: 'vbox',
											align: 'stretch'
										},
					items: [
							{	
								border: false,
								items: {
											xtype: 'combobox',
											name: 'groupnames',
											labelStyle: 'font: normal 16px Calibri',
											fieldLabel: 'Group',						
											queryMode: 'local',
											displayField: 'display',
											valueField: 'Value',
											labelWidth: 60,
											height: 20,
		
							}
							},{
								xtype: 'label',
								style: 'font: normal 16px Calibri',
								html: 'No group selected ...',
								margin: '10 10 10 10',
								hidden: false,

							},{
								xtype: 'gridpanel',
								name: 'groupgrid',
								margin: '10 2 2 2',
								hidden: true,
								store: 'ViewDataCollectionIssues.store.GroupStore',
								border: 1,
								columns: [	{
												text: 'Reason',
												dataIndex : 'reason',
												flex: 5,
											},{
												text: 'Issues',
												dataIndex : 'number',
												flex: 1,
											}],
								bbar: {
										xtype: 'pagingpanel',
										dock: 'bottom',
										store: 'ViewDataCollectionIssues.store.GroupStore',
										border: 1,
										perPageValues: [5,10,20,30,50],
										ui: 'footer',
										perPageText: 'Items per page'
									}
								},]			
							
							
						
				},{
					xtype: 'label',
					name: 'forissuesline',
					html: '<B><hr></hr></B>',
					hidden : true,
					margin: '0 10 0 10'											
				},{
					xtype: 'panel',
					style: 'font: normal 16px Calibri',
					name: 'issuesforlabel',
					defaults: {	style: 'font: normal 16px Calibri',
								border: 0},
					margin: '10 10 10 20',
					border: 0,
					hidden: true,
				},{
						xtype: 'label',
						html: '<B><hr></hr></B>',
						margin: '0 10 0 10',
				},{
					xtype: 'panel',
					header: false,
					border: false,
					flex: 1,
					margin: '2 0 0 0',
					layout: {
								type: 'hbox',
								align: 'stretch'
							},
					 
					items: [{
								xtype: 'label',
								style: 'font: normal 16px Calibri',
								html: 'Sort',
								margin: '10 10 10 20'
							},{	
								xtype: 'panel',
								border: false,
								name: 'sortitemspanel',
								flex: 1,
								layout: {
											type: 'hbox',
											align: 'left'
										},
								items: [
										{
											xtype: 'button',
											name: 'addsortbtn',
											text: '+ Add sort',
											margin: '10 5 0 10',
											menu:{ name: 'addsortitemmenu' },
										}
										],
							},{	
								xtype: 'panel',
								border: false,
								margin: '3 10 10 10',
								items: {	
											xtype: 'button',
											name: 'clearsortbtn',
											margin: '10 10 10 10',
											text: 'Clear all'
										}
							},
								
								
							],
					}
					
		],	
		

});