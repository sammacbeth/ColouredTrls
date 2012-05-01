Ext.define('Presage2.view.ColouredTrlsVisualiser', {
	extend: 'Presage2.view.2DVisualiser',
	alias: 'widget.colouredtrlsvisualiser',
	createSprites: function(timeline) {
		console.log(timeline);
		var tileRe = /^tile-(\d+)-(\d+)$/;
		for(var prop in timeline.raw) {
			if(prop.match(tileRe)) {
				var matches = tileRe.exec(prop), 
					x = matches[1], 
					y = matches[2];
				var sp = this.surface.add({
					type: 'rect',
					width: this.scale,
					height: this.scale,
					opacity: 0.8,
					fill: timeline.raw[prop],
					x: 10 + (x * this.scale),
					y: 10 + (y * this.scale)
				});
				sp.show(true);
				this.sprites[x +"-"+ y] = sp;
			}
		}

		this.nameCtr = "a".charCodeAt(0);
		this.agentNames = {};
		timeline.agents().each(function(ag) {
			if(ag.data.data.x != undefined && ag.data.data.y != undefined) {
				var name = String.fromCharCode(this.nameCtr++),
					sp = this.surface.add({
					type: 'text',
					text: name,
					font: "bold 16pt sans",
					fill: 'BLACK',
					x: 10 + ((0.5+Number(ag.data.data.x)) * this.scale),
					y: 10 + ((0.5+Number(ag.data.data.y)) * this.scale)
				});
				this.agentNames[name] = ag.getId();
				sp.show(true);
				this.sprites[ag.getId()] = sp
			}
		}, this);
	},
	setTimeStep: function(time) {
		var step = this.timeline.getById(time);
		if(step != null) {
			step.agents().each(function(ag) {
				if(ag.getId() in this.sprites) {
					var sp = this.sprites[ag.getId()],
						x = Number(ag.data.data.x),
						y = Number(ag.data.data.y);
					if(!isNaN(x) && !isNaN(y)) {
						sp.setAttributes({
							x: 10 + ((0.5+x) * this.scale),
							y: 10 + ((0.5+y) * this.scale)
						}, true)
					}
				}
			}, this);
		}
	}
});
