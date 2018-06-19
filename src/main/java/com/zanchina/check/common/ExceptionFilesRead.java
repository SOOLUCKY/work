package com.zanchina.check.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ExceptionFilesRead {
    public static String readFiles(String statusCode, String[] names) throws Exception{
        String s="";
        String lineTxt = null;
        StringBuffer str = new StringBuffer();
        StringBuffer[] buffer = new StringBuffer[names.length];
        FileReader[] fr = new FileReader[names.length];
        BufferedReader[] br = new BufferedReader[names.length];
        try {
            for(int i=0; i<names.length; i++){
                fr[i] = new FileReader(names[i]);
                br[i] = new BufferedReader(fr[i]);
            }
            for(int i=0; i<names.length; i++){
                while ((lineTxt = br[i].readLine()) != null) {
                    buffer[i] = new StringBuffer(lineTxt+"<br />");
                }
            }
            for(int i=0; i<names.length; i++){
                str.append(buffer[i].toString());
            }
            s= "状态码"+statusCode+"的url:<br />"+str.toString()+"<br />";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
       return s;
    }
}