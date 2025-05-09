/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conchat;

import java.io.IOException;
import java.util.HashMap;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 *
 * @author xxx
 */
public class ConsoleInputOutput
{
    private final Terminal terminal;
    
    /**
     * The magic number to distinguish special keys (arrows, function etc.) from characters
     */
    public static final int keySpecialNum = 2000000;

    private String keySeq;
    
    private final int[] terminalReport;
    private static final int terminalReportSize = 10;

    public ConsoleInputOutput() throws IOException
    {
        terminal = TerminalBuilder.builder().jna(true).system(true).build();
        terminal.enterRawMode();
        terminalReport = new int[terminalReportSize];
    }
    

    public void printChar(int chr)
    {
        if (chr >= 0x10000)
        {
            terminal.writer().write(((chr - 0x10000) >> 10) + 0xD800);
            terminal.writer().write(((chr - 0x10000) & 0x3FF) + 0xDC00);
        }
        else
        {
            // High surrogate
            if ((chr >= 0xD800) && (chr < 0xDBFF))
            {
                return;
            }

            // Low surrogate
            if ((chr >= 0xDC00) && (chr < 0xDFFF))
            {
                return;
            }

            terminal.writer().write(chr);
        }
    }

    public void printString(StringUTF str)
    {
        printString(str.get());
    }

    public void printString(String str)
    {
        int l = str.length();
        for (int i = 0; i < l; i++)
        {
            terminal.writer().write(str.charAt(i));
        }
    }

    public void printFlush()
    {
        terminal.writer().flush();
    }

    private void printEscString(String str)
    {
        printChar((char)0x1B);
        printString(str);
        printFlush();
    }
    
