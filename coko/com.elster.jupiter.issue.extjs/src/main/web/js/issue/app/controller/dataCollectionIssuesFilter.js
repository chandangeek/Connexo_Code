Ext.define('ViewDataCollectionIssues.controller.dataCollectionIssuesFilter', {
    extend: 'Ext.app.Controller',
	
	storeName: 'ViewDataCollectionIssues.store.DataCollectionIssuesList',

    init: function () {
        this.control({
            'checkboxgroup': { change: this.filterChange },
			'textfield[name=device]': { change: this.filterChangeTxt },
			'textfield[name=location]': { change: this.filterChangeTxt },
			'button[name=apply]' : { click: this.applyFilter },
			'button[name=reset]' : { click: this.resetFilter },		
        });
		
		this.store = this.getStore(this.storeName);
		this.topFilterPanel = {};
		this.addEvents('filterapplied', 'filterreset');
    },

	valueIsEmpty : function(val){
			return (function(s){
				for (var k in s)
					return false;
				return true;
			})(val);
		},
		
	updateParam : function(paramName, newValue){
			if (this.params == undefined)
				this.params = {};
			if (this.valueIsEmpty(newValue)) {
				delete this.params[paramName];
			}else{
				this.params[paramName] = newValue;
			};
		},
	
    filterChange : function (view, newValue, oldValue) {
			this.updateParam(view.name, newValue[view.name]);
		},
	
	filterChangeTxt : function ( view, newValue , oldValue, eOpts ){
			this.updateParam(view.name, newValue);
		},
			
	applyFilter : function(){
			var store = this.store;
			store.load({params: this.params});
			this.fireEvent('filterapplied',{
											params : this.params,
										});		
		},
	
	resetFilter : function(btn){
			var panel = btn.up(panel);
			panel.items.each(function(item){
				if (!item.is('button'))
					item.setValue('');				
			});
			this.params = {};
			this.fireEvent('filterreset',{
											params : this.params,
										});
		},
	
	resetFilterParameter : function(paramName){			
			delete this.param[paramName];
			panel.items.findBy(function(item){return (item.name == paramName)}).setValue('');						
		},
});
