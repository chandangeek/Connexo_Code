/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.AddWebserviceEndpointsGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.add-webservices-grid',

    plugins: [
        {
            ptype: 'bufferedrenderer',
        }
    ],

    counterTextFn: function (count){
        return Uni.I18n.translatePlural('general.nrOfWebserviceEndpoints.selected', count, 'APR',
            'No web service endpoints selected', '{0} web service endpoints selected', '{0} web service endpoints selected'
        );
    },
    bottomToolbarHidden: true,
    checkAllButtonPresent: true,

    columns: [
        {
            header: Uni.I18n.translate('general.webserviceEndpoint', 'APR', 'Web service endpoint'),
            dataIndex: 'name',
            flex: 1
        }
    ]
});