    /**
     * Wait for key press
     * @return character code or key number
     */
    public int getKey()
    {
        printFlush();
        keySeq = "";
        try
        {
            int key = terminal.reader().read();
            if (key < 0)
            {
                return -1;
            }
            
            if (key == 27)
            {
                int terminalReportI = 0;
                terminalReport[0] = 0;
                boolean work = true;
                while (work)
                {
                    key = terminal.reader().read();
                    if (key < 0)
                    {
                        return -1;
                    }
                        
                    keySeq = keySeq + ((char)key);
                    if ((key >= 65) && (key <= 90) && (key != 'O')) work = false;
                    if ((key >= 97) && (key <= 122)) work = false;
                    if ((key >= 48) && (key <= 57))
                    {
                        if (terminalReportI < terminalReportSize)
                        {
                            terminalReport[terminalReportI] = terminalReport[terminalReportI] * 10 + (key - 48);
                        }
                    }
                    switch (key)
                    {
                        case ':':
                        case ';':
                            terminalReportI++;
                            terminalReport[terminalReportI] = 0;
                            break;
                        case '~':
                        case ']':
                        case '^':
                        case '_':
                            work = false;
                            break;
                        case '[':
                            if (keySeq.length() > 1) work = false;
                            break;
                        case 27:
                            keySeq = "[~]";
                            ringBell();
                            work = false;
                            break;
                    }
                }
                
                key = -1;

                // Escape
                if (keySeq.equals("[~]")) key = keySpecialNum;
                
                // Arrows
                if (keySeq.equals("[A")) key = keySpecialNum + 1;
                if (keySeq.equals("[B")) key = keySpecialNum + 2;
                if (keySeq.equals("[C")) key = keySpecialNum + 3;
                if (keySeq.equals("[D")) key = keySpecialNum + 4;
                if (keySeq.equals("OA")) key = keySpecialNum + 1;
                if (keySeq.equals("OB")) key = keySpecialNum + 2;
                if (keySeq.equals("OC")) key = keySpecialNum + 3;
                if (keySeq.equals("OD")) key = keySpecialNum + 4;
                
                // Navigation
                if (keySeq.equals("[2~")) key = keySpecialNum + 11;
                if (keySeq.equals("[3~")) key = keySpecialNum + 12;
                if (keySeq.equals("[1~")) key = keySpecialNum + 13;
                if (keySeq.equals("[4~")) key = keySpecialNum + 14;
                if (keySeq.equals("[H")) key = keySpecialNum + 13;
                if (keySeq.equals("[F")) key = keySpecialNum + 14;
                if (keySeq.equals("OH")) key = keySpecialNum + 13;
                if (keySeq.equals("OF")) key = keySpecialNum + 14;
                if (keySeq.equals("[5~")) key = keySpecialNum + 15;
                if (keySeq.equals("[6~")) key = keySpecialNum + 16;
                
                // Function
                if (keySeq.equals("OP")) key = keySpecialNum + 101;
                if (keySeq.equals("OQ")) key = keySpecialNum + 102;
                if (keySeq.equals("OR")) key = keySpecialNum + 103;
                if (keySeq.equals("OS")) key = keySpecialNum + 104;
                if (keySeq.equals("OT")) key = keySpecialNum + 105;
                if (keySeq.equals("OU")) key = keySpecialNum + 106;
                if (keySeq.equals("OV")) key = keySpecialNum + 107;
                if (keySeq.equals("OW")) key = keySpecialNum + 108;
                if (keySeq.equals("OX")) key = keySpecialNum + 109;
                if (keySeq.equals("OY")) key = keySpecialNum + 110;
                if (keySeq.equals("OZ")) key = keySpecialNum + 111;
                if (keySeq.equals("O[")) key = keySpecialNum + 112;
                if (keySeq.equals("O\\")) key = keySpecialNum + 113;
                if (keySeq.equals("O]")) key = keySpecialNum + 114;
                if (keySeq.equals("O^")) key = keySpecialNum + 115;
                if (keySeq.equals("O_")) key = keySpecialNum + 116;

                if (keySeq.equals("[11~")) key = keySpecialNum + 101;
                if (keySeq.equals("[12~")) key = keySpecialNum + 102;
                if (keySeq.equals("[13~")) key = keySpecialNum + 103;
                if (keySeq.equals("[14~")) key = keySpecialNum + 104;
                if (keySeq.equals("[15~")) key = keySpecialNum + 105;
                if (keySeq.equals("[17~")) key = keySpecialNum + 106;
                if (keySeq.equals("[18~")) key = keySpecialNum + 107;
                if (keySeq.equals("[19~")) key = keySpecialNum + 108;
                if (keySeq.equals("[20~")) key = keySpecialNum + 109;
                if (keySeq.equals("[21~")) key = keySpecialNum + 110;
                if (keySeq.equals("[23~")) key = keySpecialNum + 111;
                if (keySeq.equals("[24~")) key = keySpecialNum + 112;
                if (keySeq.equals("[25~")) key = keySpecialNum + 113;
                if (keySeq.equals("[26~")) key = keySpecialNum + 114;
                if (keySeq.equals("[28~")) key = keySpecialNum + 115;
                if (keySeq.equals("[29~")) key = keySpecialNum + 116;
                if (keySeq.equals("[31~")) key = keySpecialNum + 117;
                if (keySeq.equals("[32~")) key = keySpecialNum + 118;
                if (keySeq.equals("[33~")) key = keySpecialNum + 119;
                if (keySeq.equals("[34~")) key = keySpecialNum + 120;
            }
            
            return key;
        }
        catch (IOException e)
        {
            return -1;
        }
    }
    
