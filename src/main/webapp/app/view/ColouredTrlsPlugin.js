Ext.require([
	'Presage2.view.ColouredTrlsVisualiser'
]);

Ext.define('Presage2.view.ColouredTrlsPlugin', {
	extend: 'Presage2.view.VisualiserPlugin',
	alias: 'widget.colouredtrlsplugin',
	initComponent: function() {
		Ext.apply(this, {
			drawPanel: 'Presage2.view.ColouredTrlsVisualiser'
		});
		this.callParent(arguments);
	}
});
