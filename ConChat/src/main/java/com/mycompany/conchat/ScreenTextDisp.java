/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conchat;

import java.util.ArrayList;

/**
 *
 * @author xxx
 */
public class ScreenTextDisp
{
    private ConfigFile CF;
    
    private final boolean debugLineInfo = false;
    private boolean blockUnderline = true;
    int textWidth = 80;
    int textHeight = 23;
    int textOffsetLine = 11;
    int tableCellWidth = 0;
    private final ConsoleInputOutput ConsoleInputOutput_;
    String fileName = "";
    
    ArrayList<ScreenTextDispRawItem> textRaw;
    ArrayList<ScreenTextDispMessage> textMsg;
    int blockIdCounter;
    int messageIdxCounter;

    private boolean MarkdownBold;
    private boolean MarkdownItalic;
    private boolean MarkdownQuote;
    private boolean MarkdownQuote2;
    private boolean MarkdownEquation;
    private boolean MarkdownQuoteBlock;
    private boolean MarkdownEquationBlock;
    private boolean MarkdownAfterListItem;
    private boolean MarkdownTable;
    
    private int[] MarkdownWindow;
    private static final int MarkdownWindowSize = 5;

    void textRawItemMarkdownParseReset(boolean win, boolean block)
    {
        if (win)
        {
            MarkdownWindow = new int[MarkdownWindowSize];
        }
        MarkdownBold = false;
        MarkdownItalic = false;
        MarkdownQuote = false;
        MarkdownQuote2 = false;
        MarkdownEquation = false;
        MarkdownAfterListItem = false;
        if (block)
        {
            MarkdownQuoteBlock = false;
            MarkdownEquationBlock = false;
            MarkdownTable = false;
        }
    }
    
    /**
     * Convert last parsed text into table, invoke after last table line in source Markdown
     * @param idx Index of last table line before creation
     * @return Index of last table line after creation
     */
    int textRawTableCreate(int idx)
    {
        int idx1 = idx;
        int idx2 = idx;

        int id = textRaw.get(idx1).blockId;
        while ((idx >= 0) && (id == textRaw.get(idx1).blockId))
        {
            idx1--;
        }
        idx1++;

        // Wrapping long cell text
        int cellHeaderSize = 1;
        if (tableCellWidth > 0)
        {
            // Counting table columns
            int tableColumns = 0;
            int tableOffset = 0;
            for (int i = idx1; i <= idx2; i++)
            {
                int columnCount = 0;
                int lastPos = textRaw.get(i).textLine.indexOf("|");
                if (lastPos >= 0)
                {
                    tableOffset = Math.max(tableOffset, lastPos);
                    while (lastPos >= 0)
                    {
                        columnCount++;
                        lastPos = textRaw.get(i).textLine.indexOf("|", lastPos + 1);
                    }
                    tableColumns = Math.max(tableColumns, columnCount);
                }
            }
            
            int tableCellWidth_ = ((textWidth - 1 - tableOffset) / (tableColumns - 1)) - 1;
            if (tableCellWidth_ < tableCellWidth)
            {
                tableCellWidth_ = tableCellWidth;
            }
            
            
            ArrayList<Integer> cellSeparator = new ArrayList<Integer>();
            ArrayList<ScreenTextDisp> cellContent = new ArrayList<>();
            for (int i = idx1; i <= idx2; i++)
            {
                cellSeparator.clear();
                cellContent.clear();
                int lastPos = textRaw.get(i).textLine.indexOf("|");
                while (lastPos >= 0)
                {
                    cellSeparator.add(lastPos);
                    lastPos = textRaw.get(i).textLine.indexOf("|", lastPos + 1);
                }
                for (int ii = 1; ii < cellSeparator.size(); ii++)
                {
                    ScreenTextDisp cellItem = new ScreenTextDisp(ConsoleInputOutput_, CF);
                    ScreenTextDispRawItem cellItem_ = new ScreenTextDispRawItem(textRaw.get(i), true);
                    cellItem_.remove(cellSeparator.get(ii), -1);
                    cellItem_.remove(0, cellSeparator.get(ii - 1) + 1);

                    cellItem.textRaw.add(cellItem_);
                    cellContent.add(cellItem);
                }

                int cellMaxLines = 1;
                for (int ii = 0; ii < cellContent.size(); ii++)
                {
                    cellContent.get(ii).textRaw.get(0).textType = ScreenTextDispRawItem.textTypeDef.normal;
                    cellContent.get(ii).textWidth = tableCellWidth_;
                    cellContent.get(ii).textRaw.get(0).trim(true, true);
                    cellContent.get(ii).textRawItemWrap(0);
                    cellMaxLines = Math.max(cellMaxLines, cellContent.get(ii).textRaw.size());
                }
                if (i == idx1) cellHeaderSize = cellMaxLines;

                for (int ii = 0; ii < cellMaxLines; ii++)
                {
                    ScreenTextDispRawItem cellItem_ = new ScreenTextDispRawItem(textRaw.get(i), false);
                    cellItem_.append('|');
                    for (int iii = 0; iii < cellContent.size(); iii++)
                    {
                        if (cellContent.get(iii).textRaw.size() > ii)
                        {
                            cellItem_.unWrap(cellContent.get(iii).textRaw.get(ii));
                        }
                        cellItem_.append('|');
                    }

                    textRaw.add(i, cellItem_);
                    idx++;
                    idx2++;
                    i++;
                }

                textRaw.remove(i);
                idx--;
                idx2--;
                i--;
            }
        }
        
        
        
        // Expanding and correcting the table border
        if (true)
        {
            ScreenTextDispRawItem tableHeader = new ScreenTextDispRawItem(textRaw.get(idx1), false);
            ScreenTextDispRawItem tableFooter = new ScreenTextDispRawItem(textRaw.get(idx2), false);

            tableHeader.textLine = "" + CommonTools.table1;
            String tableMiddle_textLine = "" + CommonTools.tableL;
            tableFooter.textLine = "" + CommonTools.table3;

            int[] charPos = new int[idx2 - idx1 + 1];
            int lastPos = 0;
            while (true)
            {
                int posMin = 0;
                int posMax = -1;
                for (int i = idx1; i <= idx2; i++)
                {
                    charPos[i - idx1] = textRaw.get(i).textLine.indexOf("|", lastPos);
                    posMin = Math.min(posMin, charPos[i - idx1]);
                    posMax = Math.max(posMax, charPos[i - idx1]);
                }
                if (posMin < 0)
                {
                    if (posMax < 0)
                    {
                        break;
                    }
                    else
                    {
                        for (int i = idx1; i <= idx2; i++)
                        {
                            if (charPos[i - idx1] < 0) textRaw.get(i).append('|');
                        }
                    }
                }
                else
                {
                    for (int i = idx1; i <= idx2; i++)
                    {
                        if (charPos[i - idx1] < posMax) textRaw.get(i).insert(charPos[i - idx1], CommonTools.stringIndent(posMax - charPos[i - idx1], ' '), true);
                    }
                    if (lastPos > 0)
                    {
                        tableHeader.textLine += CommonTools.stringIndent(posMax - lastPos, CommonTools.tableH);
                        tableHeader.textLine += CommonTools.tableT;
                        tableMiddle_textLine += CommonTools.stringIndent(posMax - lastPos, CommonTools.tableH);
                        tableMiddle_textLine += CommonTools.tableC;
                        tableFooter.textLine += CommonTools.stringIndent(posMax - lastPos, CommonTools.tableH);
                        tableFooter.textLine += CommonTools.tableB;
                    }
                    else
                    {
                        tableHeader.textLine += CommonTools.stringIndent(posMax - lastPos, ' ');
                        tableMiddle_textLine += CommonTools.stringIndent(posMax - lastPos, ' ');
                        tableFooter.textLine += CommonTools.stringIndent(posMax - lastPos, ' ');
                    }
                    lastPos = posMax + 1;
                }
            }

            for (int i = idx1; i <= idx2; i++)
            {
                int chrIdx = textRaw.get(i).textLine.indexOf('|');
                while (chrIdx >= 0)
                {
                    textRaw.get(i).textLine = CommonTools.stringSetChar(textRaw.get(i).textLine, chrIdx, CommonTools.tableV);
                    chrIdx = textRaw.get(i).textLine.indexOf('|', chrIdx + 1);
                }
            }
            tableHeader.textLine = tableHeader.textLine.substring(0, lastPos - 1) + CommonTools.table2;
            tableMiddle_textLine = tableMiddle_textLine.substring(0, lastPos - 1) + CommonTools.tableR;
            tableFooter.textLine = tableFooter.textLine.substring(0, lastPos - 1) + CommonTools.table4;
            textRaw.get(idx1 + cellHeaderSize).textLine = tableMiddle_textLine;
            textRaw.get(idx1 + 1).cmdIdx.clear();
            textRaw.get(idx1 + 1).cmdTxt.clear();

            textRaw.add(idx2 + 1, tableFooter);
            textRaw.add(idx1, tableHeader);
            idx += 2;
        }
        return idx;
    }
    
