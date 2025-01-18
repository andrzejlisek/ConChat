/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conchat;

/**
 *
 * @author xxx
 */
public class ScreenTextInput
{
    ConsoleInputOutput ConsoleInputOutput_;
    String textValue = "";
    int textPos = 0;
    int dispPos = 0;
    
    int fieldPos;
    int fieldSize;
    
    public ScreenTextInput(ConsoleInputOutput ConsoleInputOutput__)
    {
        ConsoleInputOutput_ = ConsoleInputOutput__;
    }
    
    public void reset()
    {
        textValue = "";
        textPos = 0;
        dispPos = 0;
        repaintAll();
    }
    
    void repaintAll()
    {
        repaint(0, -1);
    }
    
    void repaint(int first, int last)
    {
        if (last < 0)
        {
            last = (fieldSize * ConsoleInputOutput_.screenWidth);
        }
        
        if (last > (fieldSize * ConsoleInputOutput_.screenWidth))
        {
            last = (fieldSize * ConsoleInputOutput_.screenWidth);
        }
        
        ConsoleInputOutput_.setTextAttrBold1();
        ConsoleInputOutput_.setTextAttrReverse1();
        if (first >= 0)
        {
            int firstRow = first / ConsoleInputOutput_.screenWidth;
            int firstCol = first % ConsoleInputOutput_.screenWidth;
            int lastRow = last / ConsoleInputOutput_.screenWidth;
            int lastCol = last % ConsoleInputOutput_.screenWidth;

            String strToPaint = textValue.substring(dispPos);
            if (strToPaint.length() < last)
            {
                strToPaint = strToPaint + CommonTools.stringIndent(last - strToPaint.length(), ' ');
            }

            int substrPtr = firstRow * ConsoleInputOutput_.screenWidth;
            for (int ii = firstRow; ii <= lastRow; ii++)
            {
                int i_1 = (ii > firstRow) ? 0 : firstCol;
                int i_2 = (ii < lastRow) ? ConsoleInputOutput_.screenWidth : lastCol;
                ConsoleInputOutput_.setCursorPos(i_1, fieldPos + ii);
                ConsoleInputOutput_.printString(strToPaint.substring(substrPtr + i_1, substrPtr + i_2));
                substrPtr += ConsoleInputOutput_.screenWidth;
            }
        }
        
        int cursorPos = textPos - dispPos;
        ConsoleInputOutput_.setCursorPos((cursorPos % ConsoleInputOutput_.screenWidth), ConsoleInputOutput_.screenHeight - fieldSize + (cursorPos / ConsoleInputOutput_.screenWidth));
        ConsoleInputOutput_.setTextAttrReverse0();
        ConsoleInputOutput_.setTextAttrBold0();
    }

    void repaintCursor()
    {
        int cursorPos = textPos - dispPos;
        ConsoleInputOutput_.setCursorPos((cursorPos % ConsoleInputOutput_.screenWidth), ConsoleInputOutput_.screenHeight - fieldSize + (cursorPos / ConsoleInputOutput_.screenWidth));
    }
    
    public boolean keyEvent(int key)
    {
        int fieldChars = (fieldSize * ConsoleInputOutput_.screenWidth);
        
        int repaint1 = -1;
        int repaint2 = -1;
        
        switch (key)
        {
            case 0:
                {
                    repaint1 = 0;
                }
                break;
            case (ConsoleInputOutput.keySpecialNum):
                {
                    reset();
                }
                return true;
            case (ConsoleInputOutput.keySpecialNum + 3):
                if (textPos < textValue.length())
                {
                    textPos++;
                }
                else
                {
                    ConsoleInputOutput_.ringBell();
                }
                break;
            case (ConsoleInputOutput.keySpecialNum + 4):
                if (textPos > 0)
                {
                    textPos--;
                }
                else
                {
                    ConsoleInputOutput_.ringBell();
                }
                break;
            case 127:
            case 8:
                if (textPos > 0)
                {
                    textValue = textValue.substring(0, textPos - 1) + textValue.substring(textPos);
                    textPos--;
                    repaint1 = textPos - dispPos;
                    repaint2 = textValue.length() - dispPos + 1;
                }
                else
                {
                    ConsoleInputOutput_.ringBell();
                }
                break;
            case ConsoleInputOutput.keySpecialNum + 12:
                if (textPos < textValue.length())
                {
                    textValue = textValue.substring(0, textPos) + textValue.substring(textPos + 1);
                    repaint1 = textPos - dispPos;
                    repaint2 = textValue.length() - dispPos + 1;
                }
                else
                {
                    ConsoleInputOutput_.ringBell();
                }
                break;
            case 13:
            case 10:
                return false;
        }
        if ((key >= 32) && (key < ConsoleInputOutput.keySpecialNum) && (key != 127))
        {
            textValue = textValue.substring(0, textPos) + (char)key + textValue.substring(textPos);
            textPos++;
            repaint1 = textPos - dispPos - 1;
            repaint2 = textValue.length() - dispPos + 1;
        }
        if (dispPos > textPos)
        {
            dispPos = textPos;
            repaint1 = 0;
            repaint2 = textValue.length() - dispPos + 1;
        }
        if (dispPos < (textPos - fieldChars + 1))
        {
            dispPos = (textPos - fieldChars + 1);
            repaint1 = 0;
            repaint2 = textValue.length() - dispPos + 1;
        }
        repaint(repaint1, repaint2);
        return true;
    }

    public void textWrite(String txt)
    {
        for (int i = 0; i < txt.length(); i++)
        {
            int chr = txt.charAt(i);
            if (chr >= 32)
            {
                keyEvent(chr);
            }
        }
    }
}
