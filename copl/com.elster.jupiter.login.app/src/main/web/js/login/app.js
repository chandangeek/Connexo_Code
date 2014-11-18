Ext.onReady(function () {
    Ext.History.init();

    Ext.application({
        name: 'Login',

        extend: 'Login.Application',

        autoCreateViewport: true
    });
});