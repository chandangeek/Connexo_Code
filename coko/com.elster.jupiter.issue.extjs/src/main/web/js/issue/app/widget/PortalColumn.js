/**
 * @class Mtr.widget.PortalColumn
 * @extends Ext.container.Container
 * A layout column class used internally be {@link Mtr.widget.PortalPanel}.
 */
Ext.define('Mtr.widget.PortalColumn', {
    extend: 'Ext.container.Container',
    alias: 'widget.portalcolumn',

    requires: [
        'Ext.layout.container.Anchor',
        'Mtr.widget.Portlet'
    ],

    layout: 'anchor',
    defaultType: 'portlet',
    cls: 'x-portal-column'

    // This is a class so that it could be easily extended
    // if necessary to provide additional behavior.
});