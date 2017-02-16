/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.MdcProxy', {
    override: 'Ext.data.proxy.Rest',

    buildUrl: function (request) {
        //Create a template with the URL and replace the variables
        var me = this,
            url = me.callParent(arguments);

        var urlTemplate = new Ext.Template(url),
            params = request.proxy.extraParams,
            newUrl = urlTemplate.apply(params);


        //Remove variables embedded into URL
        Ext.Object.each(params, function (key, value) {
            var regex = new RegExp('{' + key + '.*?}');
            if (regex.test(url)) {
                delete params[key];
            }
        });

        request.url = url;

        return newUrl;
    }

});