    /**
     * Parse Markdown after new line character
     * @param idx Unparsed text line index
     * @return Last text line after parse and wrap
     */
    int textRawItemMarkdownParse(int idx)
    {
        ScreenTextDispRawItem item = textRaw.get(idx);
        item.MessageIdx = messageIdxCounter;
        try
        {
            boolean notInBlock = (!MarkdownEquationBlock) && (!MarkdownQuoteBlock) && (!MarkdownEquation) && (!MarkdownQuote);

            int textLineBegin = 0;

            int noSpaceIdx = 0;
            if (notInBlock)
            {
                while ((noSpaceIdx < item.textLine.length()) && ((item.textLine.charAt(noSpaceIdx) == ' ') || (item.textLine.charAt(noSpaceIdx) == '>')))
                {
                    noSpaceIdx++;
                }
                if (noSpaceIdx < (item.textLine.length() - 1))
                {
                    // Punctation list item
                    int nextSpace = item.textLine.indexOf(" ", noSpaceIdx);
                    if (nextSpace > 0)
                    {
                        char itemChr = item.textLine.charAt(nextSpace - 1);
                        if (nextSpace - noSpaceIdx == 1)
                        {
                            switch (itemChr)
                            {
                                case '-':
                                case '*':
                                case '+':
                                    textLineBegin = nextSpace + 1;
                                    while ((textLineBegin < (item.textLine.length() - 1)) && (item.textLine.charAt(textLineBegin) == ' '))
                                    {
                                        textLineBegin++;
                                    }
                                    item.indent = noSpaceIdx;
                                    item.indentNext = textLineBegin;
                                    MarkdownAfterListItem = true;
                                    break;
                            }
                        }
                        if ((nextSpace - noSpaceIdx > 1) && (itemChr == '.'))
                        {
                            boolean onlyDigits = true;
                            for (int ii = noSpaceIdx; ii < (nextSpace - 1); ii++)
                            {
                                char digitChr = item.textLine.charAt(ii);
                                if ((digitChr < 48) || (digitChr > 57))
                                {
                                    onlyDigits = false;
                                    break;
                                }
                            }
                            if (onlyDigits)
                            {
                                textLineBegin = nextSpace + 1;
                                while ((textLineBegin < (item.textLine.length() - 1)) && (item.textLine.charAt(textLineBegin) == ' '))
                                {
                                    textLineBegin++;
                                }
                                item.indent = noSpaceIdx;
                                item.indentNext = textLineBegin;
                                MarkdownAfterListItem = true;
                            }
                        }
                    }
                }
                else
                {
                    // Blank line resets text attribute state
                    textRawItemMarkdownParseReset(false, false);
                }
            }

            
            String windowingText = "     " + item.textLine.substring(textLineBegin) + "     ";
            int removedChars = 0;
            if (MarkdownQuoteBlock || MarkdownEquationBlock)
            {
                item.textType = ScreenTextDispRawItem.textTypeDef.code;
                item.blockId = blockIdCounter;
            }
            for (int i = 0; i < windowingText.length() - 5; i++)
            {
                MarkdownWindow[0] = windowingText.charAt(i + 0);
                MarkdownWindow[1] = windowingText.charAt(i + 1);
                MarkdownWindow[2] = windowingText.charAt(i + 2);
                MarkdownWindow[3] = windowingText.charAt(i + 3);
                MarkdownWindow[4] = windowingText.charAt(i + 4);

                notInBlock = (!MarkdownEquationBlock) && (!MarkdownQuoteBlock) && (!MarkdownEquation) && (!MarkdownQuote);
                boolean __EB = !MarkdownEquationBlock;
                boolean __QB = !MarkdownQuoteBlock;
                boolean __E = !MarkdownEquation;
                boolean __Q = !MarkdownQuote;
                int idx0 = i + textLineBegin - removedChars;

                // Single character
                if ((MarkdownWindow[2] != '\\'))
                {
                    if (notInBlock && (MarkdownWindow[2] != '_') && (MarkdownWindow[3] == '_') && (MarkdownWindow[4] != '_'))
                    {
                        if (MarkdownItalic)
                        {
                            if ((!CommonTools.isSpaceOrSpecialChar(MarkdownWindow[2])) && (CommonTools.isSpaceOrSpecialChar(MarkdownWindow[4])))
                            {
                                MarkdownItalic = !MarkdownItalic;
                                item.remove(idx0 - 2, 1);
                                item.setCommand(idx0 - 2, MarkdownItalic ? 21 : 20);
                                removedChars++;
                            }
                        }
                        else
                        {
                            if ((CommonTools.isSpaceOrSpecialChar(MarkdownWindow[2])) && (!CommonTools.isSpaceOrSpecialChar(MarkdownWindow[4])))
                            {
                                MarkdownItalic = !MarkdownItalic;
                                item.remove(idx0 - 2, 1);
                                item.setCommand(idx0 - 2, MarkdownItalic ? 21 : 20);
                                removedChars++;
                            }
                        }
                    }
                    if (notInBlock && (MarkdownWindow[2] != '*') && (MarkdownWindow[3] == '*') && (MarkdownWindow[4] != '*'))
                    {
                        if (MarkdownItalic)
                        {
                            if ((!CommonTools.isSpaceOrSpecialChar(MarkdownWindow[2])) && (CommonTools.isSpaceOrSpecialChar(MarkdownWindow[4])))
                            {
                                MarkdownItalic = !MarkdownItalic;
                                item.remove(idx0 - 2, 1);
                                item.setCommand(idx0 - 2, MarkdownItalic ? 21 : 20);
                                removedChars++;
                            }
                        }
                        else
                        {
                            if ((CommonTools.isSpaceOrSpecialChar(MarkdownWindow[2])) && (!CommonTools.isSpaceOrSpecialChar(MarkdownWindow[4])))
                            {
                                MarkdownItalic = !MarkdownItalic;
                                item.remove(idx0 - 2, 1);
                                item.setCommand(idx0 - 2, MarkdownItalic ? 21 : 20);
                                removedChars++;
                            }
                        }
                    }
                }

                if (__EB && __QB && __E && (MarkdownWindow[2] != '`') && (MarkdownWindow[3] == '`') && (MarkdownWindow[4] != '`'))
                {
                    if (!MarkdownQuote2)
                    {
                        if (MarkdownQuote || (MarkdownWindow[2] != '\\'))
                        {
                            MarkdownQuote = !MarkdownQuote;
                            item.remove(idx0 - 2, 1);
                            item.setCommand(idx0 - 2, MarkdownQuote ? 31 : 30);
                            removedChars++;
                        }
                    }
                }
                /*if (__EB && __QB && __Q && (MarkdownWindow[2] != '$') && (MarkdownWindow[3] == '$') && (MarkdownWindow[4] != '$'))
                {
                    MarkdownEquation = !MarkdownEquation;
                    item.remove(idx0 - 2, 1);
                    item.setCommand(idx0 - 2, MarkdownEquation ? 31 : 30);
                    removedChars++;
                }*/

                // Double character
                if (MarkdownWindow[1] != '\\')
                {
                    if (notInBlock && (MarkdownWindow[1] != '_') && (MarkdownWindow[2] == '_') && (MarkdownWindow[3] == '_') && (MarkdownWindow[4] != '_'))
                    {
                        if (MarkdownBold)
                        {
                            //if ((!CommonTools.isSpaceOrSpecialChar(MarkdownWindow[1])) && (CommonTools.isSpaceOrSpecialChar(MarkdownWindow[4])))
                            {
                                MarkdownBold = !MarkdownBold;
                                item.remove(idx0 - 3, 2);
                                item.setCommand(idx0 - 3, MarkdownBold ? 11 : 10);
                                removedChars++;
                                removedChars++;
                            }
                        }
                        else
                        {
                            //if ((CommonTools.isSpaceOrSpecialChar(MarkdownWindow[1])) && (!CommonTools.isSpaceOrSpecialChar(MarkdownWindow[4])))
                            {
                                MarkdownBold = !MarkdownBold;
                                item.remove(idx0 - 3, 2);
                                item.setCommand(idx0 - 3, MarkdownBold ? 11 : 10);
                                removedChars++;
                                removedChars++;
                            }
                        }
                    }
                    if (notInBlock && (MarkdownWindow[1] != '*') && (MarkdownWindow[2] == '*') && (MarkdownWindow[3] == '*') && (MarkdownWindow[4] != '*'))
                    {
                        if (MarkdownBold)
                        {
                            //if ((!CommonTools.isSpaceOrSpecialChar(MarkdownWindow[1])) && (CommonTools.isSpaceOrSpecialChar(MarkdownWindow[4])))
                            {
                                MarkdownBold = !MarkdownBold;
                                item.remove(idx0 - 3, 2);
                                item.setCommand(idx0 - 3, MarkdownBold ? 11 : 10);
                                removedChars++;
                                removedChars++;
                            }
                        }
                        else
                        {
                            //if ((CommonTools.isSpaceOrSpecialChar(MarkdownWindow[1])) && (!CommonTools.isSpaceOrSpecialChar(MarkdownWindow[4])))
                            {
                                MarkdownBold = !MarkdownBold;
                                item.remove(idx0 - 3, 2);
                                item.setCommand(idx0 - 3, MarkdownBold ? 11 : 10);
                                removedChars++;
                                removedChars++;
                            }
                        }
                    }
                    if (notInBlock && (MarkdownWindow[1] != '~') && (MarkdownWindow[2] == '~') && (MarkdownWindow[3] == '~') && (MarkdownWindow[4] != '~'))
                    {
                        item.remove(idx0 - 3, 2);
                        removedChars++;
                        removedChars++;
                    }
                    if (__QB && __E && __Q && (MarkdownWindow[1] != '$') && (MarkdownWindow[2] == '$') && (MarkdownWindow[3] == '$') && (MarkdownWindow[4] != '$'))
                    {
                        MarkdownEquationBlock = !MarkdownEquationBlock;
                        if (MarkdownEquationBlock) blockIdCounter++;
                        item.remove(idx0 - 3, 2);
                        removedChars++;
                        removedChars++;
                    }
                    if (__QB && __E && __Q && (MarkdownWindow[1] != '$') && (MarkdownWindow[2] == '\\') && ((MarkdownWindow[3] == '[') || (MarkdownWindow[3] == ']')) && (MarkdownWindow[4] != '$'))
                    {
                        MarkdownEquationBlock = !MarkdownEquationBlock;
                        if (MarkdownEquationBlock) blockIdCounter++;
                        item.remove(idx0 - 3, 2);
                        removedChars++;
                        removedChars++;
                    }
                    if (__EB && __E && __Q && (MarkdownWindow[1] != '`') && (MarkdownWindow[2] == '`') && (MarkdownWindow[3] == '`') && (MarkdownWindow[4] != '`'))
                    {
                        MarkdownQuote2 = !MarkdownQuote2;
                        item.remove(idx0 - 3, 2);
                        item.setCommand(idx0 - 3, MarkdownQuote2 ? 31 : 30);
                        removedChars++;
                        removedChars++;
                    }
                }

                // Triple character
                if (MarkdownWindow[0] != '\\')
                {
                    if (notInBlock && (MarkdownWindow[0] != '*') && (MarkdownWindow[1] == '*') && (MarkdownWindow[2] == '*') && (MarkdownWindow[3] == '*') && (MarkdownWindow[4] != '*'))
                    {
                        if (MarkdownBold && MarkdownItalic)
                        {
                            if ((!CommonTools.isSpaceOrSpecialChar(MarkdownWindow[0])) && (CommonTools.isSpaceOrSpecialChar(MarkdownWindow[4])))
                            {
                                MarkdownBold = !MarkdownBold;
                                MarkdownItalic = !MarkdownItalic;
                                item.remove(idx0 - 4, 3);
                                item.setCommand(idx0 - 4, MarkdownBold ? 11 : 10);
                                item.setCommand(idx0 - 4, MarkdownItalic ? 21 : 20);
                                removedChars += 3;
                            }
                        }
                        else
                        {
                            if ((CommonTools.isSpaceOrSpecialChar(MarkdownWindow[0])) && (!CommonTools.isSpaceOrSpecialChar(MarkdownWindow[4])))
                            {
                                MarkdownBold = !MarkdownBold;
                                MarkdownItalic = !MarkdownItalic;
                                item.remove(idx0 - 4, 3);
                                item.setCommand(idx0 - 4, MarkdownBold ? 11 : 10);
                                item.setCommand(idx0 - 4, MarkdownItalic ? 21 : 20);
                                removedChars += 3;
                            }
                        }
                    }
                    if (notInBlock && (MarkdownWindow[0] != '_') && (MarkdownWindow[1] == '_') && (MarkdownWindow[2] == '_') && (MarkdownWindow[3] == '_') && (MarkdownWindow[4] != '_'))
                    {
                        if (MarkdownBold && MarkdownItalic)
                        {
                            if ((!CommonTools.isSpaceOrSpecialChar(MarkdownWindow[0])) && (CommonTools.isSpaceOrSpecialChar(MarkdownWindow[4])))
                            {
                                MarkdownBold = !MarkdownBold;
                                MarkdownItalic = !MarkdownItalic;
                                item.remove(idx0 - 4, 3);
                                item.setCommand(idx0 - 4, MarkdownBold ? 11 : 10);
                                item.setCommand(idx0 - 4, MarkdownItalic ? 21 : 20);
                                removedChars += 3;
                            }
                        }
                        else
                        {
                            if ((CommonTools.isSpaceOrSpecialChar(MarkdownWindow[0])) && (!CommonTools.isSpaceOrSpecialChar(MarkdownWindow[4])))
                            {
                                MarkdownBold = !MarkdownBold;
                                MarkdownItalic = !MarkdownItalic;
                                item.remove(idx0 - 4, 3);
                                item.setCommand(idx0 - 4, MarkdownBold ? 11 : 10);
                                item.setCommand(idx0 - 4, MarkdownItalic ? 21 : 20);
                                removedChars += 3;
                            }
                        }
                    }
                    if (__EB && __E && __Q && (MarkdownWindow[0] != '`') && (MarkdownWindow[1] == '`') && (MarkdownWindow[2] == '`') && (MarkdownWindow[3] == '`') && (MarkdownWindow[4] != '`'))
                    {
                        MarkdownQuoteBlock = !MarkdownQuoteBlock;
                        if (MarkdownQuoteBlock) blockIdCounter++;
                        item.textType = ScreenTextDispRawItem.textTypeDef.code;
                        item.remove(idx0 - 4, 3);
                        removedChars += 3;
                    }
                    if (__EB && __E && __Q && (MarkdownWindow[0] != '~') && (MarkdownWindow[1] == '~') && (MarkdownWindow[2] == '~') && (MarkdownWindow[3] == '~') && (MarkdownWindow[4] != '~'))
                    {
                        MarkdownQuoteBlock = !MarkdownQuoteBlock;
                        if (MarkdownQuoteBlock) blockIdCounter++;
                        item.textType = ScreenTextDispRawItem.textTypeDef.code;
                        item.remove(idx0 - 4, 3);
                        removedChars += 3;
                    }
                }
                
                if (notInBlock)
                {
                    // Remove escape character
                    if (MarkdownWindow[3] == '\\')
                    {
                        int escapedChar = MarkdownWindow[4];
                        if (CommonTools.isSpecialChar(escapedChar))
                        {
                            // Not remove ]\ and \[
                            // Convert \[ and \] to $$
                            if ((MarkdownWindow[4] != '[') && (MarkdownWindow[4] != ']'))
                            {
                                item.remove(idx0 - 2, 1);
                                removedChars++;

                                // Modify windowing string to avoid recursively remove
                                if (escapedChar == '\\')
                                {
                                    windowingText = CommonTools.stringSetChar(windowingText, i + 4, 'X');
                                }
                            }
                            else
                            {
                                windowingText = CommonTools.stringSetChar(windowingText, i + 3, '$');
                                windowingText = CommonTools.stringSetChar(windowingText, i + 4, '$');
                            }
                        }
                    }
                    
                    // Remove multiple spaces
                    if ((MarkdownWindow[2] != ' ') && (MarkdownWindow[3] == ' ') && (MarkdownWindow[4] == ' '))
                    {
                        // Measue space sequence length
                        int spaceCount = 0;
                        int spaceMargin = 5;
                        int spaceWindowOffset = 3;
                        while ((windowingText.length() > (i + spaceWindowOffset + spaceCount + spaceMargin)) && (windowingText.charAt(i + spaceWindowOffset + spaceCount) == ' '))
                        {
                            spaceCount++;
                        }

                        // Remove additional spaces
                        while (spaceCount > 1)
                        {
                            item.remove(idx0 - 2, 1);
                            removedChars++;
                            spaceCount--;
                        }
                    }
                }
            }
            if (MarkdownQuoteBlock || MarkdownEquationBlock)
            {
                item.textType = ScreenTextDispRawItem.textTypeDef.code;
                item.blockId = blockIdCounter;
            }
            else
            {
                // Correct text indentations
                if (item.textType == ScreenTextDispRawItem.textTypeDef.normal)
                {
                    if (noSpaceIdx > 0)
                    {
                        //item.textLine = item.textLine.substring(noSpaceIdx);
                        item.remove(0, noSpaceIdx);
                    }
                    if ((noSpaceIdx < item.indent) && (noSpaceIdx < item.textLine.length()) && (!MarkdownAfterListItem))
                    {
                        item.indent = 0;
                        item.indentNext = 0;
                    }
                    if (item.indent > 0)
                    {
                        item.insert(0, CommonTools.stringIndent(item.indent, ' '), false);
                        //item.textLine = stringIndent(item.indent) + item.textLine;
                    }

                    //item.textLine = "|" + item.indent + "|" + item.indentNext + "|" + MarkdownAfterListItem + "|" + item.textLine;
                }

                windowingText = windowingText.trim();

                // header
                int headerType = 0;
                if (windowingText.startsWith("# "))
                {
                    headerType = 1;
                }
                if (windowingText.startsWith("## "))
                {
                    headerType = 2;
                }
                if (windowingText.startsWith("### "))
                {
                    headerType = 3;
                }
                if (windowingText.startsWith("#### "))
                {
                    headerType = 4;
                }
                if (windowingText.startsWith("##### "))
                {
                    headerType = 5;
                }
                if (windowingText.startsWith("###### "))
                {
                    headerType = 6;
                }
                if (headerType > 0)
                {
                    int idx0 = item.textLine.indexOf("# ") - headerType + 1;
                    item.remove(idx0, headerType + 1);
                    if (item.textLine.trim().endsWith("#") && (item.textLine.trim().contains(" ")))
                    {
                        int idxHash = item.textLine.length() - 1;
                        while (item.textLine.charAt(idxHash) != '#')
                        {
                            idxHash--;
                        }
                        while (item.textLine.charAt(idxHash) != ' ')
                        {
                            idxHash--;
                        }
                        if (idxHash < (item.textLine.length() - 1))
                        {
                            item.remove(idxHash, -1);
                        }
                    }
                    if ((headerType >= 1) && (headerType <= 2))
                    {
                        //textRaw.add(new ScreenTextDispRawItem(textRaw.get(idx), true));
                        textRaw.get(idx).lineFormat = 2;
                        //textRaw.get(idx + 1).lineFormat = 3;
                        //idx++;
                    }
                    if (headerType >= 3)
                    {
                        textRaw.get(idx).lineFormat = 1;
                    }
                }

                // Horizontal line
                if ((windowingText.equals("___")) || (windowingText.equals("***")) || (windowingText.equals("---")))
                {
                    item.textType = ScreenTextDispRawItem.textTypeDef.line;
                }
                
                // Technical data hidden as horizontal line
                if (windowingText.startsWith("___") && windowingText.endsWith("___"))
                {
                    item.textType = ScreenTextDispRawItem.textTypeDef.line;
                    if (windowingText.contains("<<<") || windowingText.contains(">>>"))
                    {
                        item.MessageIdx = -1;
                        textRawItemMarkdownParseReset(false, true);
                    }
                    if (windowingText.contains("(((") || windowingText.contains(")))"))
                    {
                        item.MessageIdx = -1;
                        item.textType = ScreenTextDispRawItem.textTypeDef.hidden;
                    }
                }
                
                // Table
                if (windowingText.startsWith("|") && windowingText.endsWith("|") && windowingText.contains("---") && (!MarkdownTable))
                {
                    if (idx > 0)
                    {
                        if (textRaw.get(idx - 1).textLine.trim().endsWith("|"))
                        {

                            String tableLine1 = textRawItemUnWrap(idx - 1, true).trim();
                            String tableLine2 = textRawItemUnWrap(idx, true).trim();
                            
                            if (tableLine1.startsWith("|") && tableLine1.endsWith("|"))
                            {
                                if (tableLine2.startsWith("|") && tableLine2.endsWith("|"))
                                {
                                    int idxRem = textRaw.get(idx).textLine.indexOf("-");
                                    while (idxRem > 0)
                                    {
                                        int idxRemLen = 0;
                                        while (textRaw.get(idx).textLine.charAt(idxRem + idxRemLen) == '-')
                                        {
                                            idxRemLen++;
                                        }
                                        textRaw.get(idx).remove(idxRem, idxRemLen);
                                        idxRem = textRaw.get(idx).textLine.indexOf("-");
                                    }
                                    
                                    textRawItemUnWrap(idx - 1, false);
                                    int idx0 = textRawItemUnWrapIdx1;
                                    idx -= (textRawItemUnWrapIdx2 - textRawItemUnWrapIdx1);

                                    MarkdownTable = true;
                                    blockIdCounter++;
                                    item.textType = ScreenTextDispRawItem.textTypeDef.table;
                                    item.blockId = blockIdCounter;
                                    textRaw.get(idx0).textType = item.textType;
                                    textRaw.get(idx0).blockId = item.blockId;
                                }
                            }
                        }
                    }
                }
                if (windowingText.startsWith("|") && windowingText.endsWith("|") && (MarkdownTable))
                {
                    item.textType = ScreenTextDispRawItem.textTypeDef.table;
                    item.blockId = blockIdCounter;
                }
                else
                {
                    if (MarkdownTable)
                    {
                        idx = textRawTableCreate(idx - 1);
                        MarkdownTable = false;
                    }
                }
            }
            if (item.textType == ScreenTextDispRawItem.textTypeDef.hidden)
            {
                textRaw.remove(idx);
                idx--;
            }
            
        }
        catch (Exception e)
        {
            item.textLine = "Parse error: " + e.getMessage();
            for (int i = 0; i < e.getStackTrace().length; i++)
            {
                String stackItem = e.getStackTrace()[i].toString();
                if (!stackItem.startsWith("java.base"))
                {
                    item.textLine = item.textLine + "|" + stackItem;
                }
            }
            blockIdCounter++;
            item.blockId = blockIdCounter;
            item.textType = ScreenTextDispRawItem.textTypeDef.code;
            blockIdCounter++;
        }
        return idx;
    }
    
