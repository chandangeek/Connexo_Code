Ext.define('Imt.registerdata.store.Register', {
    extend: 'Ext.data.Store',
    model: 'Imt.registerdata.model.Register',
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
	    }
	}

});