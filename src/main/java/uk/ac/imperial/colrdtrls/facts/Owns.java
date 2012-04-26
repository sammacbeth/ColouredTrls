package uk.ac.imperial.colrdtrls.facts;

public class Owns {

	Player player;
	Colour colour;
	int count;

	public Owns(Player p, Colour c, int count) {
		super();
		this.player = p;
		this.colour = c;
		this.count = count;
	}

	public Player getPlayer() {
		return player;
	}

	public Colour getColour() {
		return colour;
	}

	public int getCount() {
		return count;
	}

	public void increment() {
		count++;
	}

	public void decrement() {
		count--;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colour == null) ? 0 : colour.hashCode());
		result = prime * result + ((player == null) ? 0 : player.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Owns other = (Owns) obj;
		if (colour != other.colour) {
			return false;
		}
		if (player == null) {
			if (other.player != null) {
				return false;
			}
		} else if (!player.equals(other.player)) {
			return false;
		}
		return true;
	}

}
