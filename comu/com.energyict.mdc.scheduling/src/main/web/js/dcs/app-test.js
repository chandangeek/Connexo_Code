Ext.onReady(function () {
    Ext.Loader.addClassPathMappings({
        "Mtr.Application": "app/application.js",
        "ExtThemeNeptune": "ext/packages/ext-theme-neptune/overrides",
        "Ext": "ext/src",
        "Ext.rtl.EventObjectImpl": "ext/src/rtl/EventObject.js",
        "Mtr": "app"
    });

    Ext.Loader.setConfig({
        enabled: true,
        disableCaching: true, // For debug only.
        paths: {
            'Chart': 'packages/Highcharts_Sencha/Chart'
        }
    });

    Ext.application({
        name: 'Jasmine',

        extend: 'Mtr.Application',

        controllers: [],

        launch: function () {
            jasmine.getEnv().addReporter(new jasmine.TrivialReporter());
            jasmine.getEnv().execute();
        }
    });
});