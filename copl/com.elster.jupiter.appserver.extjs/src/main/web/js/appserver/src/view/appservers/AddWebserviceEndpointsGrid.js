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
            'No webservice endpoints selected', '{0} webservice endpoints selected', '{0} webservice endpoints selected'
        );
    },
    bottomToolbarHidden: true,
    checkAllButtonPresent: true,

    columns: [
        {
            header: Uni.I18n.translate('general.webserviceEndpoint', 'APR', 'Webservice endpoint'),
            dataIndex: 'name',
            flex: 1
        }
    ]
});
