Ext.onReady(function () {
    var loader = Ext.create('Uni.Loader');

    // <debug>
    var packages = [
        {
            name: 'Cfg',
            path: '../../apps/cfg/src'
        },
        {
            name: 'Tme',
            path: '../../apps/tme/src'
        },
        {
            name: 'Mdc',
            path: '../../apps/mdc/src'
        },
        {
            name: 'Isu',
            path: '../../apps/isu/src'
        },
        {
            name: 'Idc',
            path: '../../apps/idc/src'
        },
        {
            name: 'Dvi',
            path: '../../apps/dvi/src'
        },
        {
            name: 'Dsh',
            path: '../../apps/dsh/src'
        },
        {
            name: 'Yfn',
            path: '../../apps/yfn/src'
        },
        {
            name: 'Fwc',
            path: '../../apps/fwc/src'
        },
        {
            name: 'Dlc',
            path: '../../apps/dlc/src'
        },
        {
            name: 'Dxp',
            path: '../../apps/dxp/src'
        }

    ];

    loader.initPackages(packages);
    // </debug>

    loader.onReady(function () {
        Ext.Loader.setConfig({
            // <debug>
            enabled: true,
            // </debug>

            paths: {
                'Ext.ux.form': '../uni/packages/Ext.ux.form',
                'Ext.ux.Rixo': '../uni/packages/Ext.ux.Rixo',
                'Ext.ux.window': '../uni/packages/Ext.ux.window.Notification'
            }
        });

        Ext.application({
            name: 'MdcApp',
            extend: 'MdcApp.Application',
            autoCreateViewport: true
        });
    });
});

