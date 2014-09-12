Ext.define('Mdc.view.setup.comserver.SubMenu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.comserversubmenu',
    ui: 'side-menu',
    items: [
        {
            text: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
            itemId: 'comServerOverviewLink',
            href: '#/administration/comservers/{0}/overview',
            hrefTarget: '_self'
        },
        {
            text: Uni.I18n.translate('comserver.preview.communicationPortsLabel', 'MDC', 'Communication ports'),
            itemId: 'comServerComPortsLink',
            href: '#/administration/comservers/{0}/comports',
            hrefTarget: '_self'
        }
    ],

    setServer: function (model) {
        var id = model.getId(),
            name = model.get('name'),
            currentHash = window.location.hash;

        this.setTitle(name);

        Ext.Array.each(this.query('menuitem'), function (item) {
            var href = item.href,
                formatHref = Ext.String.format(href, id);

            item.setHref(formatHref);
            (currentHash == formatHref) && item.addCls('current');
        });
    }
});