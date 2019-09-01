package com.htetznaing.unitoolkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Test {
    public static void main(String pa[]){

        String data = readTextFile("/Users/htetznaing/Downloads/link.ibd");
        for (String a:data.split("\n")){
            System.out.println(a+"\n\n\n");
        }
    }

    private static String readTextFile(String path) {
        File file = new File(path);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
        }

        return text.toString();
    }
}
