/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conchat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author xxx
 */
public class CommonTools
{
    static int tableH = 0x2500;
    static int tableV = 0x2502;
    static int tableC = 0x253C;

    static int tableT = 0x252C;
    static int tableB = 0x2534;
    static int tableL = 0x251C;
    static int tableR = 0x2524;

    static int table1 = 0x250C;
    static int table2 = 0x2510;
    static int table3 = 0x2514;
    static int table4 = 0x2518;
    
    /**
     * Block horizontal scroll indicator on the left edge
     */
    static int scrollL = 0x25C4;
    
    /**
     * Block horizontal scroll indicator on the right edge
     */
    static int scrollR = 0x25BA;
    
    /**
     * Background outside the chat text
     */
    static int background = 0x2592;
    
    /**
     * Splitter between chat messages
     */
    static int splitterMsg = 0x2550;

    /**
     * Splitter within message
     */
    static int splitterText = 0x2500;
    
    static int splitterInfo = ' ';
    static String splitterInfoS = " ";
    
    static int modelNameBlankChar = '.';
    static String modelNameBlankCharS = ".";
    
    static String applDir = "";
    static String logFileName = "http.txt";
    static String configFileName = "config.txt";
    static String counterFileName = "counter.txt";
    static String modelsFileName = "models.txt";
    static String contextFileNamePrefix = "context";
    static String contextFileNameSuffix = ".txt";
    
    static String modelNameBlankCharRemove(String cmd)
    {
        while (cmd.startsWith(CommonTools.modelNameBlankCharS) && (cmd.length() > 1))
        {
            cmd = cmd.substring(1);
        }
        while (cmd.endsWith(CommonTools.modelNameBlankCharS) && (cmd.length() > 1))
        {
            cmd = cmd.substring(0, cmd.length() - 1);
        }
        cmd = cmd.trim();
        for (int i = 0; i < cmd.length(); i++)
        {
            if (cmd.charAt(i) == splitterInfo)
            {
                cmd = stringSetChar(cmd, i, '_');
            }
            if (cmd.charAt(i) == ',')
            {
                cmd = stringSetChar(cmd, i, '_');
            }
        }
        return cmd;
    }
    
    static void debug(String str)
    {
        CommonTools.fileSaveText(CommonTools.applDir + CommonTools.logFileName, str);
    }

    static void debugln(String str)
    {
        CommonTools.fileSaveText(CommonTools.applDir + CommonTools.logFileName, str + "\n");
    }
    
    /**
     * Replace spaces into exclamation marks for test and debug purposes
     * @param str
     * @return 
     */
    static String debugSpace(String str)
    {
        for (int i = 0; i < str.length(); i++)
        {
            if (str.charAt(i) == ' ')
            {
                str = stringSetChar(str, i, '!');
            }
        }
        return str;
    }
    
    static int strToInt(String str, int def)
    {
        try
        {
            return Integer.parseInt(str);
        }
        catch (Exception e)
        {
            return def;
        }
    }

    static String intToDec(long val, int crop, int dec)
    {
        if (val < 0)
        {
            String S_ = "";
            while (S_.length() < dec)
            {
                S_ = "-" + S_;
            }
            return "-." + S_;
        }
        while (crop > 1)
        {
            val = val / 10L;
            crop--;
        }
        if (crop == 1)
        {
            if ((val % 10L) >= 5)
            {
                val += 10L;
            }
            val = val / 10L;
            crop--;
        }
        String S = val + "";
        while (S.length() <= dec)
        {
            S = "0" + S;
        }
        return S.substring(0, S.length() - dec) + "." + S.substring(S.length() - dec);
    }
    
    static String intToStr(int val, int pad)
    {
        String valStr = "" + val;
        return stringIndent(pad - valStr.length(), ' ') + valStr;
    }

    static String intToStr(String valStr, int pad)
    {
        return stringIndent(pad - valStr.length(), ' ') + valStr;
    }
    
    static String intLimited(int val)
    {
        if (val > 0)
        {
            return "" + val;
        }
        else
        {
            return "unlimited";
        }
    }
    
