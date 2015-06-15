/**
 * @class Uni.Loader
 *
 * Loader class for the Unifying JS project, it makes sure every dependency is
 * ready to use.
 */
Ext.define('Uni.Loader', {
    scriptLoadingCount: 0,

    requires: [
        'Ext.tip.QuickTipManager',
        'Ext.layout.container.Absolute',
        'Ext.data.proxy.Rest',
        'Ext.state.CookieProvider',

        'Uni.About',
        'Uni.I18n',
        'Uni.DateTime',
        'Uni.Number',
        'Uni.Auth',

        'Uni.controller.Acknowledgements',
        'Uni.controller.Configuration',
        'Uni.controller.Error',
        'Uni.controller.Navigation',
        'Uni.controller.Notifications',
        'Uni.controller.Portal',
        'Uni.controller.Search',
        'Uni.controller.Session',

        'Uni.view.form.field.Vtypes',
        'Uni.component.filter.view.FilterTopPanel',
        'Uni.form.NestedForm',

        'Uni.override.ServerOverride',
        'Uni.override.ApplicationOverride',
        'Uni.override.ButtonOverride',
        'Uni.override.CheckboxOverride',
        'Uni.override.FieldBaseOverride',
        'Uni.override.FieldContainerOverride',
        'Uni.override.NumberFieldOverride',
        'Uni.override.JsonWriterOverride',
        'Uni.override.RestOverride',
        'Uni.override.StoreOverride',
        'Uni.override.GridPanelOverride',
        'Uni.override.FormOverride',
        'Uni.override.form.field.ComboBoxOverride',
        'Uni.override.ModelOverride',
        'Uni.override.FieldSetOverride',

        'Uni.override.ContainerOverride',

        'Uni.override.form.field.Base',
        'Uni.override.form.field.ComboBoxOverride',
        'Uni.override.form.field.Date',
        'Uni.override.form.field.FieldContainer',
        'Uni.override.form.Label',
        'Uni.override.form.Panel',
        'Uni.override.grid.plugin.BufferedRenderer',
        'Uni.override.grid.Panel',
        'Uni.override.menu.Item',
        'Uni.override.panel.Header',
        'Uni.override.panel.Panel',
        'Uni.override.ux.window.Notification',
        'Uni.override.view.Table',
        'Uni.override.window.MessageBox',
        'Uni.override.form.field.Picker',
        'Uni.form.field.ReadingTypeCombo',
        'Uni.view.grid.BulkSelection',
        'Uni.override.ColumnOverride',
        'Uni.override.DisplayFieldOverride'
    ],

    /**
     * Initializes the internationalization components that should be used during loading.
     *
     * @param {String} components Components to load
     */
    initI18n: function (components) {
        // The I18n singleton is not initialized here because there is no guarantee
        // this method will be called since it is optional.
        Uni.I18n.init(components);
    },

    //<debug>
    /**
     * Used during development to load in paths for packages.
     *
     * @example
     *     [
     *         {
     *             name: 'Cfg',
     *             controller: 'Cfg.controller.Main',
     *             path: '../../apps/cfg/app'
     *         },
     *         {
     *             name: 'Mdc',
     *             controller: 'Mdc.controller.Main',
     *             path: '../../apps/mdc/app'
     *         }
     *     ]
     *
     * @param {Object[]} packages Packages to initialize
     */
    initPackages: function (packages) {
        for (var i = 0; i < packages.length; i++) {
            var pkg = packages[i];
            Ext.Loader.setPath(pkg.name, pkg.path);
        }
    },
    //</debug>

    onReady: function (callback) {
        var me = this;

        me.loadExt();
        me.loadFont();
        me.loadTooltips();
        me.loadStateManager();
        me.loadStores();
        me.loadVtypes();

        callback();
    },

    loadExt: function () {
        Ext.History.init();
    },

    loadFont: function () {
        Ext.setGlyphFontFamily('icomoon');
    },

    loadTooltips: function () {
        Ext.tip.QuickTipManager.init();
    },

    loadStateManager: function () {
        Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));
    },

    loadStores: function () {
        Ext.require('Uni.store.Apps');
        Ext.require('Uni.store.AppItems');
        Ext.require('Uni.store.Notifications');
    },

    loadVtypes: function () {
        Ext.create('Uni.view.form.field.Vtypes').init();
    },

    loadStyleSheet: function (href) {
        var fileref = document.createElement('link');
        fileref.setAttribute('rel', 'stylesheet');
        fileref.setAttribute('type', 'text/css');
        fileref.setAttribute('href', href);

        document.getElementsByTagName('head')[0].appendChild(fileref);
    }

});