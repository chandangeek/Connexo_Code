Ext.define('Mdc.customattributesonvaluesobjects.model.AttributeSetOnRegister', {
    extend: 'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject'
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/mds/customattributesets/',

        //setUrl: function (mRID) {
        //    this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        //}
    }
});
