
package com.heerbann.packer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class EntryPoint {

	public static void main (String[] args) {
		if (args.length == 0 || args.length > 1) System.exit(1);
		ArrayList<File> assets = findAllAssets(args[0]);
		ArrayList<String> cache = new ArrayList<String>();
		ArrayList<String> filesIndex = new ArrayList<String>();
		for (int i = 0; i < assets.size(); i++) {
			// System.out.println(assets.get(i));
			//FIXME multiple files will not work
			convert(assets.get(i), cache);
		}
		
		 HashMap<String, Long> map = new HashMap<String, Long>(60000);
		
		for(int i = 0; i < cache.size(); i++){
			String line = cache.get(i);
			if(line.contains(";")) continue;
			String[] t = line.split(",");
			map.put(t[0], Long.parseLong(t[1]));
		}
		
		serializeHashmap(args[0], map);
		write(args[0].concat("/packed.osh"), cache, filesIndex);
	}
	
	private static void serializeHashmap(String path, HashMap<String, Long> map){
		File f = new File(path.concat("/hashMap.ser"));
		try {
			FileOutputStream outFile = new FileOutputStream(f);
			ObjectOutputStream out = new ObjectOutputStream(outFile);
			out.writeObject(map);
         out.close();
         outFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void write (String output, ArrayList<String> cache, ArrayList<String> filesIndex) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(output);
			for (int i = 0; i < filesIndex.size(); i++)
				writer.print(filesIndex.get(i) + System.lineSeparator());
			
			for (int i = 0; i < cache.size(); i++)
				if (i < cache.size() - 1)
					writer.print(cache.get(i) + System.lineSeparator());
				else
					writer.print(cache.get(i));
			writer.close();		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) writer.close();
		}
	}
	
	private static void convert (File file, ArrayList<String> cache) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
			byte tiles = 0;
			short x = 0, y = 0, width = 0, height = 0, offSetX = 0, offsetY = 0, index = -1;
			boolean rotate = false;
			String name = null;
			boolean newFile = false;
			int i = 0, k = 0;
			while(true){
				if(!scanner.hasNextLine()) break;
				cache.add(k++, scanner.nextLine() + ";");
				cache.add(k++, scanner.nextLine().replaceAll("size: ", "").replaceAll(" ", "") + ";");
				for (int j = 1; j < 4; j++)				
					scanner.nextLine();
			 
				while (true) {
					if(!scanner.hasNextLine()) break;
					String fileNameS = scanner.nextLine();
					if(fileNameS.trim().matches("")){
						newFile = true;
						break;
					}
					String rotateS = scanner.nextLine();
					String xyS = scanner.nextLine();
					String sizeS = scanner.nextLine();
					String origS = scanner.nextLine();
					String offsetS = scanner.nextLine();
					String indexS = scanner.nextLine();
	
					// rotate
					//rotate = line.contains("true");
					// xy
					String[] l1 = xyS.replaceAll("xy: ", "").replaceAll(" ", "").split(",");
					x = Short.parseShort(l1[0]);
					y = Short.parseShort(l1[1]);
					// size
					String[] l2 = sizeS.replaceAll("size: ", "").replaceAll(" ", "").split(",");
					width = Short.parseShort(l2[0]);
					height = Short.parseShort(l2[1]);
					//offset
					String[] l3 = offsetS.replaceAll("offset: ", "").replaceAll(" ", "").split(",");
					offSetX = Short.parseShort(l3[0]);
					offsetY = Short.parseShort(l3[1]);
					//index
					index = Short.parseShort(indexS.replaceAll("index: ", "").trim());
					
					tiles = (byte)((width - width%30)/30);
	
					cache.add(fileNameS + (index != -1 ? "." + index : "") + "," + pack((short)(x + offSetX), (short)(y + offsetY), width, height, tiles, i));
	
				}
				i++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (scanner != null) scanner.close();
		}
	}

	// 0000 0001 1000 0110 0001 0001 0110 1001 0000 0001 1110 0000 0001 0000 0000 0000
	// 0000 0001 1000 0110 0001 0000 1010 0001 0000 0001 1110 0000 0001 0000 0000 0000
	// [tttt tttt] [xxxx xxxx] [xxxx yyyy] [yyyy yyyy] [wwww wwww] [wwww hhhh] [hhhh hhhh] [iiii iiii]
	private static long pack (short x, short y, short width, short height, byte tiles, int index) {
		return ((((((((((long)((0l | tiles) <<12) | (x & 0xFFF)) << 12) | (y & 0xFFF)) << 12)
			| (width & 0xFFF)) << 12) | (height & 0xFFF)) << 8) | index & 0xFF);
	}

	/** finds all files with .atlas in a given folder and its subfolders */
	private static ArrayList<File> findAllAssets (String pathToParent) {
		ArrayList<File> assets = new ArrayList<File>();
		LinkedList<File> toLoad = new LinkedList<File>();
		File[] files = new File(pathToParent).listFiles();
		for (int i = 0; i < files.length; i++)
			toLoad.addLast(files[i]);
		while (!toLoad.isEmpty()) {
			File f = toLoad.removeFirst();
			if (f.isDirectory()) {
				File[] newFiles = f.listFiles();
				for (int i = 0; i < newFiles.length; i++)
					toLoad.addLast(newFiles[i]);
			} else if (f.getName().contains(".osh")) assets.add(f);
		}
		return assets;
	}

}