    static String intIsSpecified(int val, int valMin, int valMax)
    {
        if (isWithinRange(val, valMin, valMax))
        {
            return "" + val;
        }
        else
        {
            return "unspecified";
        }
    }

    static boolean isWithinRange(int val, int valMin, int valMax)
    {
        if (val < valMin) return false;
        if (val > valMax) return false;
        return true;
    }
    
    /**
     * String find and replace several times, until the phrase to search is not contained
     * @param str String to edit
     * @param phraseFrom Phrase to find
     * @param phraseTo Replacement phrase
     * @return Edited string
     */
    static public String stringReplaceMultiply(String str, String phraseFrom, String phraseTo)
    {
        String str0 = str.replace(phraseFrom, phraseTo);
        while (!str0.equals(str))
        {
            str = str0;
            str0 = str.replace(phraseFrom, phraseTo);
        }
        return str0;
    }
    
    /**
     * Change one character in string
     * @param str String to modify
     * @param idx Character index
     * @param chr New character
     * @return String with changed character
     */
    static public String stringSetChar(String str, int idx, char chr)
    {
        if (idx == 0)
        {
            return chr + str.substring(idx + 1);
        }
        return str.substring(0, idx) + chr + str.substring(idx + 1);
    }
    
    /**
     * Remove substring from string
     * @param textLine String to modify
     * @param idx Substring index
     * @param n Substring length
     * @return Modified string
     */
    static public String stringRemove(String textLine, int idx, int n)
    {
        if (idx == 0)
        {
            if (textLine.length() > n)
            {
                return textLine.substring(n);
            }
            else
            {
                return "";
            }
        }
        else
        {
            if (textLine.length() > (idx + n))
            {
                return textLine.substring(0, idx) + textLine.substring(idx + n);
            }
            else
            {
                return textLine.substring(0, idx);
            }
        }
    }

