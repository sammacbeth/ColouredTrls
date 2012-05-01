Ext.define('Presage2.view.ColouredTrlsVisualiser', {
	extend: 'Presage2.view.2DVisualiser',
	alias: 'widget.colouredtrlsvisualiser',
	createSprites: function(timeline) {
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
		this.goals = {};
		timeline.agents().each(function(ag) {
			if(ag.data.data.x != undefined && ag.data.data.y != undefined) {
				var name = String.fromCharCode(this.nameCtr++),
					sp = this.surface.add({
					type: 'text',
					text: name,
					font: "bold 16pt sans",
					fill: 'BLACK',
					x: 10 + ((0.5+Number(ag.data.data.x)) * this.scale),
					y: 10 + ((0.5+Number(ag.data.data.y)) * this.scale),
					zIndex: 10
				});
				this.agentNames[ag.getId()] = name;
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
				if('goal-x' in ag.data.data && 'goal-y' in ag.data.data) {
					var x = Number(ag.data.data["goal-x"]),
						y = Number(ag.data.data["goal-y"]);
					if(ag.getId() in this.goals) {
						var goal = this.goals[ag.getId()];
						if(goal.x != x || goal.y != y) {
							goal.x = x;
							goal.y = y;
							goal.sp.setAttributes({
								x: 10 + ((0.2+x) * this.scale),
								y: 10 + ((0.2+y) * this.scale)
							}, true);
							goal.text.setAttributes({
								x: 10 + ((0.35+x) * this.scale),
								y: 10 + ((0.5+y) * this.scale)
							}, true);
						}
					} else {
						this.insertGoal(ag);
					}
				}
			}, this);
		}
	},
	insertGoal: function(ag) {
		var x = Number(ag.data.data["goal-x"]),
			y = Number(ag.data.data["goal-y"]);
		this.goals[ag.getId()] = {
			x: x,
			y: y,
			sp: this.surface.add({
				type: 'rect',
				x: 10 + ((0.2+x) * this.scale),
				y: 10 + ((0.2+y) * this.scale),
				height: 0.5*this.scale,
				width: 0.5*this.scale,
				fill: 'WHITE',
				opacity: 0.5
			}),
			text: this.surface.add({
				type: 'text',
				x: 10 + ((0.35+x) * this.scale),
				y: 10 + ((0.5+y) * this.scale),
				font: "14pt sans",
				fill: 'BLACK',
				text: this.agentNames[ag.getId()],
				opacity: 0.5
			})
		}
		this.goals[ag.getId()].sp.setAttributes({
			rotate: {
				degrees: 45
			}
		});
		this.goals[ag.getId()].sp.show(true);
		this.goals[ag.getId()].text.show(true);
	}
});
