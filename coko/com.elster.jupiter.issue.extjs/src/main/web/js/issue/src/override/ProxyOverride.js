/**
 * @class Uni.override.ProxyOverride
 */
Ext.define('Isu.override.ProxyOverride', {
    override: 'Ext.data.proxy.Server',

    buildUrl: function(request) {
        var url = this.callParent([request]);
        url = Ext.Loader.getBasePath() + url;

        return url;
    }

//    headers : {
//        Authorization : 'Basic ' + window.btoa([this.login, this.password].join(':'))
//    }
});