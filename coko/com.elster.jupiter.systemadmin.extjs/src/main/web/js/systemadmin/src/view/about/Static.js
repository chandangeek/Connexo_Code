Ext.define('Sam.view.about.Static', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.about-static-info',
    html: '<p>' + Uni.I18n.translate('about.static.copyright', 'SAM', 'Copyright &copy; 2015 EnergyICT NV. All rights reserved.', false) + '</p>'
    + '<p>' + Uni.I18n.translate('about.static.part.1', 'SAM', 'The use of this product is subject to the terms of the Elster EnergyICT Software License Agreement, unless otherwise agreed or specified', false) + '</p>'
    + '<p>' + Uni.I18n.translate('about.static.part.2', 'SAM', 'This product includes:', false) + '</p>'
    + '<ul>'
    + '<li>' + Uni.I18n.translate('about.static.part.2.1', 'SAM', 'Software developed under the license terms of the <a href="http://www.apache.org/" target="_blank">Apache Software Foundation</a>.', false) + '</li>'
    + '<li>' + Uni.I18n.translate('about.static.part.2.2', 'SAM', 'The following libraries which are covered by the GNU LGPL license', false)
    + '<ul>'
    + '<li>JavaBeans(TM) Activation Framework (<a href="http://java.sun.com/javase/technologies/desktop/javabeans/jaf/index.jsp" target="_blank">javax.activation:activation:1.1.1</a>)</li>'
    + '<li>Serial Device based on RxTx (<a href="https://github.com/joelbinn/rxtx-osgi" target="_blank">se.joel.osgi:rxtx-osgi:1.0</a>)</li>'
    + '<li>Yet Another Java Service Wrapper (<a href="http://yajsw.sourceforge.net/" target="_blank">YAJSW 11.11</a>)</li>'
    + '</ul>'
    + '</li>'
    + '<li>' + Uni.I18n.translate('about.static.part.2.3', 'SAM', 'Code written by other third parties.', false) + '</li>'
    + '</ul>'
    + '<p>' + Uni.I18n.translate('about.static.part.3', 'SAM', 'Additional details regarding these and other third party code included in this product, including applicable copyright, legal and licensing notices, are available in the &quot;licenses&quot; directory under the Connexo installation directory.', false) + '</p>'
});