    /**
     * Test getKey working
     * @return Key number
     */
    public int getKeyTest()
    {
        int keyNum = getKey();
        System.out.print(keyNum);
        if (keyNum >= keySpecialNum)
        {
            switch (keyNum)
            {
                case keySpecialNum + 0: System.out.print(" Esc"); break;

                case keySpecialNum + 1: System.out.print(" Up"); break;
                case keySpecialNum + 2: System.out.print(" Down"); break;
                case keySpecialNum + 3: System.out.print(" Right"); break;
                case keySpecialNum + 4: System.out.print(" Left"); break;

                case keySpecialNum + 11: System.out.print(" Insert"); break;
                case keySpecialNum + 12: System.out.print(" Delete"); break;
                case keySpecialNum + 13: System.out.print(" Home"); break;
                case keySpecialNum + 14: System.out.print(" End"); break;
                case keySpecialNum + 15: System.out.print(" PageUp"); break;
                case keySpecialNum + 16: System.out.print(" PageDown"); break;

                case keySpecialNum + 101: System.out.print(" F1"); break;
                case keySpecialNum + 102: System.out.print(" F2"); break;
                case keySpecialNum + 103: System.out.print(" F3"); break;
                case keySpecialNum + 104: System.out.print(" F4"); break;
                case keySpecialNum + 105: System.out.print(" F5"); break;
                case keySpecialNum + 106: System.out.print(" F6"); break;
                case keySpecialNum + 107: System.out.print(" F7"); break;
                case keySpecialNum + 108: System.out.print(" F8"); break;
                case keySpecialNum + 109: System.out.print(" F9"); break;
                case keySpecialNum + 110: System.out.print(" F10"); break;
                case keySpecialNum + 111: System.out.print(" F11"); break;
                case keySpecialNum + 112: System.out.print(" F12"); break;
            }
        }
        if ((keyNum >= 32) && (keyNum < keySpecialNum) && (keyNum != 127))
        {
            System.out.print(" ");
            System.out.print((char)keyNum);
        }
        System.out.println();
        return keyNum;
    }
    
    public int screenWidth;
    public int screenHeight;
    
    /**
     * Detect cursor position and save them into terminalReport array
     */
    private void getCursorPos()
    {
        printEscString("[6n");
        getKey();
    }
    
    /**
     * Measure screen size and save them into screenWidth and screenHeight fields
     */
    public void getScreenSize()
    {
        setScrollRegion(-1, -1);
        setCursorPos(0, 2);
        terminalReport[0] = 0;
        terminalReport[1] = 0;
        screenHeight = -1;
        screenWidth = -1;
        while (screenHeight < terminalReport[0])
        {
            screenHeight = terminalReport[0];
            printEscString("[10B");
            getCursorPos();
        }
        while (screenWidth < terminalReport[1])
        {
            screenWidth = terminalReport[1];
            printEscString("[20C");
            getCursorPos();
        }
        setScrollRegion(-1, -1);
    }
    
    public void ringBell()
    {
        printChar((char)7);
        printFlush();
    }
    
    public void setCursorPos(int x, int y)
    {
        printEscString("[" + (y + 1) + ";" + (x + 1) + "H");
    }

    /**
     * Reset all text attributes
     */
    public void setTextAttrReset()
    {
        printEscString("[0m");
    }

    public void screenClear()
    {
        setCursorPos(0, 0);
        setTextAttrReset();
        printEscString("[2J");
    }

    public void screenLineClear()
    {
        setTextAttrReset();
        printEscString("[0K");
    }
    
    public void setTextAttrBold1()
    {
        printEscString("[1m");
    }

    public void setTextAttrBold0()
    {
        printEscString("[22m");
    }
    
    public void setTextAttrItalic1()
    {
        printEscString("[3m");
    }

    public void setTextAttrItalic0()
    {
        printEscString("[23m");
    }

    public void setTextAttrUnderline1()
    {
        printEscString("[4m");
    }

    public void setTextAttrUnderline0()
    {
        printEscString("[24m");
    }

    public void setTextAttrReverse1()
    {
        printEscString("[7m");
    }

    public void setTextAttrReverse0()
    {
        printEscString("[27m");
    }

    public void setTextAttrStrike1()
    {
        printEscString("[9m");
    }

    public void setTextAttrStrike0()
    {
        printEscString("[29m");
    }
    
    /**
     * Test text attributes
     */
    void setTextAttrTest()
    {
        setTextAttrReset();
        System.out.print("normal");
        setTextAttrBold1();
        System.out.print("bold");
        setTextAttrBold0();
        System.out.print("normal");
        setTextAttrItalic1();
        System.out.print("italic");
        setTextAttrItalic0();
        System.out.print("normal");
        setTextAttrUnderline1();
        System.out.print("underline");
        setTextAttrUnderline0();
        System.out.print("normal");
        setTextAttrReverse1();
        System.out.print("reverse");
        setTextAttrReverse0();
        System.out.print("normal");
    }
    
