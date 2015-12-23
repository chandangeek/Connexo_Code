Ext.define('Imt.registerdata.store.Register', {
	extend: 'Uni.data.store.Filterable',
    model: 'Imt.registerdata.model.Register',
    storeId: 'register',
    autoLoad: false,
	proxy: {
	    type: 'rest',
	    urlTpl: '/api/udr/usagepoints/{mRID}/registers',
	    timeout: 240000,
	    reader: {
	        type: 'json',
	        root: 'registers'
	    },
	    setUrl: function (mRID) {
	        this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
	    },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
	}

});