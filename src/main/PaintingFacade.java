package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import utils.Hole;
import utils.InstructionSet;
import utils.Line;
import utils.Square;

public class PaintingFacade {
	private static final char PIXEL_ON = '#';
	private static final char PIXEL_OFF = '.';
	private int row,col;
	private Logger log;
	private File input;
	private String OUTPUT_FILE_NAME = "list.txt";
	private char image[][], imageTemp[][];
	private InstructionSet instructions;
	
	public PaintingFacade(File input){
		log = Logger.getLogger("global");
		this.instructions = new InstructionSet();
		this.input = input;
		getImage();
	}
	
	private void getImage() {
		try {
			Scanner in = new Scanner(input);
			String imSize = in.nextLine();
			this.row = Integer.parseInt(imSize.split(" ")[0]);
			this.col = Integer.parseInt(imSize.split(" ")[1]);
			this.image = new char[row][col];
			this.imageTemp = new char[row][col];
			String line = "";
			int actualRow = 0;
			while (in.hasNextLine()){
				line = in.nextLine();
				for (int actualCol=0; actualCol<line.length(); actualCol++){
					if (line.charAt(actualCol)==PIXEL_ON){
						this.image[actualRow][actualCol] = PIXEL_ON;
					}
					else{
						this.image[actualRow][actualCol] = PIXEL_OFF;
					}
				}
				actualRow = actualRow+1;
			}
			System.out.println(printImage(image));
		} catch (FileNotFoundException e) {
			log.severe("File not found: " + e.getMessage());			
			e.printStackTrace();
		}
	}
	
	public String printImage(char[][] toPrint){
		String toReturn = "";
		for (int rowTemp=0; rowTemp<toPrint.length; rowTemp++){
			for (int colTemp=0; colTemp<toPrint[0].length; colTemp++){
				toReturn = toReturn+ toPrint[rowTemp][colTemp];
			}
			toReturn+="\n";
		}
		return toReturn;
	}

	public void searchPattern(){
		log.info("Searching squares...");
		for (int i=0; i<this.row; i++){
			System.arraycopy(image[i], 0, imageTemp[i], 0, image[i].length);
		}
		int maxSquareSize = (Math.min(row, col)%2==0) ? Math.min(row, col)-1 : Math.min(row, col);
		int currentSize = maxSquareSize;	
		int CURRENT_IMAGE = 0;
		saveCurrentImage(CURRENT_IMAGE);
		//starting with research of squares
		//sliding window
		while(currentSize>=3){
			for (int i=0; i<row-currentSize; i++){
				for (int j=0; j<col-currentSize; j++){
					Square tmp = containsGoodSquare(i,j,currentSize);
					if (tmp!=null){
						this.instructions.getSquares().add(tmp);
						saveCurrentImage(CURRENT_IMAGE);
						CURRENT_IMAGE++;
					}
				}
			}
			currentSize-=2;
		}
		//starting with research of lines
		//sliding window
		for (int i=0; i<row; i++){
			for (int j=0; j<col; j++){
				if(imageTemp[i][j]==PIXEL_ON){
					Line tmp = searchLine(i,j);
					instructions.getLines().add(tmp);
					saveCurrentImage(CURRENT_IMAGE);
					CURRENT_IMAGE++;
				}
			}
		}
		
		//outputFile creation
		String instructionsList = "";
		int instructionNum = 0;
		//squares
		for (int i=0; i<instructions.getSquares().size(); i++){
			instructionNum++;
			Square tmp = instructions.getSquares().get(i);
			int r = tmp.getRow() + ((tmp.getSize()-1)/2);
			int c = tmp.getCol() + ((tmp.getSize()-1)/2);
			instructionsList += "PAINT_SQUARE " + r + " " + c + " " + ((tmp.getSize()-1)/2) + "\n";
			for (Hole h : tmp.getHoles()){
				if (image[h.getX()][h.getY()]==PIXEL_OFF){
					instructionNum++;
					instructionsList += "ERASE_CELL " + h.getX() + " " + h.getY() + "\n";
				}
			}
		}
		//lines
		for (int i=0; i<instructions.getLines().size(); i++){		
			instructionNum++;
			Line tmp = instructions.getLines().get(i);
			instructionsList += "PAINT_LINE " + tmp.getX1() + " " + tmp.getY1() + " " + tmp.getX2() + " " + tmp.getY2() + "\n";
		}
		PrintWriter pw;
		try {
			pw = new PrintWriter(new File(OUTPUT_FILE_NAME));
			pw.write(instructionNum + "\n");
			pw.write(instructionsList);
			pw.close();
		} catch (FileNotFoundException e) {
			log.severe("FileNotFound: " + e.getMessage());
			e.printStackTrace();
		}

		System.out.println("MAX SCORE = " + (this.col * this.row));
		System.out.println("SCORE = " + (this.col * this.row) + "-" + instructionNum + " = " + (this.col * this.row - instructionNum));
		
	}
	
