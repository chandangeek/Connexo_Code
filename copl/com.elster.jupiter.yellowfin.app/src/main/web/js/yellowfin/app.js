Ext.onReady(function () {
    var run_loader = function(){
        var loader = Ext.create('Uni.Loader');

        var packages = [
            {
                name: 'Yfn',
                path: '../../apps/yfn/src'
            }
        ];
        loader.initPackages(packages);

        Ext.Ajax.on("beforerequest", function(conn){
            var xAuthToken = localStorage.getItem('X-AUTH-TOKEN');
            conn.defaultHeaders.Authorization =  xAuthToken != null ? 'Bearer '.concat(xAuthToken.substr(xAuthToken.lastIndexOf(" ")+1)) : xAuthToken;
        });
        Ext.Ajax.on("requestcomplete", function(conn, response){
            localStorage.setItem('X-AUTH-TOKEN',response.getResponseHeader('X-AUTH-TOKEN'));
        });

        loader.onReady(function () {
            if(localStorage.getItem('X-AUTH-TOKEN')){
                Ext.Ajax.defaultHeaders = {
                    'X-CONNEXO-APPLICATION-NAME': 'YFN', // a function that return the main application
                    'Authorization': 'Bearer ' + localStorage.getItem('X-AUTH-TOKEN')
                };
            }else{
                Ext.Ajax.defaultHeaders = {
                    'X-CONNEXO-APPLICATION-NAME': 'YFN'
                };
            }

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
    } ;


    Ext.Ajax.request({
        url: '/api/yfn/user/url',
        method: 'GET',
        async: false,
        success: function(response){
            var data = Ext.JSON.decode(response.responseText);
            var url = data.url;
            var parts = window.location.href.split("#");
            if((parts.length>1) && parts[1].length>0){
                Ext.Loader.injectScriptElement(data.url+'/JsAPI', function(){}, function(){});
                Ext.Loader.injectScriptElement(data.url+'/JsAPI?api=reports', function(){}, function(){});

                run_loader();
                return;
            }

            window.location = url;
        },
        failure: function(response) {
            run_loader();
        }
    });

});