    /**
     * Set the font size for specified line
     * @param n Line format from 0 to 3 as following: Normal, Double width, Double size upper half, Double size lower half
     */
    public void setLineFormat(int n)
    {
        switch (n)
        {
            case 0: printEscString("#5"); break;
            case 1: printEscString("#6"); break;
            case 2: printEscString("#3"); break;
            case 3: printEscString("#4"); break;
        }
    }
    
    private int scrollRegionFirst;
    private int scrollRegionLast;
    
    /**
     * Set scroll region
     * @param first First line
     * @param last Last line
     */
    public void setScrollRegion(int first, int last)
    {
        if ((first < 0) || (last < 0))
        {
            printEscString("[r");
            return;
        }
        scrollRegionFirst = first;
        scrollRegionLast = last;
        printEscString("[" + (first + 1) + ";" + (last + 1) + "r");
    }
    
    /***
     * Scroll one line down
     */
    public void scrollDn()
    {
        printString("\n");
        printFlush();
    }
    
    /**
     * Scroll one line up
     */
    public void scrollUp()
    {
        printEscString("M");
    }
    
    private final HashMap<Integer, Integer> charSizeMap = new HashMap<>();
    
    private boolean charSizeDuospace = true;
    
    /**
     * Reset character size data
     */
    public void charSizeReset(boolean charSizeDuospace_)
    {
        charSizeDuospace = charSizeDuospace_;
        charSizeMap.clear();
        charSizeMap.put((int)CommonTools.tableH, 1);
        charSizeMap.put((int)CommonTools.tableV, 1);
        charSizeMap.put((int)CommonTools.tableC, 1);
        charSizeMap.put((int)CommonTools.tableT, 1);
        charSizeMap.put((int)CommonTools.tableB, 1);
        charSizeMap.put((int)CommonTools.tableL, 1);
        charSizeMap.put((int)CommonTools.tableR, 1);
        charSizeMap.put((int)CommonTools.table1, 1);
        charSizeMap.put((int)CommonTools.table2, 1);
        charSizeMap.put((int)CommonTools.table3, 1);
        charSizeMap.put((int)CommonTools.table4, 1);
        charSizeMap.put((int)CommonTools.scrollL, 1);
        charSizeMap.put((int)CommonTools.scrollR, 1);
        charSizeMap.put((int)CommonTools.background, 1);
        charSizeMap.put((int)CommonTools.splitterMsg, 1);
        charSizeMap.put((int)CommonTools.splitterText, 1);
        charSizeMap.put((int)CommonTools.splitterInfo, 1);
    }


    /**
     * Get character size for proper text wrap
     * @param chr Character number
     * @return Character size in text cells
     */
    public int charSize(int chr)
    {
        // High surrogate
        if ((chr >= 0xD800) && (chr < 0xDBFF))
        {
            throw new RuntimeException("High surrogate character");
        }

        // Low surrogate
        if ((chr >= 0xDC00) && (chr < 0xDFFF))
        {
            throw new RuntimeException("Low surrogate character");
        }

        if (charSizeDuospace)
        {
            if (chr < 128)
            {
                return 1;
            }

            if (charSizeMap.containsKey(chr))
            {
                return charSizeMap.get(chr);
            }

            getCursorPos();
            int tempX = terminalReport[1] - 1;
            int tempY = terminalReport[0] - 1;

            printChar(chr);
            printFlush();
            getCursorPos();
            int tempX0 = terminalReport[1] - 1;

            setCursorPos(tempX, tempY);

            charSizeMap.put(chr, tempX0 - tempX);

            //CommonTools.debug(Integer.toHexString(chr) + " ");
            //CommonTools.debug(chr + " ");
            //CommonTools.debug((new StringUTF()).append(chr).get());
            //CommonTools.debug("=");
            //CommonTools.debugln((tempX0 - tempX) + "");

            return tempX0 - tempX;
        }
        else
        {
            return 1;
        }
    }
}
