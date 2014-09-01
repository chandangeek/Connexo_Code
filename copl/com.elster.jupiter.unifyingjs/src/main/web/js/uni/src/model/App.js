/**
 * @class Uni.model.App
 */
Ext.define('Uni.model.App', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'url',
        'icon',
        {
            name: 'isActive',
            persist: false,
            convert: function (value, record) {
                var href = window.location.href,
                    pathname = window.location.pathname,
                    fullPath = pathname + window.location.hash;

                return href.indexOf(record.data.url, 0) === 0
                    || fullPath.indexOf(record.data.url, 0) === 0;
            }
        }
    ]
});