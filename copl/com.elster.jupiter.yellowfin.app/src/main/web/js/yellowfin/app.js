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

        loader.onReady(function () {

            Ext.Ajax.defaultHeaders = {
                'X-CONNEXO-APPLICATION-NAME': 'YFN' // a function that return the main application
            };

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
        url: '/api/yfn/user/login',
        method: 'POST',
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

            if(data.token == "LICENSE_BREACH"){
                url = data.url +"?LOGIN";
            }
            else{
                url = data.url +"/logon.i4?LoginWebserviceId=" +data.token+"&disablelogoff=true";
            }

            window.location = url;
        },
        failure: function(response) {
            run_loader();
        }
    });

});