    /**
     * Create the string containing number of specified character
     * @param n String length
     * @param chr Character
     * @return String consisting of n characters
     */
    static public String stringIndent(int n, char chr)
    {
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < n; i++)
        {
            spaces.append(chr);
        }
        return spaces.toString();
    }

    /**
     * Format JSON text
     * @param json Raw JSON text
     * @return Formatted JSON text
     */
    static String jsonFormat(String json)
    {
        try
        {
            return (new JSONObject(json)).toString(4);
        }
        catch (JSONException e)
        {
            return json;
        }
    }

    static public int fileGetType(String fileName)
    {
        File F = new File(fileName);
        if (F.isFile()) { return 1; }
        if (F.isDirectory()) { return 2; }
        return 0;
    }
    
    static public int fileGetSize(String fileName)
    {
        File F = new File(fileName);
        return (int)F.length();
    }
    
    static public void fileClear(String fileName)
    {
        try
        {
            File F = new File(fileName);
            F.delete();
        }
        catch (Exception __)
        {

        }
    }
    
    /**
     * Append text to specified file
     * @param fileName File name
     * @param textData Text to append
     */
    static public void fileSaveText(String fileName, String textData)
    {
        try
        {
            try (FileWriter F_ = new FileWriter(fileName, true); BufferedWriter F = new BufferedWriter(F_))
            {
                F.write(textData);
            }
        }
        catch (IOException __)
        {

        }
    }
    
    static public ArrayList<String> fileLoadText(String fileName, boolean includeEmpty)
    {
        ArrayList<String> temp = new ArrayList<>();
        try
        {
            try (FileReader F_ = new FileReader(fileName); BufferedReader F = new BufferedReader(F_))
            {
                String S = F.readLine();
                while (S != null)
                {
                    if (includeEmpty || (!S.trim().isBlank()))
                    {
                        temp.add(S);
                    }
                    S = F.readLine();
                }
            }
        }
        catch (IOException __)
        {

        }
        return temp;
    }
    
    public static void fileCopy(String srcFileName, String dstFileName)
    {
        try
        {
            Files.copy((Path)Paths.get(srcFileName), (Path)Paths.get(dstFileName), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException __)
        {

        }
    }
    
    public static ArrayList<String> fileList(String dir)
    {
        ArrayList<String> temp = new ArrayList<>();
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();
        if(listOfFiles != null)
        {
            for (int i = 0; i < listOfFiles.length; i++)
            {
                if (listOfFiles[i].isFile())
                {
                    temp.add(listOfFiles[i].getName());
                }
            }
        }
        Collections.sort(temp);
        return temp;
    }
    
    /**
     * Time stamp consisting of date and time in digits
     * @return Human readable time stamp
     */
    public static String timeStamp()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dt = sdf.format(new Date());
        return dt;
    }

    /**
     * Append the directory separator when the path does not containing the separator at the end
     * @param D Path to check and correct
     * @return Corrected path
     */
    public static String correctDir(String D)
    {
        if ((D.charAt(D.length() - 1) != '/') && (D.charAt(D.length() - 1) != '\\'))
        {
            int T1 = 0;
            int T2 = 0;
            for (int I = 0; I < D.length(); I++)
            {
                if (D.charAt(I) == '/')
                {
                    T1++;
                }
                if (D.charAt(I) == '\\')
                {
                    T2++;
                }
            }
            if (T2 > T1)
            {
                return D + "\\";
            }
            else
            {
                return D + "/";
            }
        }
        else
        {
            return D;
        }
    }
    
    /**
     * Get application directory
     * @param t Get method from 0 to 5
     * @return Application directory
     */
    public static String getApplDir(int t)
    {
        switch (t)
        {
            case 0:
                return System.getProperty("user.dir");
            case 1:
                try
                {
                    return new File(".").getCanonicalPath();
                }
                catch (IOException ex)
                {
                    return ex.getMessage();
                }

            case 2:
                return FileSystems.getDefault().getPath("").toAbsolutePath().toString();
            case 3:
                return Paths.get("").toAbsolutePath().toString();
            case 4:
                return CommonTools.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            case 5:
                try
                {
                    return new File(CommonTools.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
                }
                catch (URISyntaxException exception)
                {
                    //exception.printStackTrace();
                    return exception.toString();
                }
            default:
                for (int i = 0; i < 6; i++)
                {
                    System.out.println(i + "{" + CommonTools.getApplDir(i) + "}");
                }
                return "";
        }
    }

    public static boolean strOnlyDigits(String str)
    {
        for (int i = 0; i < str.length(); i++)
        {
            if (!isChar(str.charAt(i), false, false, true, false))
            {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isChar(int chr, boolean chrSpace, boolean chrAlpha, boolean chrNum, boolean chrSpecial)
    {
        if (chrSpace)
        {
            switch (chr)
            {
                case 0x20:
                case 0x09:
                    return true;
            }
        }
        if (chrAlpha)
        {
            if ((chr >= 0x41) && (chr <= 0x5A)) return true;
            if ((chr >= 0x61) && (chr <= 0x7A)) return true;
            if (chr >= 128)
            {
                if (Character.isLetterOrDigit(chr))
                {
                    return true;
                }
            }
        }
        if (chrNum)
        {
            if ((chr >= 0x30) && (chr <= 0x39)) return true;
        }
        if (chrSpecial)
        {
            if ((chr >= 0x21) && (chr <= 0x2F)) return true;
            if ((chr >= 0x3A) && (chr <= 0x40)) return true;
            if ((chr >= 0x5B) && (chr <= 0x60)) return true;
            if ((chr >= 0x7B) && (chr <= 0x7E)) return true;
            if (chr >= 128)
            {
                if (!Character.isLetterOrDigit(chr))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static String exceptionToStr(Exception e)
    {
        String msg = e.getMessage();
        for (int i = 0; i < e.getStackTrace().length; i++)
        {
            String stackItem = e.getStackTrace()[i].toString();
            if (!stackItem.startsWith("java.base"))
            {
                msg = msg + "|" + stackItem;
            }
        }
        return msg;
    }
}