	private Line searchLine(int i, int j) {
		//vertical search
		int verticalSize = 1, horizontalSize = 1;
		for (int h=i; h<imageTemp.length; h++){
			if (imageTemp[h][j]==PIXEL_ON){
				verticalSize += 1;
			}
			else
				break;
		}
		for (int w=j; w<imageTemp[0].length; w++){
			if (imageTemp[i][w]==PIXEL_ON){
				horizontalSize += 1;
			}
			else
				break;
		}
		if (verticalSize > horizontalSize){
			Line toReturn = new Line(i, j, i+verticalSize-2, j);
			for (int h=i; h<i+verticalSize-1; h++){
				imageTemp[h][j] = PIXEL_OFF;
			}
			log.info("found line: (" + i + "," + j + ")(" + (i+verticalSize-2) + "," + j + ")");
			return toReturn;
		}
		else{
			Line toReturn = new Line(i, j, i, j+horizontalSize-2);
			for (int w=j; w<j+horizontalSize-1; w++){
				imageTemp[i][w] = PIXEL_OFF;
			}
			log.info("found line: (" + i + "," + j + ")(" + i + "," + (j+horizontalSize-2) + ")");
			return toReturn;
		}
	}

	private void saveCurrentImage(int cURRENT_IMAGE) {
		/*PrintWriter writer;
		try {
			writer = new PrintWriter("Res_tmp_" + cURRENT_IMAGE + ".txt", "UTF-8");
			writer.println(printImage(imageTemp));
			writer.close();
		}catch (FileNotFoundException e) {
			log.severe("FileNotFound: " + e.getMessage());
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			log.severe("UnsupportedEncodingException: " + e.getMessage());
			e.printStackTrace();
		}*/

	}

	private Square containsGoodSquare(int i, int j, int currentSize) {
		int numberOfPixelOff = 0;
		Square toReturn = new Square(i, j, currentSize);
		for (int rowTmp=i; rowTmp<i+currentSize; rowTmp++){
			for (int colTmp=j; colTmp<j+currentSize; colTmp++){
				if(this.imageTemp[rowTmp][colTmp] == PIXEL_OFF){
					numberOfPixelOff = numberOfPixelOff+1;
					toReturn.getHoles().add(new Hole(rowTmp, colTmp));
				}				
			}
		}
		if (numberOfPixelOff >= currentSize-1){
			//log.info("Sauare not found: " + i + ";" + j + " - size=" + currentSize);
			return null;
		}
		else{
			log.info("found Square: pos=" + i + ";" + j + " - size=" + currentSize);
			//image update...
			for (int row=i; row<i+currentSize; row++){
				for (int col=j; col<j+currentSize; col++){
					if(this.imageTemp[row][col] == PIXEL_ON){
						this.imageTemp[row][col] = PIXEL_OFF;
					}
				}
			}
			return toReturn;
		}
	}

	public static void main(String[] args) {
		File f = new File(args[0]);
		PaintingFacade pf = new PaintingFacade(f);
		pf.searchPattern();
	}
	
	public static void test1(){
		String line = "...##.......#####...";
		System.out.println(line.indexOf("#"));
	}
}
