package utils;
import java.util.ArrayList;


public class Square {
	private int row,col,size;
	private ArrayList<Hole> holes;
	public Square(int row, int col, int size) {
		super();
		this.row = row;
		this.col = col;
		this.size = size;
		this.holes = new ArrayList<Hole>();
	}
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public int getCol() {
		return col;
	}
	public void setCol(int col) {
		this.col = col;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public ArrayList<Hole> getHoles() {
		return holes;
	}
	public void setHoles(ArrayList<Hole> holes) {
		this.holes = holes;
	}
	
	
}
