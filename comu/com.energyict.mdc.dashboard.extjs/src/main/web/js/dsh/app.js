Ext.Loader.setConfig({
    paths: {
        'Uni': "../../../../../../../com.elster.jupiter.unifyingjs/src/main/web/js/uni/src"
    }
});

Ext.application({
    name: 'Dsh',
    extend: 'Dsh.Application',
    autoCreateViewport: true
});