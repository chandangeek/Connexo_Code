/**
 * Config loader
 */
Ext.define('Isu.util.Config', {
    urls: [
        'resources/config/application.json',
        'resources/config/application.local.json'
    ],
    requires: [
        'Ext.Ajax'
    ],

    load: function(config) {
        _.each(config, function(item, key) {
            var obj = Ext.decode(key);

            if (obj) {
                Ext.override(obj, item);
            }
        });
    },

    /**
     * todo: refactor this
     * @param callback
     */
    onReady: function(callback) {
        var me = this;

        Ext.Ajax.request({
            url: 'resources/config/application.local.json',
            success: function(response, opts) {
                if (response.responseText) {
                    var config = Ext.decode(response.responseText);
                    me.load(config);
                }
                callback();
            },

            failure: function(response, opts) {
            }
        });
    }
});