    int textRawItemUnWrapIdx1;
    int textRawItemUnWrapIdx2;

    /**
     * Unwrap text necessary while table creation
     * @param idx 
     */
    String textRawItemUnWrap(int idx, boolean simulate)
    {
        int n = textRaw.get(idx).lineNumber;
        int idx1 = textRawFindIdx1Line(idx);
        textRawItemUnWrapIdx1 = idx1;

        int idx2 = textRawFindIdx2Line(idx);
        textRawItemUnWrapIdx2 = idx2;
        if (simulate)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = idx1; i < idx2; i++)
            {
                sb.append(textRaw.get(i).textLine);
                sb.append(textRaw.get(i).textLineWrap);
            }
            sb.append(textRaw.get(idx2).textLine);

            return sb.toString();
        }
        else
        {
            while (idx1 < idx2)
            {
                textRaw.get(idx1).unWrap(textRaw.get(idx1 + 1));
                textRaw.remove(idx1 + 1);
                idx2--;
            }
            return textRaw.get(idx1).textLine;
        }
    }
    
    /**
     * Wrap text after parsing
     * @param idx 
     */
    void textRawItemWrap(int idx)
    {
        if (textRaw.get(idx).textType == ScreenTextDispRawItem.textTypeDef.normal)
        {
            int textWidthWrap = textWidth;
            if (textRaw.get(idx).lineFormat > 0)
            {
                textWidthWrap = textWidthWrap / 2;
            }
            int idx0 = idx;
            for (int i = 0; i < textRaw.get(idx).textLine.length(); i++)
            {
                if ((textRaw.get(idx).textLine.charAt(i) != 32) && (i >= textWidthWrap))
                {
                    textRaw.add(new ScreenTextDispRawItem(textRaw.get(idx), false));
                    idx++;
                    for (int ii = 0; ii < textRaw.get(idx).indent; ii++)
                    {
                        textRaw.get(idx).append(' ');
                    }

                    int lastSpace = textRaw.get(idx - 1).textLine.substring(0, i).lastIndexOf(' ');
                    if (lastSpace >= textRaw.get(idx).indent)
                    {
                        int lastSpace0 = lastSpace;
                        while ((lastSpace0 > 0) && (textRaw.get(idx - 1).textLine.charAt(lastSpace0) == ' '))
                        {
                            lastSpace0--;
                        }
                        lastSpace0++;
                        lastSpace++;
                        textRaw.get(idx).moveSuffix(textRaw.get(idx - 1), lastSpace);
                        i -= lastSpace;
                        i += textRaw.get(idx).indent;
                        if (lastSpace0 < lastSpace)
                        {
                            textRaw.get(idx - 1).textLineWrap = textRaw.get(idx - 1).textLine.substring(lastSpace0);
                            textRaw.get(idx - 1).textLine = textRaw.get(idx - 1).textLine.substring(0, lastSpace0);
                        }
                    }
                    else
                    {
                        textRaw.get(idx).moveSuffix(textRaw.get(idx - 1), i);
                        i = textRaw.get(idx).indent;
                    }
                    textRaw.get(idx).textType = textRaw.get(idx - 1).textType;
                }
            }
            if (textRaw.get(idx).textLine.endsWith(" "))
            {
                int spaceIdx = textRaw.get(idx).textLine.length() - 1;
                while ((spaceIdx >= 0) && (textRaw.get(idx).textLine.charAt(spaceIdx) == ' '))
                {
                    spaceIdx--;
                }
                if (spaceIdx >= 0)
                {
                    textRaw.get(idx).textLineWrap = textRaw.get(idx).textLine.substring(spaceIdx + 1);
                    textRaw.get(idx).textLine = textRaw.get(idx).textLine.substring(0, spaceIdx + 1);
                }
                else
                {
                    textRaw.get(idx).textLineWrap = textRaw.get(idx).textLine;
                    textRaw.get(idx).textLine = "";
                }
            }
            if (textRaw.get(idx).lineFormat == 2)
            {
                for (int i = idx0; i <= idx; i += 2)
                {
                    textRaw.add(i, new ScreenTextDispRawItem(textRaw.get(i), true));
                    textRaw.get(i + 1).lineFormat = 3;
                    idx++;
                }
            }
        }
    }
    
    
    
    int supplyLineNumber;
    
    public ScreenTextDisp(ConsoleInputOutput ConsoleInputOutput__, ConfigFile CF_)
    {
        CF = CF_;
        ConsoleInputOutput_ = ConsoleInputOutput__;
        textRaw = new ArrayList<>();
        textMsg = new ArrayList<>();
    }
    
    /**
     * Get the text of message in the middle of screen
     * @return 
     */
    public String getCurrentMessage()
    {
        int t = textRaw.get(displayOffset).MessageIdx;
        if (t >= 0)
        {
            return textMsg.get(t).message;
        }
        return "";
    }
    
    /**
     * Enable or disable message ommit
     */
    public void ommitSwitch()
    {
        int t = textRaw.get(displayOffset).MessageIdx;
        if (t >= 0)
        {
            textMsg.get(t).ommit = !textMsg.get(t).ommit;
            int idx1 = textRawFindIdx1Message(displayOffset);
            int idx2 = textRawFindIdx2Message(displayOffset);
            //displayIdx(idx1, idx2);
            displayIdx(0, idx2);
            supplyLine("___(((" + t + ")))___");
        }
    }
    
    /**
     * Get current message length
     * @return Messag length in tokens
     */
    public int getMessageLength()
    {
        int t = textRaw.get(displayOffset).MessageIdx;
        if (t >= 0)
        {
            return textMsg.get(t).tokens;
        }
        return 0;
    }
    
    private void displayUseCmd(int cmd, int i)
    {
        switch (cmd)
        {
            case 11:
                ConsoleInputOutput_.setTextAttrBold1();
                break;
            case 10:
                ConsoleInputOutput_.setTextAttrBold0();
                break;
            case 21:
                ConsoleInputOutput_.setTextAttrItalic1();
                break;
            case 20:
                ConsoleInputOutput_.setTextAttrItalic0();
                break;
            case 31:
                ConsoleInputOutput_.setTextAttrReverse1();
                if (blockUnderline && (i == textHeight - 1))
                {
                    displayUnderline = true;
                    ConsoleInputOutput_.setTextAttrUnderline1();
                }
                break;
            case 30:
                ConsoleInputOutput_.setTextAttrReverse0();
                if (blockUnderline && (i == textHeight - 1))
                {
                    ConsoleInputOutput_.setTextAttrUnderline0();
                }
                break;
        }
    }
    
    /**
     * Perform the scroll up while displaying
     * @param n 
     */
    public void displayScrollUp(int n)
    {
        if (n > displayOffset)
        {
            n = displayOffset;
        }
        if (n <= (0 - 9))
        {
            displayOffset = 0;
            return;
        }
        if (n < 0)
        {
            displayOffset = 0;
            displayAll();
            return;
        }

        if (n > 0)
        {
            ConsoleInputOutput_.setScrollRegion(0, textHeight - 1);
            ConsoleInputOutput_.setCursorPos(0, 0);
            displayUnderline = false;
            while (n > 0)
            {
                ConsoleInputOutput_.scrollUp();
                displayBackground.remove(textHeight - 1);
                displayBackground.add(0, 0);
                displayOffset--;
                display(0, 1);
                n--;
            }
            ConsoleInputOutput_.setScrollRegion(-1, -1);
            if (displayIsCode(textHeight - 1))
            {
                display(textHeight - 1, textHeight);
            }
        }
    }
    
    /**
     * Perform the scroll down while displaying
     * @param n 
     */
    public void displayScrollDn(int n)
    {
        if (n <= (0 - 9))
        {
            displayOffset = textRaw.size() - 1;
            return;
        }
        if (n < 0)
        {
            displayOffset = textRaw.size() - 1;
            displayAll();
            return;
        }

        if (n > textRaw.size() - 1 - displayOffset)
        {
            n = textRaw.size() - 1 - displayOffset;
        }
        if (n > 0)
        {
            ConsoleInputOutput_.setScrollRegion(0, textHeight - 1);
            ConsoleInputOutput_.setCursorPos(0, textHeight - 1);
            boolean blockUnderline_ = blockUnderline;
            if (blockUnderline_)
            {
                blockUnderline = false;
                if (displayUnderline)
                {
                    display(textHeight - 1, textHeight);
                }
            }
            while (n > 0)
            {
                ConsoleInputOutput_.scrollDn();
                displayBackground.remove(0);
                displayBackground.add(0);
                displayOffset++;
                if (blockUnderline_ && (n == 1))
                {
                    blockUnderline = true;
                }
                display(textHeight - 1, textHeight);
                n--;
            }
            ConsoleInputOutput_.setScrollRegion(-1, -1);
        }
    }

    /**
     * Repaint the whole screen
     */
    public void displayAll()
    {
        display(0, textHeight);
    }

    /**
     * Detect of the line contains the code or quote
     * @param scrLine
     * @return 
     */
    private boolean displayIsCode(int scrLine)
    {
        if (blockUnderline)
        {
            int i_ = scrLine + displayOffset - textOffsetLine;
            if ((i_ >= 0) && (i_ < textRaw.size()))
            {
                if (textRaw.get(i_).textType == ScreenTextDispRawItem.textTypeDef.code)
                {
                    return true;
                }
                if (textRaw.get(i_).textType == ScreenTextDispRawItem.textTypeDef.normal)
                {
                    if (textRaw.get(i_).cmdTxt.indexOf(30) >= 0)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Perform the display based on the first index and last index instead of screen lines
     * @param idx1 First index
     * @param idx2 Last index
     */
    public void displayIdx(int idx1, int idx2)
    {
        int idx1Disp = idx1 - displayOffset + textOffsetLine;
        int idx2Disp = idx2 - displayOffset + textOffsetLine + 1;

        if (idx1Disp < 0) idx1Disp = 0;
        if (idx2Disp > textHeight) idx2Disp = textHeight;
        
        display(idx1Disp, idx2Disp);
    }
    
    private boolean displayUnderline = false;
    
    private static ArrayList<Integer> displayBackground = null;
    
    public static void displayResize(int textHeight)
    {
        if (displayBackground == null)
        {
            displayBackground = new ArrayList<>();
        }
        displayBackground.clear();
        while (displayBackground.size() < textHeight)
        {
            displayBackground.add(0);
        }
    }
    
    /**
     * Display the parsed text
     * @param i_min First screen line
     * @param i_max Last screen line incremented by 1
     */
    public void display(int i_min, int i_max)
    {
        if (displayOffset < 0)
        {
            displayOffset = 0;
        }
        if (displayOffset >= textRaw.size())
        {
            displayOffset = textRaw.size();
        }

        int contextBeginIdx = ChatEngine.contextBeginIdx(textMsg, CF);
        
        for (int i = i_min; i < i_max; i++)
        {
            ConsoleInputOutput_.setTextAttrReset();
            if (i == (textHeight - 1))
            {
                displayUnderline = false;
            }
            ConsoleInputOutput_.setCursorPos(0, i);
            ConsoleInputOutput_.setLineFormat(0);
            int ii_min = 0;
            int ii_max = 0;
            int i_ = i + displayOffset - textOffsetLine;
            if (((i_) >= 0) && ((i_) < textRaw.size()))
            {
                ScreenTextDispRawItem item = textRaw.get(i_);
                boolean ommit = false;
                boolean ommitZero = false;
                int ommitZeroI = 1;
                if (item.MessageIdx >= 0)
                {
                    ommit = textMsg.get(item.MessageIdx).ommit;
                    if (!ommit)
                    {
                        ommitZero = (textMsg.get(item.MessageIdx).tokens <= 0);
                        if (!ommitZero)
                        {
                            if (contextBeginIdx > item.MessageIdx)
                            {
                                ommitZero = true;
                            }
                        }
                    }
                }
                if (ommit || ommitZero)
                {
                    ConsoleInputOutput_.setTextAttrStrike1();
                }
                if (item.textType == ScreenTextDispRawItem.textTypeDef.line)
                {
                    for (int ii = 0; ii < textWidth; ii++)
                    {
                        if (ommitZero)
                        {
                            if (ii == ommitZeroI)
                            {
                                ConsoleInputOutput_.setTextAttrStrike0();
                            }
                            if (ii == (textWidth - ommitZeroI))
                            {
                                ConsoleInputOutput_.setTextAttrStrike1();
                            }
                        }
                        ConsoleInputOutput_.printChar(CommonTools.splitter);
                    }
                }
                else
                {
                    ConsoleInputOutput_.setLineFormat(item.lineFormat);
                    if (item.textType == ScreenTextDispRawItem.textTypeDef.code)
                    {
                        displayUseCmd(31, i);
                    }
                    ii_min = item.blockOffset;
                    ii_max = item.length();
                    if (ii_max > (textWidth + item.blockOffset)) ii_max = (textWidth + item.blockOffset);
                    if ((item.blockOffset > 0) && (item.textLine.trim().length() > 0))
                    {
                        ConsoleInputOutput_.printChar(CommonTools.scrollL);
                        ii_min++;
                    }
                    if ((item.length() - item.blockOffset) > textWidth)
                    {
                        ii_max--;
                    }
                    for (int iii = 0; iii < textRaw.get(i_).cmdIdx.size(); iii++)
                    {
                        if (textRaw.get(i_).cmdIdx.get(iii) < ii_min)
                        {
                            displayUseCmd(textRaw.get(i_).cmdTxt.get(iii), i);
                        }
                    }
                    for (int ii = ii_min; ii < ii_max; ii++)
                    {
                        if (ommitZero)
                        {
                            if ((ii - item.blockOffset) == ommitZeroI)
                            {
                                ConsoleInputOutput_.setTextAttrStrike0();
                            }
                            if ((ii - item.blockOffset) == (textWidth - ommitZeroI))
                            {
                                ConsoleInputOutput_.setTextAttrStrike1();
                            }
                        }
                        for (int iii = 0; iii < textRaw.get(i_).cmdIdx.size(); iii++)
                        {
                            if (textRaw.get(i_).cmdIdx.get(iii) == ii)
                            {
                                displayUseCmd(textRaw.get(i_).cmdTxt.get(iii), i);
                            }
                        }
                        ConsoleInputOutput_.printChar(textRaw.get(i_).textLine.charAt(ii));
                    }
                    if ((item.length() - item.blockOffset) > textWidth)
                    {
                        if (ommitZero)
                        {
                            ConsoleInputOutput_.setTextAttrStrike1();
                        }
                        ConsoleInputOutput_.printChar(CommonTools.scrollR);
                        ii_max++;
                    }
                    for (int iii = 0; iii < textRaw.get(i_).cmdIdx.size(); iii++)
                    {
                        if (textRaw.get(i_).cmdIdx.get(iii) >= ii_max)
                        {
                            displayUseCmd(textRaw.get(i_).cmdTxt.get(iii), i);
                        }
                    }
                }

                if ((item.textType == ScreenTextDispRawItem.textTypeDef.code) || (item.textType == ScreenTextDispRawItem.textTypeDef.table))
                {
                    int ii__ = 0;
                    if ((item.blockOffset > 0) && (item.textLine.trim().length() > 0))
                    {
                        ii__ = 1;
                    }
                    int ii_max_ = Math.max(ii__, ii_max - item.blockOffset);
                    for (int ii = ii_max_; ii < textWidth; ii++)
                    {
                        if (ommitZero)
                        {
                            if (ii == ommitZeroI)
                            {
                                ConsoleInputOutput_.setTextAttrStrike0();
                            }
                            if (ii == (textWidth - ommitZeroI))
                            {
                                ConsoleInputOutput_.setTextAttrStrike1();
                            }
                        }
                        ConsoleInputOutput_.printChar(' ');
                    }
                    if (item.textType == ScreenTextDispRawItem.textTypeDef.code)
                    {
                        displayUseCmd(30, i);
                    }
                }

                if (item.textType == ScreenTextDispRawItem.textTypeDef.normal)
                {
                    for (int ii = ii_max; ii < textWidth; ii++)
                    {
                        if (ommitZero)
                        {
                            if (ii == ommitZeroI)
                            {
                                ConsoleInputOutput_.setTextAttrStrike0();
                            }
                            if (ii == (textWidth - ommitZeroI))
                            {
                                ConsoleInputOutput_.setTextAttrStrike1();
                            }
                        }
                        ConsoleInputOutput_.printChar(' ');
                    }
                }

                if (ommit || ommitZero)
                {
                    ConsoleInputOutput_.setTextAttrStrike0();
                }
                
                if (debugLineInfo)
                {
                    ConsoleInputOutput_.setCursorPos(3, i);
                    ConsoleInputOutput_.printString("[" + item.lineNumber + "][" + item.MessageIdx + "]");
                }
                displayBackground.set(i, 0);
            }
            else
            {
                if (displayBackground.get(i) != 1)
                {
                    for (int ii = 0; ii < textWidth; ii++)
                    {
                        ConsoleInputOutput_.printChar(CommonTools.background);
                    }
                    displayBackground.set(i, 1);
                }
            }
            ConsoleInputOutput_.setTextAttrReset();
        }
    }

    public void supplyFile(String fileName, boolean msg)
    {
        messageIdxCounter = -1;
        boolean lastMsgOmmit = false;
        ArrayList<String> msgBuf = new ArrayList<>();
        int msgTokens = -1;
        boolean msgAnswer = false;

        ArrayList<String> fileRawData = CommonTools.fileLoadText(fileName, true);
        for (int i_ = 0; i_ < fileRawData.size(); i_++)
        {
            String S = fileRawData.get(i_);
            if (msg)
            {
                if ((S.startsWith("___<<<") && S.endsWith("<<<___")) || (S.startsWith("___>>>") && S.endsWith(">>>___")) || (S.startsWith("___(((") && S.endsWith(")))___")))
                {
                    if (S.startsWith("___(((") && S.endsWith(")))___"))
                    {
                        int msgNum = CommonTools.strToInt(S.substring(6, S.length() - 6), -1);
                        if ((msgNum >= 0) && (msgNum < textMsg.size()))
                        {
                            textMsg.get(msgNum).ommit = !textMsg.get(msgNum).ommit;
                        }
                        else
                        {
                            if (msgNum == textMsg.size())
                            {
                                lastMsgOmmit = !lastMsgOmmit;
                            }
                        }
                    }
                    else
                    {
                        messageIdxCounter = 0;

                        ScreenTextDispMessage item = ScreenTextDispMessage.supplyArrayListToStr(msgAnswer, msgBuf, msgTokens);
                        if (item != null)
                        {
                            item.ommit = lastMsgOmmit;
                            textMsg.add(item);
                            lastMsgOmmit = false;
                            //supplyLine(item.debugText());
                        }

                        msgBuf.clear();
                        int msgTokens0 = CommonTools.strToInt(S.substring(6, S.length() - 6), -1);
                        if (msgTokens0 >= 0) msgTokens = msgTokens0;

                        if (S.startsWith("___<<<") && S.endsWith("<<<___")) { msgAnswer = false; }
                        if (S.startsWith("___>>>") && S.endsWith(">>>___")) { msgAnswer = true; }
                    }
                }
                else
                {
                    if (messageIdxCounter >= 0)
                    {
                        messageIdxCounter = textMsg.size();
                    }
                    msgBuf.add(S);
                }
            }

            supplyLine(S);

        }
        {
            ScreenTextDispMessage item = ScreenTextDispMessage.supplyArrayListToStr(msgAnswer, msgBuf, msgTokens);
            if (item != null)
            {
                item.ommit = lastMsgOmmit;
                textMsg.add(item);
                //supplyLine(item.debugText());
            }
        }
    }

    int displayOffset;

    public void clear(boolean preservePosition)
    {
        if (!preservePosition)
        {
            displayOffset = 0;
        }
        blockIdCounter = 1;
        supplyLineNumber = 1;
        supplyPointTemp = false;
        textRaw.clear();
        textMsg.clear();
        messageIdxCounter = -1;
        textRaw.add(new ScreenTextDispRawItem(supplyLineNumber, 0));
        textRawItemMarkdownParseReset(true, true);
        if (fileName.length() > 0)
        {
            CommonTools.fileClear(fileName);
        }
    }
    
    int supplyPointSize;
    int supplyPointFileSize;
    boolean supplyPointTemp;
    
    public void supplyPointSave()
    {
        supplyPointSize = textRaw.size();
        if (fileName.length() > 0)
        {
            supplyPointFileSize = CommonTools.fileGetSize(fileName);
        }
        supplyPointTemp = true;
    }
    
    public void supplyPointRestore()
    {
        while (textRaw.size() > supplyPointSize)
        {
            textRaw.remove(textRaw.size() - 1);
        }
        supplyPointTemp = false;
    }

    public void supply(String raw)
    {
        if ((fileName.length() > 0) && (!supplyPointTemp))
        {
            CommonTools.fileSaveText(fileName, raw);
        }
        
        int t = textRaw.size() - 1;
        for (int i = 0; i < raw.length(); i++)
        {
            int chrN = raw.charAt(i);
            if (chrN == 10)
            {
                supplyLineNumber++;
                t = textRawItemMarkdownParse(t);
                textRawItemWrap(t);
                textRaw.add(new ScreenTextDispRawItem(supplyLineNumber, textRaw.get(t).indentNext));
                t = textRaw.size() - 1;
                if (t > 0)
                {
                    textRaw.get(t).MessageIdx = textRaw.get(t-1).MessageIdx;
                }
                if (MarkdownQuoteBlock || MarkdownEquationBlock)
                {
                    textRaw.get(t).textType = ScreenTextDispRawItem.textTypeDef.code;
                }
            }
            if (chrN == 9)
            {
                textRaw.get(t).append(' ');
                textRaw.get(t).append(' ');
            }
            if ((chrN >= 32) && (chrN != 127))
            {
                textRaw.get(t).append((char)chrN);
            }
        }
    }


    public void supplyLine(String raw)
    {
        supply(raw);
        supply("\n");
    }
    
    /**
     * Scroll horizontally the block while viewing
     * @param n
     * @return 
     */
    public boolean blockScroll(int n)
    {
        if ((displayOffset >= 0) && (displayOffset < textRaw.size()))
        {
            int id = textRaw.get(displayOffset).blockId;
            
            if (id != 0)
            {
                if ((n < 0) && (textRaw.get(displayOffset).blockOffset == 0))
                {
                    return true;
                }
                
                int maxOffset = textRaw.get(displayOffset).textLine.length();

                int idx1 = displayOffset;
                while ((idx1 >= 0) && (id == textRaw.get(idx1).blockId))
                {
                    maxOffset = Math.max(maxOffset, textRaw.get(idx1).textLine.length());
                    idx1--;
                }
                idx1++;
                int idx2 = displayOffset;
                while ((idx2 < textRaw.size()) && (id == textRaw.get(idx2).blockId))
                {
                    maxOffset = Math.max(maxOffset, textRaw.get(idx2).textLine.length());
                    idx2++;
                }
                idx2--;
                
                maxOffset = maxOffset - textWidth;

                if ((n > 0) && (textRaw.get(displayOffset).blockOffset >= maxOffset))
                {
                    return true;
                }
                
                for (int i = idx1; i <= idx2; i++)
                {
                    textRaw.get(i).blockOffset += n;
                }
                
                displayIdx(idx1, idx2);
                return true;
            }
        }
        return false;
    }

    /**
     * Find the first index of the wrapped line pointed by index
     * @param idx
     * @return 
     */
    int textRawFindIdx1Line(int idx)
    {
        int n = textRaw.get(idx).lineNumber;
        while ((idx >= 0) && (textRaw.get(idx).lineNumber == n))
        {
            idx--;
        }
        return (idx + 1);
    }

    /**
     * Find the last index of the wrapped line pointed by index
     * @param idx
     * @return 
     */
    int textRawFindIdx2Line(int idx)
    {
        int n = textRaw.get(idx).lineNumber;
        while ((idx < textRaw.size()) && (textRaw.get(idx).lineNumber == n))
        {
            idx++;
        }
        return (idx - 1);
    }

    /**
     * Find the first index of the message pointed by index
     * @param idx
     * @return 
     */
    int textRawFindIdx1Message(int idx)
    {
        int n = textRaw.get(idx).MessageIdx;
        while ((idx >= 0) && (textRaw.get(idx).MessageIdx == n))
        {
            idx--;
        }
        return (idx + 1);
    }

    /**
     * Find the last index of the message pointed by index
     * @param idx
     * @return 
     */
    int textRawFindIdx2Message(int idx)
    {
        int n = textRaw.get(idx).MessageIdx;
        while ((idx < textRaw.size()) && (textRaw.get(idx).MessageIdx == n))
        {
            idx++;
        }
        return (idx - 1);
    }

    
    public static String convSingleToMulti(String str)
    {
        StringBuilder str_ = new StringBuilder();
        for (int i = 0; i < str.length(); i++)
        {
            if ((str.charAt(i) == '\\') && (i < (str.length() - 1)))
            {
                i++;
                switch (str.charAt(i))
                {
                    case 'n':
                        str_.append("\n");
                        break;
                    case 't':
                        str_.append("\t");
                        break;
                    case '\\':
                        str_.append("\\");
                        break;
                    default:
                        str_.append('\\');
                        str_.append(str.charAt(i));
                        break;
                }
            }
            else
            {
                str_.append(str.charAt(i));
            }
        }
        str = str_.toString();
        return str;
    }

    public static String convMultiToSingle(String str)
    {
        StringBuilder str_ = new StringBuilder();
        for (int i = 0; i < str.length(); i++)
        {
            char chr = str.charAt(i);
            switch (chr)
            {
                case '\n':
                    str_.append("\\n");
                    break;
                case '\t':
                    str_.append("\\t");
                    break;
                case '\\':
                    str_.append("\\\\");
                    break;
                default:
                    str_.append(chr);
                    break;
            }
        }
        return str_.toString();
    }
    
    public static String convPlainToMarkdown(String str)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++)
        {
            char chr = str.charAt(i);
            if (CommonTools.isSpecialChar(chr)) sb.append("\\");
            sb.append(chr);
        }
        return sb.toString();
    }
    
    private static ScreenTextDisp convMarkdownToPlain_ = null;
    
    public static String convMarkdownToPlain(String str)
    {
        if (convMarkdownToPlain_ == null) convMarkdownToPlain_ = new ScreenTextDisp(null, null);
        convMarkdownToPlain_.textWidth = str.length() + 10;
        convMarkdownToPlain_.clear(false);
        convMarkdownToPlain_.supplyLine(str);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < (convMarkdownToPlain_.textRaw.size() - 1); i++)
        {
            if (i > 0) sb.append("\n");
            sb.append(convMarkdownToPlain_.textRaw.get(i).textLine);
        }
        return sb.toString();
    }
    
    /**
     * Convert plain text message into Markdown text consisting of quote block containing the message
     * @param str
     * @return 
     */
    public static String convMessageToMarkdown(String str)
    {
        str = "```" + str + "\n```";
        for (int i = 2; i < (str.length() - 6); i++)
        {
            char c0 = str.charAt(i + 0);
            char c1 = str.charAt(i + 1);
            char c2 = str.charAt(i + 2);
            char c3 = str.charAt(i + 3);
            char c4 = str.charAt(i + 4);
            
            // Convert tab into space
            if (c0 == '\t')
            {
                str = CommonTools.stringSetChar(str, i + 0, ' ');
            }
            if (c4 == '\t')
            {
                str = CommonTools.stringSetChar(str, i + 4, ' ');
            }
            
            // Convert ``` into `_` occurence within message to avoid display distortion
            if (((c0 == ' ') || (c0 == '\n')) && (c1 == '`') && (c2 == '`') && (c3 == '`') && ((c4 == ' ') || (c4 == '\n')))
            {
                str = CommonTools.stringSetChar(str, i + 2, '_');
            }
        }
        return str;
    }
}
