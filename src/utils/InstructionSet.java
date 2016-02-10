package utils;

import java.util.ArrayList;

public class InstructionSet {
	private ArrayList<Square> squares;
	private ArrayList<Line> lines;
	
	public InstructionSet() {
		super();
		this.squares  = new ArrayList<Square>();
		this.lines = new ArrayList<Line>();
	}
	public ArrayList<Square> getSquares() {
		return squares;
	}
	public void setSquares(ArrayList<Square> squares) {
		this.squares = squares;
	}
	public ArrayList<Line> getLines() {
		return lines;
	}
	public void setLines(ArrayList<Line> lines) {
		this.lines = lines;
	}
	
	
}
