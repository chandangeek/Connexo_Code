Ext.define('ViewDataCollectionIssues.view.dataCollectionIssuesFilter', {
    extend: 'Ext.panel.Panel',
    requires:[
    	'Ext.form.CheckboxGroup'
    ],
    xtype: 'data-collection-issues-filter',
    store: 'ViewDataCollectionIssues.store.DataCollectionIssuesList',
    border: true,
	name: 'dcifilter',
	title: 'Filters',
	collapsible: false,
	header: false,
	scroll: true,
	items: [{
				xtype: 'label',
				margin: '10 10 10 10',
				html: '<B>Filters</B>',
			},{
				xtype : 'checkboxgroup',
				fieldLabel : 'Status',
				name: 'status',
				layout: 'vbox',
				margin: '10 10 10 10',
				items: [
						{
							boxLabel  : 'Open',
							name      : 'status',
							inputValue: 'open',
				
						 },
						 {
							boxLabel  : 'In progress',
							name      : 'status',
							inputValue: 'inprogress',
				
						 },
						 {
							boxLabel  : 'On hold',
							name      : 'status',
							inputValue: 'onhold',
				
						 },
						 {
							boxLabel  : 'Closed',
							name      : 'status',
							inputValue: 'closed',
						
						 },
						 {
							boxLabel  : 'Rejected',
							name      : 'status',
							inputValue: 'rejected',
					
						 }
					]
				},{
					xtype : 'checkboxgroup',
					fieldLabel : 'Due date',
					name: 'dueDate',
					layout: 'vbox',
					margin: '10 10 10 10',
					items: [
							{
								boxLabel  : 'Overdue',
								name      : 'dueDate',
								inputValue: 'overdue',				
							 },
							 {
								boxLabel  : 'Due today',
								name      : 'dueDate',
								inputValue: 'duetoday',				
							 }						 
						]
				},{
					xtype : 'checkboxgroup',
					fieldLabel : 'Assagnee',
					name: 'assagnee',
					layout: 'vbox',
					margin: '10 10 10 10',
					items: [
							{
								boxLabel  : 'Monique Doe',
								name      : 'assagnee',
								inputValue: 'MoniqueDoe',				
							 },
							 {
								boxLabel  : 'Michael Dell',
								name      : 'assagnee',
								inputValue: 'MichaelDell',				
							 }						 
						]
				},{
						xtype: 'textfield',
						fieldLabel: 'Device',
						name: 'device',
						margin: '10 10 10 10'
					},{
						xtype: 'textfield',
						fieldLabel: 'Location',
						name: 'location',
						margin: '10 10 10 10'
					},{
					xtype : 'checkboxgroup',
					fieldLabel : 'Reason',
					name: 'reason',
					layout: 'vbox',
					margin: '10 10 10 10',
					items: [
							{
								boxLabel  : 'Connection lost',
								name      : 'reason',
								inputValue: 'connectionlost',				
							 },{
								boxLabel  : 'File import failed',
								name      : 'reason',
								inputValue: 'fileimportfailed',				
							 },{
								boxLabel  : 'Unable to connect',
								name      : 'reason',
								inputValue: 'unabletoconnect',				
							 },{
								boxLabel  : 'Web import service down',
								name      : 'reason',
								inputValue: 'webimportservicedown',				
							 },						 
						]
				},{
					xtype : 'checkboxgroup',
					fieldLabel : 'Region',
					layout: 'vbox',
					name: 'region',
					margin: '10 10 10 10',
					items: [
							{
								boxLabel  : 'Oost-Vlaanderen',
								name      : 'region',
								inputValue: 'Oost-Vlaanderen',	
								labelAlign: 'right',
							 },
							 {
								boxLabel  : 'West-Vlaanderen',
								inputValue: 'West-Vlaanderen',
								name      : 'region',
								inputValue: 'open',				
							 }						 
						]
				},{
					xtype: 'button',
					text: 'Apply',
					name: 'apply',
					margin: '10 10 10 10',
					
				},{
					xtype: 'button',
					text: 'Reset',
					name: 'reset',
					margin: '10 10 10 10',
				}
				
	],
	
});