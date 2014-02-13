Ext.define('ViewDataCollectionIssues.controller.DataCollectionIssues', {
     extend: 'Ext.app.Controller',

    init: function () {
        this.control({
						'controller' : 					{ 	filterapplied : this.onFilterApplied,
															filterreset : this.onFilterReset 		},
						'button[name=addsortbtn]' : 	{ 	click: this.setAddSortMenu 				},
						'menu[name=addsortitemmenu]' : 	{	click : this.addSortItem 				},
						'combobox[name=groupnames]' : 	{	afterrender : this.setGroupFields,
															change: this.setGroup 					},
						'grid[name=groupgrid]' : 		{ 	itemclick : this.getIssuesForGroup 		},
						'button[name=clearsortbtn]' : 	{ 	click : this.clearSort					},
						'panel[name=sortrulepanel]' : 	{ 	close : this.deleteSortItem				},
						'header[name=sortrulepanel]':	{	click : this.changeSortOrder}
							
			});	
		
		

		this.groupStore = this.getStore('Mtr.store.GroupStore');	
		this.store = this.getStore('Mtr.store.DataCollectionIssuesList');
		this.groupParams = {};
		this.sortParams = {};

    },

	updateIssueList : function(){
			var extraParams = {};
			if (this.sortParams != undefined || this.groupParams != undefined){
					Ext.merge(extraParams, this.groupParams);
					Ext.merge(extraParams, this.sortParams);
				}
			this.store.proxy.extraParams = extraParams;
			this.store.load();
		},
	
	
//====  SORT  ===============================================================================================================================	
	
	applySort : function(view){
			var me = this;
			this.sortParams = {};
			view.items.each(function(item){
					if (item.name == 'sortrulepanel')
						me.sortParams.sort = item.sortValue;
				})
			me.updateIssueList();
		},
		
	addSortItem : function( menu, item, e, eOpts){
			var btn = menu.up('button'),
			panel = btn.up('panel'),
			index = panel.items.getCount() - 1;
			sortItem = {	
							xtype: 'panel',
							sortValue: item.value,
							margin: '10 5 10 5',
							style: {borderRadius: 5 },
							name: 'sortrulepanel',
							header: { 	
										cls: 'x-btn',
										title: item.text,
										name: 'sortrulepanel',
										style: {
											'border-radius': '5px',
											padding: '2px 3px 4px 6px'
										}
									},
							closable: true,
							collapsed: true,
							width: 150,
						};
						
			var sortItem = panel.insert(index, sortItem);
			this.applySort(panel);		
					
		/*	var overrides = {
							b4StartDrag: function () {
							console.log(this);
									if (!this.el) {
											this.el = Ext.get(this.getEl());
										}
									this.originalXY = this.el.getXY();
								},
							
							onInvalidDrop: function () {
									console.log(this);
									this.invalidDrop = true;
								},
							
							endDrag: function () {
								console.log(this);
								if (this.invalidDrop === true) {
										this.el.moveTo(this.originalXY[0], this.originalXY[1]);
										delete this.invalidDrop;
									}
							},

							onDragDrop: function (evtObj, targetElId) {
								console.log(this);
								var dropEl = Ext.get(targetElId);
								if (this.el.dom.parentNode.id == targetElId) {
										dropEl.appendChild(this.el);
										this.onDragOut(evtObj, targetElId);
										this.el.dom.style.right = '';
										this.el.dom.style.top = '';
										this.el.dom.style.left = '';
									}else {
										this.onInvalidDrop();
									}
								}
							};
							
				//	var butA = Ext.get(panel.getId() + '-innerCt').select('a');		
					console.log(sortItem.getEl());
					var dd = new Ext.dd.DD(sortItem.getEl().dom, 'buttonsDDGroup', {
							isTarget: false
						});
						
					console.log(dd);
					Ext.apply(dd, overrides);	
			
			var buttonArray = Ext.ComponentQuery.query('splitbutton[name=sortrulebtn]');

			Ext.Array.each(buttonArray, function (el) {						
					var dd = new Ext.dd.DD(el, 'buttonsDDGroup', {
							isTarget: false
						});
					console.log(dd);
					Ext.apply(dd, overrides);			
				});

					console.log(panel.getId() + '-innerCt');
					if (this.sortBtnsDDTarget == undefined)
						this.sortBtnsDDTarget = new Ext.dd.DDTarget(panel.getId() + '-innerCt', 'buttonsDDGroup');	
	*/			
				},
	
	changeSortOrder : function(header, e, eOpts){
			if (header.sortOrder == 'asc'){
				header.sortOrder = 'desc';
			}else{
				header.sortOrder = 'asc';
			};
		},

	setAddSortMenu : function(btn){
			var pan = btn.up('panel');
			var model =  Ext.ModelManager.getModel('Mtr.model.DataCollectionIssue'),
			fields = model.getFields();
			if (btn.menu.items.getCount() < 1){
					var menu = [];
					for (var c = 0; c < fields.length; c++) {				
							item = {};					
							item.text = fields[c].displayValue;
							item.value = fields[c].name;
							item.name = 'addsortmenuitem';				
							menu.push(item);					
						};
					btn.menu.add(menu);	
					btn.showMenu();
				};
		},
		
	clearSort : function( btn, e, eOpts){
			var pan = btn.up('panel').up('panel').down('panel[name=sortitemspanel]');			
			pan.items.each(function(item){
						if (item.name == 'sortrulepanel'){
								pan.remove(item, true);
							}	
				});
			this.applySort(pan);
		},
		
	deleteSortItem : function( pan,  eOpts){
			var view = pan.up('panel')
			panel = view.up('panel'),
			panel.remove(pan);
			this.applySort(panel);
		},
		
//===============================================================================================================================================	

//====  FILTER  ============================================================================================================================

	onFilterApplied : function(params){
			for (key in params) {
					
				};
		},

	onFilterReset : function(params){
			
		},
	
	applyFilter : function(params){
			
		},
	
	resetFilter: function(params){
			
		},
	
	removeFilterItem : function(params){
			
		},
//=======================================================================================================================================

//=====  GROUP  =========================================================================================================================	
	
		
	setGroupFields : function(view){
	
				var model = Ext.ModelManager.getModel('Mtr.model.DataCollectionIssue'),
				fields = model.getFields();
		
				var data = [{ Value: 'none', display: '(none)'}];
				
				Ext.Array.each(fields, function(field){
						var rec = { Value: field.name, display: field.displayValue };
						data.push(rec);
					});	
					
				groupStore = Ext.create('Ext.data.Store', {
								fields: ['Value', 'display'],
								data : data,
							});
							
				view.store = groupStore;
			},
	// combobox : change		
	setGroup : function(view, newValue, oldValue, eOpts){
			var me = this;
			var pan = view.up('panel').up('panel').down('grid'),
			label = view.up('panel').up('panel').down('label'),
			grid = view.up('panel').up('panel').down('grid'),
			issuesFor = Ext.ComponentQuery.query('data-collection-issues-panel panel[name=issuesforlabel]')[0],
			lineLabel = Ext.ComponentQuery.query('data-collection-issues-panel label[name=forissuesline]')[0];

			if (newValue != 'none'){				
					var extraParams = {reason: newValue}
					this.groupStore.proxy.extraParams = extraParams;
					this.groupStore.load();
					this.group = newValue;
					if (!pan.isVisible())
						pan.show();				
						label.hide();
				}else{
					this.groupStore.removeAll();
					if (pan.isVisible())
						pan.hide();
						label.show();
						issuesFor.hide();
						lineLabel.hide();
						me.groupParams = {};
						this.updateIssueList();
				};
		},
		
	getIssuesForGroup : function( grid, record, item, index, e, eOpts){
			var me = this,
			iString = 'Issues for ' + this.group + ': ' + record.data.reason,
			extraParams = {},			
			issuesFor = Ext.ComponentQuery.query('data-collection-issues-panel panel[name=issuesforlabel]')[0],
			lineLabel = Ext.ComponentQuery.query('data-collection-issues-panel label[name=forissuesline]')[0];
			
			issuesFor.removeAll();
			issuesFor.add({html: iString});
			issuesFor.show();
			lineLabel.show();
			
			me.groupParams[this.group] = record.data.reason;
			me.updateIssueList();					
		},

//===============================================================================================================================================
		
		
});
