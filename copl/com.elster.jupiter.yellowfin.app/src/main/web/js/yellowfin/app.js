Ext.onReady(function () {
    Ext.Ajax.request({
        url: '/api/yfn/user/login',
        method: 'POST',
        async: false,
        success: function(response){
            var data = Ext.JSON.decode(response.responseText);
            var url = data.url +"/logon.i4?LoginWebserviceId=" +data.token+"&disablelogoff=true";
            window.location = url;
        },
        failure: function(response) {
            var loader = Ext.create('Uni.Loader');
            loader.onReady(function () {

                Ext.Loader.setConfig({
                    // <debug>
                    enabled: true,
                    // </debug>

                    paths: {

                    }
                });

                Ext.application({
                    name: 'YellowfinApp',
                    extend: 'YellowfinApp.Application',
                    autoCreateViewport: true
                });
            });
        }
    });

});

