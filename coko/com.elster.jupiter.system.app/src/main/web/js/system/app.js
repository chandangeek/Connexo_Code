Ext.onReady(function () {
    var loader = Ext.create('Uni.Loader');

    // <debug>
    // Used only during development to point to hosted files.
    var packages = [
        {
            name: 'Usr',
            path: '../../apps/usr/src'
        },
        {
            name: 'Sam',
            path: '../../apps/sam/src'
        },
        {
            name: 'Tme',
            path: '../../apps/tme/src'
        },
        {
            name: 'Yfn',
            path: '../../apps/yfn/src'
        },
		{
            name: 'Apr',
            path: '../../apps/apr/src'
        },
        {
            name: 'Fim',
            path: '../../apps/fim/src'
        },
        {
            name: 'Cps',
            path: '../../apps/cps/src'
        }
    ];




    loader.initPackages(packages);
    // </debug>

    Ext.Ajax.on("beforerequest", function(conn){
        var xAuthToken = localStorage.getItem('X-AUTH-TOKEN');
        conn.defaultHeaders.Authorization =  xAuthToken != null ? 'Bearer '.concat(xAuthToken.substr(xAuthToken.lastIndexOf(" ")+1)) : 'Bearer '.concat(xAuthToken);

    });
    Ext.Ajax.on("requestcomplete", function(conn, response){
        localStorage.setItem('X-AUTH-TOKEN',response.getResponseHeader('X-AUTH-TOKEN'));
    });

    loader.onReady(function () {

        Ext.Ajax.defaultHeaders = {
            'X-CONNEXO-APPLICATION-NAME': 'SYS', // a function that return the main application
            'Authorization': 'Bearer ' + localStorage.getItem('X-AUTH-TOKEN')
        };
        // <debug>
        Ext.Loader.setConfig({
            enabled: true
        });
        // </debug>

        Ext.application({
            name: 'SystemApp',
            extend: 'SystemApp.Application',
            autoCreateViewport: true
        });
    });
});

