package com.heerbann.packer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class EntryPoint {

	public static void main (String[] args) {
		if(args.length == 0 || args.length > 1) System.exit(1);
		
		//
		
		ArrayList<File> assets = findAllAssets(args[0]);
		
		ArrayList<Long> cache = new ArrayList<Long>();
		ArrayList<String> filesIndex = new ArrayList<String>();
		
		for(int i = 0; i < assets.size(); i++){
			//System.out.println(assets.get(i));
			convert(i, assets.get(i), cache, filesIndex);
		}
		
		write(args[0].concat("_packed.txt"), cache, filesIndex);
	}
	
	private static void write(String output, ArrayList<Long> cache, ArrayList<String> filesIndex){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(output);
			for(int i = 0 ; i < cache.size(); i++)
				writer.print(cache.get(i) + "" + System.lineSeparator());
			writer.close();
			
			writer = new PrintWriter(output.concat("_index.txt"));
			for(int i = 0 ; i < filesIndex.size(); i++)
				writer.print(filesIndex.get(i) + System.lineSeparator());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) writer.close();
		}
	}
	
	private static void convert(int index, File file, ArrayList<Long> cache, ArrayList<String> filesIndex){
		Scanner scanner = null;
		try {
			
			scanner = new Scanner(file);
			
			short x = 0, y = 0, width = 0, height = 0;
			boolean rotate = false;
			
			filesIndex.add(scanner.nextLine());
			
			for(int i = 1; i < 5; i++)
				scanner.nextLine();
			
			int k = 0;
			while(scanner.hasNext()){
				String line = scanner.nextLine();
				
				switch(k){
				case 1: //rotate
					rotate = line.contains("true");
					break;
				case 2: //xy
					String[] l1 = line.replaceAll("xy: ", "").replaceAll(" ", "").split(",");
					x = Short.parseShort(l1[0]);
					y = Short.parseShort(l1[1]);
					break;
				case 3: //size
					String[] l2 = line.replaceAll("size: ", "").replaceAll(" ", "").split(",");
					width = Short.parseShort(l2[0]);
					height = Short.parseShort(l2[1]);
					break;
				}			
				
				k++;
				if(k >= 7){
					k = 0;
					cache.add(pack(x, y, width, height, rotate, index));
				}			
			}
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally{
			if(scanner != null) scanner.close();
		}
	}
	
	public static final int maskByte = 0xFF, maskShort = 0xFFFF, maskInt = 0xFFFFFFFF;
	public static final int shiftByte = 4, shiftShort = 8, shiftInt = 16;
	
	//byte [0000 0000]
	//short [0000 0000] [0000 0000]
	//int [0000 0000] [0000 0000] [0000 0000] [0000 0000]
	//long [0000 0000] [0000 0000] [0000 0000] [0000 0000] [0000 0000] [0000 0000] [0000 0000] [0000 0000]
	
	//2048 : [0000 1000] [0000 0000]
	
	//packed: 1:[0000 000r] 2:[x] 3:[x|y] 4:[y] 5:[w] 6:[w|h] 7:[h] 8:[index]
	//[0000 000r] [xxxx xxxx] [xxxx yyyy] [yyyy yyyy] [wwww wwww] [wwww hhhh] [hhhh hhhh] [iiii iiii]
	
	//0000_0001_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000;
	//0000_0000_1111_1111_1111_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000;
	//0000_0000_0000_0000_0000_1111_1111_1111_0000_0000_0000_0000_0000_0000_0000_0000;
	//0000_0000_0000_0000_0000_0000_0000_0000_1111_1111_1111_0000_0000_0000_0000_0000;
	//0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_1111_1111_1111_0000_0000;
	//0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_1111_1111;
	//0000_0001_0000_0000_0001_0000_0000_0001_0000_0001_1110_0000_0001_0000_0000_0000
	
	//0000_0001_0000_0000_0001_0000_0000_0001_0000_0001_1110_0000_0001_0000_0000_0000
	//0001_1110_0000_0001_0000_0000_0000
	
	//72075190550401024
	
	private static long pack(short x, short y, short width, short height, boolean rotate, int index){
		return ((((((((((long)(((rotate ? 0x1 : 0x0) & 0x1) << 12) | (x & 0xFFF)) << 12) | (y & 0xFFF)) << 12) | (width & 0xFFF)) << 12) | (height & 0xFFF)) << 8) | index & 0xFF);     
	}
	
	/** finds all files with .atlas in a given folder and its subfolders */
	private static ArrayList<File> findAllAssets(String pathToParent){
		ArrayList<File> assets = new ArrayList<File>();
		LinkedList<File> toLoad = new LinkedList<File>();
		
		File[] files = new File(pathToParent).listFiles();
		for(int i = 0; i < files.length; i++)
			toLoad.addLast(files[i]);
		
		while(!toLoad.isEmpty()){		
			File f = toLoad.removeFirst();
			if(f.isDirectory()){
				File[] newFiles = f.listFiles();
				for(int i = 0; i < newFiles.length; i++){
					toLoad.addLast(newFiles[i]);
				}
			}else if(f.getName().contains(".atlas")) assets.add(f);
		}
		return assets;
	}

}
