/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conchat;

import java.util.ArrayList;
import java.util.Locale;

/**
 *
 * @author xxx
 */
public class ScreenTextDisp
{
    private ConfigFile CF;
    
    private final boolean debugClearScreen = false;
    private final boolean debugLineInfo = false;
    private boolean blockUnderline = true;
    int textWidth = 80;
    int textMessageWidth = 70;
    int textHeight = 23;
    int textOffsetLine = 11;
    int tableCellWidth = 0;
    private final ConsoleInputOutput ConsoleInputOutput_;
    String fileName = "";
    
    public boolean waitingReload = false;
    public int waitingReloadPosition = 0;
    public String waitingReloadFileName = "";
    
    
    boolean parseMarkdown = true;
    
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
    private boolean MarkdownAlignRight;
    
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
            if (!textRaw.get(idx1).textLine.startsWith('|'))
            {
                textRaw.get(idx1).textLine.prepend('|');
            }
            if (!textRaw.get(idx1).textLine.endsWith('|'))
            {
                textRaw.get(idx1).textLine.append('|');
            }
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
                int lastPos = textRaw.get(i).textLine.indexOf('|', 0);
                if (lastPos >= 0)
                {
                    tableOffset = Math.max(tableOffset, lastPos);
                    while (lastPos >= 0)
                    {
                        columnCount++;
                        lastPos = textRaw.get(i).textLine.indexOf('|', lastPos + 1);
                    }
                    tableColumns = Math.max(tableColumns, columnCount);
                }
            }
            
            int tableCellWidth_ = ((textMessageWidth - 1 - tableOffset) / (tableColumns - 1)) - 1;
            if (tableCellWidth_ < tableCellWidth)
            {
                tableCellWidth_ = tableCellWidth;
            }
            
            
            ArrayList<Integer> cellSeparator = new ArrayList<>();
            ArrayList<ScreenTextDisp> cellContent = new ArrayList<>();
            for (int i = idx1; i <= idx2; i++)
            {
                cellSeparator.clear();
                cellContent.clear();
                int lastPos = textRaw.get(i).textLine.indexOf('|', 0);
                while (lastPos >= 0)
                {
                    cellSeparator.add(lastPos);
                    lastPos = textRaw.get(i).textLine.indexOf('|', lastPos + 1);
                }
                for (int ii = 1; ii < cellSeparator.size(); ii++)
                {
                    ScreenTextDisp cellItem = new ScreenTextDisp(ConsoleInputOutput_, CF);
                    ScreenTextDispRawItem cellItem_ = new ScreenTextDispRawItem(textRaw.get(i), true);
                    cellItem_.remove(cellSeparator.get(ii), -1);
                    cellItem_.remove(0, cellSeparator.get(ii - 1) + 1);
                    cellItem.textRaw.add(cellItem_);
                    cellItem.supplyFinish();
                    cellContent.add(cellItem);
                }

                int cellMaxLines = 1;
                for (int ii = 0; ii < cellContent.size(); ii++)
                {
                    cellContent.get(ii).textRaw.get(0).textType = ScreenTextDispRawItem.textTypeDef.normal;
                    cellContent.get(ii).textWidth = tableCellWidth_;
                    cellContent.get(ii).textMessageWidth = tableCellWidth_;
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

            tableHeader.textLine.clear().append(CommonTools.table1);
            StringUTF tableMiddle_textLine = (new StringUTF()).append(CommonTools.tableL);
            tableFooter.textLine.clear().append(CommonTools.table3);

            int[] charPos = new int[idx2 - idx1 + 1];
            int[] charLastPos = new int[idx2 - idx1 + 1];
            int[] columnSize = new int[idx2 - idx1 + 1];
            while (true)
            {
                int columnSizeMax = 0;
                int posMin = 0;
                int posMax = -1;
                for (int i = idx1; i <= idx2; i++)
                {
                    textRawItemMeasureChars(i, true);
                    charPos[i - idx1] = textRaw.get(i).textLine.indexOf('|', charLastPos[i - idx1]);
                    posMin = Math.min(posMin, charPos[i - idx1]);
                    posMax = Math.max(posMax, charPos[i - idx1]);

                    columnSize[i - idx1] = 0;
                    for (int ii = charLastPos[i - idx1]; ii < charPos[i - idx1]; ii++)
                    {
                        columnSize[i - idx1] += textRaw.get(i).textLineCharSize[ii];
                    }
                    columnSizeMax = Math.max(columnSizeMax, columnSize[i - idx1]);
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
                    if (charLastPos[0] > 0)
                    {
                        tableHeader.textLine.append(CommonTools.tableH, columnSizeMax).append(CommonTools.tableT);
                        tableMiddle_textLine.append(CommonTools.tableH, columnSizeMax).append(CommonTools.tableC);
                        tableFooter.textLine.append(CommonTools.tableH, columnSizeMax).append(CommonTools.tableB);
                    }
                    else
                    {
                        tableHeader.textLine.append(' ', columnSizeMax);
                        tableMiddle_textLine.append(' ', columnSizeMax);
                        tableFooter.textLine.append(' ', columnSizeMax);
                    }

                    int padSize = 0;
                    for (int i = idx1; i <= idx2; i++)
                    {
                        charLastPos[i - idx1] = charPos[i - idx1] + 1;
                        if (columnSize[i - idx1] < columnSizeMax)
                        {
                            padSize = columnSizeMax - columnSize[i - idx1];
                            textRaw.get(i).insert(charPos[i - idx1], StringUTF.indent(padSize, ' '), true);
                            charLastPos[i - idx1] += padSize;
                        }
                    }
                }
            }

            for (int i = idx1; i <= idx2; i++)
            {
                textRawItemMeasureChars(i, true);
                int chrIdx = textRaw.get(i).textLine.indexOf('|', 0);
                while (chrIdx >= 0)
                {
                    textRaw.get(i).textLine.setChar(chrIdx, CommonTools.tableV);
                    chrIdx = textRaw.get(i).textLine.indexOf('|', chrIdx + 1);
                }
            }


            if (true)
            {
                tableHeader.textLine.setChar(charLastPos[0] - 1, CommonTools.table2);
                tableMiddle_textLine.setChar(charLastPos[0] - 1, CommonTools.tableR);
                tableFooter.textLine.setChar(charLastPos[0] - 1, CommonTools.table4);

                textRaw.get(idx1 + cellHeaderSize).textLine = tableMiddle_textLine;
                textRaw.get(idx1 + 1).cmdIdx.clear();
                textRaw.get(idx1 + 1).cmdTxt.clear();

                textRaw.add(idx2 + 1, tableFooter);
                textRaw.add(idx1, tableHeader);
                idx += 2;
            }
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
        item.alignRight = MarkdownAlignRight;
        try
        {
            if (parseMarkdown)
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
                        int nextSpace = item.textLine.indexOf(' ', noSpaceIdx);
                        if (nextSpace > 0)
                        {
                            int itemChr = item.textLine.charAt(nextSpace - 1);
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
                                    int digitChr = item.textLine.charAt(ii);
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


                StringUTF windowingText = new StringUTF(item.textLine.clone().substring(textLineBegin));
                windowingText.prepend(' ').prepend(' ').prepend(' ').prepend(' ').prepend(' ');
                windowingText.append(' ').append(' ').append(' ').append(' ').append(' ');
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
                        if (notInBlock && (((MarkdownWindow[2] != '_') && (MarkdownWindow[3] == '_') && (MarkdownWindow[4] != '_')) || ((MarkdownWindow[2] != '*') && (MarkdownWindow[3] == '*') && (MarkdownWindow[4] != '*'))))
                        {
                            boolean isAllowed = false;
                            if (MarkdownItalic)
                            {
                                if ((CommonTools.isChar(MarkdownWindow[2], false, true, true, true)) && (CommonTools.isChar(MarkdownWindow[4], true, false, false, true)))
                                {
                                    isAllowed = true;
                                }
                                else
                                {
                                    if ((MarkdownWindow[3] == '*') && (CommonTools.isChar(MarkdownWindow[2], false, true, true, false)) && (CommonTools.isChar(MarkdownWindow[4], false, true, true, false)))
                                    {
                                        isAllowed = true;
                                    }
                                }

                                if (isAllowed)
                                {
                                    MarkdownItalic = !MarkdownItalic;
                                    item.remove(idx0 - 2, 1);
                                    item.setCommand(idx0 - 2, MarkdownItalic ? 21 : 20);
                                    removedChars++;
                                }
                            }
                            else
                            {
                                if ((CommonTools.isChar(MarkdownWindow[2], true, false, false, true)) && (CommonTools.isChar(MarkdownWindow[4], false, true, true, true)))
                                {
                                    isAllowed = true;
                                }
                                else
                                {
                                    if ((MarkdownWindow[3] == '*') && (CommonTools.isChar(MarkdownWindow[2], false, true, true, false)) && (CommonTools.isChar(MarkdownWindow[4], false, true, true, false)))
                                    {
                                        isAllowed = true;
                                    }
                                }

                                if (isAllowed)
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
                        if (notInBlock && (((MarkdownWindow[1] != '_') && (MarkdownWindow[2] == '_') && (MarkdownWindow[3] == '_') && (MarkdownWindow[4] != '_')) || ((MarkdownWindow[1] != '*') && (MarkdownWindow[2] == '*') && (MarkdownWindow[3] == '*') && (MarkdownWindow[4] != '*'))))
                        {
                            boolean isAllowed = false;
                            if (MarkdownBold)
                            {
                                if ((CommonTools.isChar(MarkdownWindow[1], false, true, true, true)) && (CommonTools.isChar(MarkdownWindow[4], true, false, false, true)))
                                {
                                    isAllowed = true;
                                }
                                else
                                {
                                    if ((MarkdownWindow[3] == '*') && (CommonTools.isChar(MarkdownWindow[1], false, true, true, false)) && (CommonTools.isChar(MarkdownWindow[4], false, true, true, false)))
                                    {
                                        isAllowed = true;
                                    }
                                }

                                if (isAllowed)
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
                                if ((CommonTools.isChar(MarkdownWindow[1], true, false, false, true)) && (CommonTools.isChar(MarkdownWindow[4], false, true, true, true)))
                                {
                                    isAllowed = true;
                                }
                                else
                                {
                                    if ((MarkdownWindow[3] == '*') && (CommonTools.isChar(MarkdownWindow[1], false, true, true, false)) && (CommonTools.isChar(MarkdownWindow[4], false, true, true, false)))
                                    {
                                        isAllowed = true;
                                    }
                                }

                                if (isAllowed)
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
                        if (notInBlock && (((MarkdownWindow[0] != '*') && (MarkdownWindow[1] == '*') && (MarkdownWindow[2] == '*') && (MarkdownWindow[3] == '*') && (MarkdownWindow[4] != '*')) || ((MarkdownWindow[0] != '_') && (MarkdownWindow[1] == '_') && (MarkdownWindow[2] == '_') && (MarkdownWindow[3] == '_') && (MarkdownWindow[4] != '_'))))
                        {
                            boolean isAllowed = false;
                            if (MarkdownBold && MarkdownItalic)
                            {
                                if ((CommonTools.isChar(MarkdownWindow[0], false, true, true, true)) && (CommonTools.isChar(MarkdownWindow[4], true, false, false, true)))
                                {
                                    isAllowed = true;
                                }
                                else
                                {
                                    if ((MarkdownWindow[3] == '*') && (CommonTools.isChar(MarkdownWindow[0], false, true, true, false)) && (CommonTools.isChar(MarkdownWindow[4], false, true, true, false)))
                                    {
                                        isAllowed = true;
                                    }
                                }

                                if (isAllowed)
                                {
                                    MarkdownBold = !MarkdownBold;
                                    MarkdownItalic = !MarkdownItalic;
                                    item.remove(idx0 - 4, 3);
                                    item.setCommand(idx0 - 4, MarkdownBold ? 11 : 10);
                                    item.setCommand(idx0 - 4, MarkdownItalic ? 21 : 20);
                                    removedChars += 3;
                                }
                            }
                            else if ((!MarkdownBold) && (!MarkdownItalic))
                            {
                                if ((CommonTools.isChar(MarkdownWindow[0], true, false, false, true)) && (CommonTools.isChar(MarkdownWindow[4], false, true, true, true)))
                                {
                                    isAllowed = true;
                                }
                                else
                                {
                                    if ((MarkdownWindow[3] == '*') && (CommonTools.isChar(MarkdownWindow[0], false, true, true, false)) && (CommonTools.isChar(MarkdownWindow[4], false, true, true, false)))
                                    {
                                        isAllowed = true;
                                    }
                                }

                                if (isAllowed)
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
                            if (CommonTools.isChar(escapedChar, false, false, false, true))
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
                                        windowingText.setChar(i + 4, 'X');
                                    }
                                }
                                else
                                {
                                    windowingText.setChar(i + 3, '$');
                                    windowingText.setChar(i + 4, '$');
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

                    // Technical data hidden as horizontal line
                    if (windowingText.clone().trim().startsWith("___") && windowingText.clone().trim().endsWith("___"))
                    {
                        if (windowingText.contains("<<<") || windowingText.contains(">>>"))
                        {
                            if (windowingText.contains("<<<"))
                            {
                                item.alignRight = true;
                                MarkdownAlignRight = true;
                            }
                            else
                            {
                                item.alignRight = false;
                                MarkdownAlignRight = false;
                            }
                            item.MessageIdx = -1;
                            textRawItemMarkdownParseReset(false, true);
                            item.textType = ScreenTextDispRawItem.textTypeDef.message;
                        }
                        if (windowingText.contains("(((") || windowingText.contains(")))"))
                        {
                            item.MessageIdx = -1;
                            item.textType = ScreenTextDispRawItem.textTypeDef.hidden;
                        }
                    }
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
                            item.insert(0, StringUTF.indent(item.indent, ' '), false);
                            //item.textLine = stringIndent(item.indent) + item.textLine;
                        }

                        //item.textLine = "|" + item.indent + "|" + item.indentNext + "|" + MarkdownAfterListItem + "|" + item.textLine;
                    }

                    windowingText.trim();

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
                        int idx0 = item.textLine.indexOf("# ", 0) - headerType + 1;
                        item.remove(idx0, headerType + 1);
                        if (item.textLine.clone().trim().endsWith('#') && ((item.textLine.clone().trim().indexOf(" ", 0) >= 0)))
                        {
                            int idxHash = item.textLine.length() - 1;
                            while (item.textLine.charAt(idxHash) != '#')
                            {
                                idxHash--;
                            }
                            while (item.textLine.charAt(idxHash) == '#')
                            {
                                idxHash--;
                            }
                            if ((item.textLine.charAt(idxHash) == ' ') && (idxHash < (item.textLine.length() - 1)))
                            {
                                item.remove(idxHash, -1);
                            }
                        }
                        if (headerType >= 1)
                        {
                            if (headerType < CF.ParamGetI("MarkdownHeader"))
                            {
                                //textRaw.add(new ScreenTextDispRawItem(textRaw.get(idx), true));
                                textRaw.get(idx).lineFormat = 2;
                                //textRaw.get(idx + 1).lineFormat = 3;
                                //idx++;
                            }
                            else
                            {
                                textRaw.get(idx).lineFormat = 1;
                            }
                        }
                    }

                    // Horizontal line
                    if ((windowingText.equalsStr("___")) || (windowingText.equalsStr("***")) || (windowingText.equalsStr("---")))
                    {
                        item.textType = ScreenTextDispRawItem.textTypeDef.line;
                    }

                    // Technical data hidden as horizontal line
                    if (windowingText.startsWith("___") && windowingText.endsWith("___"))
                    {
                        item.textType = ScreenTextDispRawItem.textTypeDef.line;
                        if (windowingText.contains("<<<") || windowingText.contains(">>>"))
                        {
                            if (windowingText.contains("<<<"))
                            {
                                item.alignRight = true;
                                MarkdownAlignRight = true;
                            }
                            else
                            {
                                item.alignRight = false;
                                MarkdownAlignRight = false;
                            }
                            item.MessageIdx = -1;
                            textRawItemMarkdownParseReset(false, true);
                            item.textType = ScreenTextDispRawItem.textTypeDef.message;
                        }
                        if (windowingText.contains("(((") || windowingText.contains(")))"))
                        {
                            item.MessageIdx = -1;
                            item.textType = ScreenTextDispRawItem.textTypeDef.hidden;
                        }
                    }

                    // Table
                    if (windowingText.contains("|") && windowingText.contains("---") && (!MarkdownTable))
                    {
                        boolean isTableCandidate = true;
                        for (int ii = 0; ii < windowingText.length(); ii++)
                        {
                            if (CommonTools.isChar(windowingText.charAt(ii), false, true, true, false))
                            {
                                isTableCandidate = false;
                            }
                        }
                        if (isTableCandidate && (idx > 0))
                        {
                            if (textRaw.get(idx - 1).textLine.clone().trim().indexOf('|', 0) >= 0)
                            {
                                String tableLine1 = textRawItemUnWrap(idx - 1, true).trim();
                                String tableLine2 = textRawItemUnWrap(idx, true).trim();

                                if (tableLine1.contains("|"))
                                {
                                    if (tableLine2.contains("|"))
                                    {
                                        int idxRem = textRaw.get(idx).textLine.indexOf('-', 0);
                                        while (idxRem > 0)
                                        {
                                            int idxRemLen = 0;
                                            while (textRaw.get(idx).textLine.charAt(idxRem + idxRemLen) == '-')
                                            {
                                                idxRemLen++;
                                            }
                                            textRaw.get(idx).remove(idxRem, idxRemLen);
                                            idxRem = textRaw.get(idx).textLine.indexOf('-', 0);
                                        }

                                        idxRem = textRaw.get(idx).textLine.indexOf(':', 0);
                                        while (idxRem > 0)
                                        {
                                            int idxRemLen = 0;
                                            while (textRaw.get(idx).textLine.charAt(idxRem + idxRemLen) == ':')
                                            {
                                                idxRemLen++;
                                            }
                                            textRaw.get(idx).remove(idxRem, idxRemLen);
                                            idxRem = textRaw.get(idx).textLine.indexOf(':', 0);
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
                    if (windowingText.contains("|") && (MarkdownTable))
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
            }
            else
            {
                //if (item.textLine.length() > 0)
                {
                    if (item.textLine.startsWith("___") && item.textLine.endsWith("___"))
                    {
                        item.textType = ScreenTextDispRawItem.textTypeDef.line;
                        if (item.textLine.contains("<<<") || item.textLine.contains(">>>"))
                        {
                            if (item.textLine.contains("<<<"))
                            {
                                item.alignRight = true;
                                MarkdownAlignRight = true;
                            }
                            else
                            {
                                item.alignRight = false;
                                MarkdownAlignRight = false;
                            }
                            item.MessageIdx = -1;
                            textRawItemMarkdownParseReset(false, true);
                            item.textType = ScreenTextDispRawItem.textTypeDef.message;
                        }
                        if (item.textLine.contains("(((") || item.textLine.contains(")))"))
                        {
                            item.MessageIdx = -1;
                            item.textType = ScreenTextDispRawItem.textTypeDef.hidden;
                        }
                    }
                    else
                    {
                        item.textType = ScreenTextDispRawItem.textTypeDef.code;
                    }
                }
                /*else
                {
                    item.textType = ScreenTextDispRawItem.textTypeDef.normal;
                }*/
                
                if (item.textType == ScreenTextDispRawItem.textTypeDef.code)
                {
                    item.blockId = blockIdCounter;
                }
                else
                {
                    blockIdCounter++;
                }

/*                String windowingText = "     " + item.textLine.substring(textLineBegin) + "     ";
                int removedChars = 0;
                if (MarkdownQuoteBlock || MarkdownEquationBlock)
                {
                    item.textType = ScreenTextDispRawItem.textTypeDef.code;
                    item.blockId = blockIdCounter;
                }
*/


            }
                
            if (item.textType == ScreenTextDispRawItem.textTypeDef.hidden)
            {
                textRaw.remove(idx);
                idx--;
            }
            
        }
        catch (Exception e)
        {
            item.textLine = new StringUTF("Parse error: " + CommonTools.exceptionToStr(e));
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
                sb.append(textRaw.get(i).textLine.get());
                sb.append(textRaw.get(i).textLineWrap.get());
            }
            sb.append(textRaw.get(idx2).textLine.get());

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
            return textRaw.get(idx1).textLine.get();
        }
    }
    
    void textRawItemMeasureChars(int idx, boolean reset)
    {
        if (reset)
        {
            textRaw.get(idx).textLineCharSize = null;
        }
        if (textRaw.get(idx).textLineCharSize != null)
        {
            return;
        }
        textRaw.get(idx).textLineCharSize = new int[textRaw.get(idx).textLine.length()];
        textRaw.get(idx).textLineLength = textRaw.get(idx).textLine.length();
        textRaw.get(idx).textLineCells = 0;
        if (ConsoleInputOutput_ != null)
        {
            for (int i = 0; i < textRaw.get(idx).textLine.length(); i++)
            {
                textRaw.get(idx).textLineCharSize[i] = ConsoleInputOutput_.charSize(textRaw.get(idx).textLine.charAt(i));
                textRaw.get(idx).textLineCells += textRaw.get(idx).textLineCharSize[i];
            }
        }
        else
        {
            for (int i = 0; i < textRaw.get(idx).textLine.length(); i++)
            {
                textRaw.get(idx).textLineCharSize[i] = 1;
                textRaw.get(idx).textLineCells += 1;
            }
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
            int textWidthWrap = textMessageWidth;
            if (textRaw.get(idx).lineFormat > 0)
            {
                textWidthWrap = textWidthWrap / 2;
            }
            int idx0 = idx;
            int textWidthWrapLine = textWidthWrap;
            for (int i = 0; i < textRaw.get(idx).textLine.length(); i++)
            {
                int charCode = textRaw.get(idx).textLine.charAt(i);
                if (ConsoleInputOutput_ != null)
                {
                    int charSize = ConsoleInputOutput_.charSize(charCode);
                    if (charSize == 0)
                    {
                        if (textRaw.get(idx).lineFormat > 0)
                        {
                            textWidthWrapLine += 2;
                        }
                        else
                        {
                            textWidthWrapLine += 1;
                        }
                    }
                    if (charSize > 1)
                    {
                        if (textRaw.get(idx).lineFormat > 0)
                        {
                            textWidthWrapLine -= ((charSize - 1) * 2);
                        }
                        else
                        {
                            textWidthWrapLine -= ((charSize - 1));
                        }
                    }
                }
                if ((charCode != 32) && (i >= textWidthWrapLine))
                {
                    textWidthWrapLine = textWidthWrap;
                    textRaw.add(new ScreenTextDispRawItem(textRaw.get(idx), false));
                    idx++;
                    for (int ii = 0; ii < textRaw.get(idx).indent; ii++)
                    {
                        textRaw.get(idx).append(' ');
                    }

                    int lastSpace = textRaw.get(idx - 1).textLine.clone().substring(0, i).indexOf(' ', 1 - i);
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
                            textRaw.get(idx - 1).textLineWrap = textRaw.get(idx - 1).textLine.clone().substring(lastSpace0);
                            textRaw.get(idx - 1).textLine.substring(0, lastSpace0);
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
                    textRaw.get(idx).textLineWrap = textRaw.get(idx).textLine.clone().substring(spaceIdx + 1);
                    textRaw.get(idx).textLine.substring(0, spaceIdx + 1);
                }
                else
                {
                    textRaw.get(idx).textLineWrap = textRaw.get(idx).textLine;
                    textRaw.get(idx).textLine.clear();
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
        else
        {
            if (ConsoleInputOutput_ != null)
            {
                for (int i = 0; i < textRaw.get(idx).textLine.length(); i++)
                {
                    ConsoleInputOutput_.charSize(textRaw.get(idx).textLine.charAt(i));
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
    public StringUTF getLastQuestion(boolean idx)
    {
        int t = textRaw.get(displayOffset).MessageIdx;
        if ((t < 1) && (displayOffset > 0))
        {
            t = textRaw.get(displayOffset - 1).MessageIdx;
        }
        if (t >= 0)
        {
            while ((t > 0) && (textMsg.get(t).isAnswer || textMsg.get(t).ommit))
            {
                t--;
            }
            if (textMsg.get(t).isAnswer || textMsg.get(t).ommit)
            {
                return new StringUTF(idx ? "-1" : "");
            }
            return new StringUTF(idx ? ("" + t + "") : textMsg.get(t).message.get());
        }
        return new StringUTF(idx ? "-1" : "");
    }
    
    /**
     * Enable or disable message ommit
     */
    public void ommitSwitch(int idx)
    {
        int t = (idx >= 0) ? idx : textRaw.get(displayOffset).MessageIdx;
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

    public int getMessageLength(int unit)
    {
        int t = textRaw.get(displayOffset).MessageIdx;
        if (t >= 0)
        {
            return textMsg.get(t).unitLength(unit);
        }
        return 0;
    }
    
    /**
     * Get current message info
     * @param engName
     * @return Message info
     */
    public String getMessageInfo(String engName)
    {
        int t = textRaw.get(displayOffset).MessageIdx;
        if (t >= 0)
        {
            if ((!textMsg.get(t).model.isBlank()) && (!textMsg.get(t).model.trim().equals(engName)))
            {
                return textMsg.get(t).model.trim();
            }
        }
        return "";
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
    public boolean displayScrollUp(int n)
    {
        int displayOffset_ = displayOffset;
        if (n > displayOffset)
        {
            n = displayOffset;
        }
        if (n <= (0 - 9))
        {
            displayOffset = 0;
            return (displayOffset_ != displayOffset);
        }
        if (n < 0)
        {
            displayOffset = 0;
            displayAll();
            return (displayOffset_ != displayOffset);
        }

        if (n > 0)
        {
            if (debugClearScreen)
            {
                displayOffset -= n;
                displayAll();
                return (displayOffset_ != displayOffset);
            }
        
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
        return (displayOffset_ != displayOffset);
    }
    
    /**
     * Perform the scroll down while displaying
     * @param n 
     */
    public boolean displayScrollDn(int n)
    {
        int displayOffset_ = displayOffset;
        if (n <= (0 - 9))
        {
            displayOffset = textRaw.size() - 1;
            return (displayOffset_ != displayOffset);
        }
        if (n < 0)
        {
            displayOffset = textRaw.size() - 1;
            displayAll();
            return (displayOffset_ != displayOffset);
        }

        if (n > textRaw.size() - 1 - displayOffset)
        {
            n = textRaw.size() - 1 - displayOffset;
        }
        if (n > 0)
        {
            if (debugClearScreen)
            {
                displayOffset += n;
                displayAll();
                return (displayOffset_ != displayOffset);
            }

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
        return (displayOffset_ != displayOffset);
    }

    public void displaySearchWord(StringUTF word, boolean backward)
    {
        if (word.length() == 0) return;
        if (displayOffset < 0) return;
        if (displayOffset >= textRaw.size()) return;
        
        if ((backward) && (displayOffset > 0))
        {
            int idx = displayOffset - 1;
            while (idx >= 0)
            {
                ScreenTextDispRawItem item_ = textRaw.get(idx);
                if ((item_.textType == ScreenTextDispRawItem.textTypeDef.normal) || (item_.textType == ScreenTextDispRawItem.textTypeDef.code) || (item_.textType == ScreenTextDispRawItem.textTypeDef.table))
                {
                    if (item_.textLine.get().toUpperCase(Locale.ROOT).contains(word.get().toUpperCase(Locale.ROOT)))
                    {
                        displayOffset = idx;
                        displayAll();
                        return;
                    }
                }
                idx--;
            }
        }
        if ((!backward) && (displayOffset < (textRaw.size() - 1)))
        {
            int idx = displayOffset + 1;
            while (idx < textRaw.size())
            {
                ScreenTextDispRawItem item_ = textRaw.get(idx);
                if ((item_.textType == ScreenTextDispRawItem.textTypeDef.normal) || (item_.textType == ScreenTextDispRawItem.textTypeDef.code) || (item_.textType == ScreenTextDispRawItem.textTypeDef.table))
                {
                    if (item_.textLine.get().toUpperCase(Locale.ROOT).contains(word.get().toUpperCase(Locale.ROOT)))
                    {
                        displayOffset = idx;
                        displayAll();
                        return;
                    }
                }
                idx++;
            }
        }
    }
    
    /**
     * Repaint the whole screen
     */
    public void displayAll()
    {
        if (debugClearScreen)
        {
            for (int i = 0; i < textHeight; i++)
            {
                ConsoleInputOutput_.setTextAttrReset();
                ConsoleInputOutput_.setCursorPos(0, i);
                ConsoleInputOutput_.setLineFormat(0);
                for (int ii = 0; ii < textWidth; ii++)
                {
                    ConsoleInputOutput_.printChar(CommonTools.background);
                }
            }
        }
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

        int contextBeginIdx = ChatEngine.contextBeginIdx(textMsg, ConChat.modelTalkListContextLimitModels, CF, true);

        for (int i = i_min; i < i_max; i++)
        {
            ConsoleInputOutput_.setTextAttrReset();
            if (i == (textHeight - 1))
            {
                displayUnderline = false;
            }
            ConsoleInputOutput_.setCursorPos(0, i);
            ConsoleInputOutput_.setLineFormat(0);
            int i_ = i + displayOffset - textOffsetLine;
            if (((i_) >= 0) && ((i_) < textRaw.size()))
            {
                ScreenTextDispRawItem item = textRaw.get(i_);
                textRawItemMeasureChars(i_, false);
                boolean ommit = false;
                int ommitZeroI = 0;
                int textWidth_ = (item.lineFormat == 0) ? textWidth : (textWidth / 2);
                int itemOffset = 0;
                if (item.MessageIdx >= 0)
                {
                    ommit = textMsg.get(item.MessageIdx).ommit;
                    if (!ommit)
                    {
                        if (item.MessageIdx >= contextBeginIdx)
                        {
                            switch (ChatEngine.ctxMatchBulk(textMsg, item.MessageIdx, ConChat.modelTalkListContextLimitModels, CF))
                            {
                                case ommited:
                                case partialMatch:
                                case fullMatch:
                                    ommitZeroI = 0;
                                    break;
                                case notMatch:
                                    ommitZeroI = (item.lineFormat == 0) ? 2 : 1;
                                    break;
                            }
                        }
                        else
                        {
                            ommitZeroI = (item.lineFormat == 0) ? 2 : 1;
                        }
                    }
                }

                // Reduce glitches in some consoles
                if ((item.lineFormat > 0) && (item.textType == ScreenTextDispRawItem.textTypeDef.normal))
                {
                    ConsoleInputOutput_.screenLineClear();
                }

                if (ommit || (ommitZeroI > 0))
                {
                    ConsoleInputOutput_.setTextAttrStrike1();
                }
                if ((item.textType == ScreenTextDispRawItem.textTypeDef.line) || (item.textType == ScreenTextDispRawItem.textTypeDef.message))
                {
                    StringUTF msgInfo = new StringUTF();
                    if (item.textType == ScreenTextDispRawItem.textTypeDef.message)
                    {
                        if (parseMarkdown)
                        {
                            msgInfo = ConChat.modelInfoFromMessage(item.textLine.get(), item.alignRight ? 1 : 0);
                        }
                        else
                        {
                            msgInfo = item.textLine.clone();
                        }
                    }
                    for (int ii = 0; ii < textWidth_; ii++)
                    {
                        if (ommitZeroI > 0)
                        {
                            if (ii == ommitZeroI)
                            {
                                ConsoleInputOutput_.setTextAttrStrike0();
                            }
                            if (ii == (textWidth_ - ommitZeroI))
                            {
                                ConsoleInputOutput_.setTextAttrStrike1();
                            }
                        }
                        if ((item.textType == ScreenTextDispRawItem.textTypeDef.line))
                        {
                            ConsoleInputOutput_.printChar(CommonTools.splitterText);
                        }
                        else
                        {
                            int infoChr = CommonTools.splitterMsg;
                            int infoMargin = 2;
                            if (textWidth_ < 50) infoMargin = 1;
                            if (textWidth_ < 30) infoMargin = 0;
                            int infoLen = msgInfo.length();
                            if ((item.alignRight) && (ii >= textWidth_ - infoLen - infoMargin) && (ii < textWidth_ - infoMargin))
                            {
                                infoChr = msgInfo.charAt(ii - textWidth_ + infoMargin + infoLen);
                            }
                            if ((!item.alignRight) && (ii >= infoMargin) && ((ii - infoMargin) < infoLen))
                            {
                                infoChr = msgInfo.charAt(ii - infoMargin);
                            }
                            ConsoleInputOutput_.printChar(infoChr);
                        }
                    }
                }
                else
                {
                    ConsoleInputOutput_.setLineFormat(item.lineFormat);

                    // Code line highlight begin
                    if (item.textType == ScreenTextDispRawItem.textTypeDef.code)
                    {
                        displayUseCmd(31, i);
                    }

                    
                    itemOffset = item.blockOffset;
                    
                    
                    // Right text alignment
                    if ((item.alignRight) && (item.textType == ScreenTextDispRawItem.textTypeDef.normal))
                    {
                        /*itemOffset = 0 - textWidth_;
                        if (item.lineFormat > 0)
                        {
                            itemOffset += (textWidth_ / 2);
                        }
                        int itemTextL = item.textLineLength;
                        while ((itemTextL > 0) && (itemOffset < 0))
                        {
                            itemTextL--;
                            itemOffset += item.textLineCharSize[itemTextL];
                        }
                        itemOffset += item.blockOffset;*/
                        
                        ScreenTextDispRawItem item_;
                        
                        int idxTemp1 = i_;
                        item_ = textRaw.get(idxTemp1);
                        while ((item_.alignRight) && (item_.MessageIdx == item.MessageIdx) && (item_.textType != ScreenTextDispRawItem.textTypeDef.message) && (idxTemp1 > 0))
                        {
                            idxTemp1--;
                            item_ = textRaw.get(idxTemp1);
                        }
                        int idxTemp2 = i_;
                        item_ = textRaw.get(idxTemp2);
                        while ((item_.alignRight) && (item_.MessageIdx == item.MessageIdx) && (item_.textType != ScreenTextDispRawItem.textTypeDef.message) && (idxTemp2 < (textRaw.size())))
                        {
                            idxTemp2++;
                            item_ = textRaw.get(idxTemp2);
                        }
                        
                        boolean idxHasHeader = false;
                        int textLineWidth = 0;
                        for (int ii = (idxTemp1 + 1); ii < idxTemp2; ii++)
                        {
                            int temp = 0;
                            if (ii != i_)
                            {
                                textRawItemMeasureChars(ii, false);
                            }
                            item_ = textRaw.get(ii);
                            for (int iii = 0; iii < item_.textLineLength; iii++)
                            {
                                temp += item_.textLineCharSize[iii];
                            }
                            if (item_.lineFormat > 0)
                            {
                                idxHasHeader = true;
                                textLineWidth = Math.max(textLineWidth, (temp * 2));
                            }
                            else
                            {
                                textLineWidth = Math.max(textLineWidth, temp);
                            }
                        }
                        
                        itemOffset = 0;
                        if (idxHasHeader)
                        {
                            int t = (textWidth - textLineWidth) / 2;
                            if (textWidth_ < textWidth)
                            {
                                itemOffset = itemOffset - t;
                            }
                            else
                            {
                                itemOffset = itemOffset - t - t;
                            }
                        }
                        else
                        {
                            itemOffset = itemOffset - (textWidth - textLineWidth);
                        }
                    }
                    
                    
                    try
                    {
                        // Offset measure
                        int writeOffsetChar = 0;
                        int writeOffsetSize = 0;
                        int writeOffsetPad1 = 0;
                        int writeOffsetPad2 = 0;
                        int writeMarginPad = 0;
                        if (itemOffset > 0)
                        {
                            while ((writeOffsetSize < (itemOffset + 1)) && (writeOffsetChar < item.textLineLength))
                            {
                                writeOffsetSize += item.textLineCharSize[writeOffsetChar];
                                writeOffsetChar++;
                            }
                            writeOffsetPad1 = writeOffsetSize - (itemOffset + 1);
                            writeOffsetPad2 = writeOffsetSize - (itemOffset + 1);
                        }
                        if (itemOffset < 0)
                        {
                            writeOffsetChar = itemOffset;
                        }

                        boolean writeLeftScroll1 = (itemOffset > 0);
                        boolean writeLeftScroll2 = (itemOffset > 0);
                        int writeRightScrollPos = textWidth_ + 10;


                        // Character counting
                        int virtualCursor = 0;
                        int textLinePointer = writeOffsetChar;
                        int writeCount = 0;
                        while (virtualCursor < textWidth_)
                        {
                            int textCharSize = ((textLinePointer >= 0) && (textLinePointer < item.textLineLength)) ? item.textLineCharSize[textLinePointer] : 1;

                            if (writeLeftScroll1)
                            {
                                textCharSize = 1;
                                writeLeftScroll1 = false;
                            }
                            else
                            {
                                if (writeOffsetPad1 > 0)
                                {
                                    textCharSize = 1;
                                    writeOffsetPad1--;
                                }
                                else
                                {
                                    textLinePointer++;
                                }
                            }
                            virtualCursor += textCharSize;
                            
                            writeCount++;

                            // High Surrogates (D800 - DBFF)
                            // Low Surrogates (DC00 - DFFF)  - 1024
                            if (textLinePointer < item.textLineLength)
                            {
                                int chrx = textLinePointer >= 0 ? ((int)item.textLine.charAt(textLinePointer)) : 32;
                                if ((chrx >= 0xDC00) && (chrx < 0xDFFF))
                                {
                                    writeCount++;
                                }
                            }
                        }

                        if (textLinePointer < item.textLineLength)
                        {
                            writeRightScrollPos = textLinePointer - 1 - writeOffsetChar + writeOffsetPad2;
                            if (writeLeftScroll2)
                            {
                                writeRightScrollPos++;
                            }
                            
                            textLinePointer--;
                            int textCharSize = ((textLinePointer >= 0) && (textLinePointer < item.textLineLength)) ? item.textLineCharSize[textLinePointer] : 1;
                            virtualCursor -= textCharSize;
                            writeMarginPad = textWidth_ - 1 - virtualCursor;
                        }
                        else
                        {
                            if (virtualCursor > textWidth_)
                            {
                                writeRightScrollPos = textLinePointer - writeOffsetChar + writeOffsetPad2;
                                writeMarginPad = virtualCursor - textWidth_ - 1;
                            }
                        }
                        
                        
                        textLinePointer = writeOffsetChar;

                        // Attributes before writing
                        for (int iii = 0; iii < item.cmdIdx.size(); iii++)
                        {
                            if (item.cmdIdx.get(iii) < textLinePointer)
                            {
                                displayUseCmd(item.cmdTxt.get(iii), i);
                            }
                        }
                    
                        // Character writing
                        for (int writeIterator = 0; writeIterator < writeCount; writeIterator++)
                        {
                            if (ommitZeroI > 0)
                            {
                                if ((writeIterator == ommitZeroI) && ((writeCount - writeIterator) > ommitZeroI))
                                {
                                    ConsoleInputOutput_.setTextAttrStrike0();
                                }
                                if ((writeIterator > ommitZeroI) && (writeCount - writeIterator) == ommitZeroI)
                                {
                                    ConsoleInputOutput_.setTextAttrStrike1();
                                }
                            }

                            int textChar = ((textLinePointer >= 0) && (textLinePointer < item.textLineLength)) ? item.textLine.charAt(textLinePointer) : ' ';
                            int useAttributesPtr = textLinePointer;

                            if (writeLeftScroll2)
                            {
                                textChar = CommonTools.scrollL;
                                writeLeftScroll2 = false;
                                useAttributesPtr = -1;
                            }
                            else
                            {
                                if (writeOffsetPad2 > 0)
                                {
                                    textChar = ' ';
                                    writeOffsetPad2--;
                                    useAttributesPtr = -1;
                                }
                                else
                                {
                                    textLinePointer++;
                                }
                            }

                            if (writeIterator >= writeRightScrollPos)
                            {
                                while (writeMarginPad > 0)
                                {
                                    ConsoleInputOutput_.printChar(' ');
                                    writeMarginPad--;
                                }
                                textChar = CommonTools.scrollR;
                            }

                            // Attributes associated with the character
                            if (useAttributesPtr >= 0)
                            for (int iii = 0; iii < item.cmdIdx.size(); iii++)
                            {
                                if (item.cmdIdx.get(iii) == useAttributesPtr)
                                {
                                    displayUseCmd(item.cmdTxt.get(iii), i);
                                }
                            }
                            
                            ConsoleInputOutput_.printChar(textChar);
                        }

                        // Attributes after writing
                        for (int iii = 0; iii < item.cmdIdx.size(); iii++)
                        {
                            if (item.cmdIdx.get(iii) > textLinePointer)
                            {
                                displayUseCmd(item.cmdTxt.get(iii), i);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        ConsoleInputOutput_.printString("Display error: " + e.getMessage());
                        //ConsoleInputOutput_.printString("Display error: " + CommonTools.exceptionToStr(e));
                    }

                    // Code line highlight end
                    if (item.textType == ScreenTextDispRawItem.textTypeDef.code)
                    {
                        displayUseCmd(30, i);
                    }
                }


                if (ommit || (ommitZeroI > 0))
                {
                    ConsoleInputOutput_.setTextAttrStrike0();
                }
                
                if (debugLineInfo)
                {
                    ConsoleInputOutput_.setCursorPos(3, i);
                    ConsoleInputOutput_.printString("[" + item.lineNumber + "]");
                    ConsoleInputOutput_.printString("[" + item.MessageIdx + "]");
                    ConsoleInputOutput_.printString("[" + itemOffset + "]");
                    if (item.alignRight)
                    {
                        ConsoleInputOutput_.printChar('>');
                    }
                    else
                    {
                        ConsoleInputOutput_.printChar('<');
                    }
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
        ArrayList<StringUTF> msgBuf = new ArrayList<>();
        int msgTokens = -1;
        String msgTokensModel = "";
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

                        ScreenTextDispMessage item = ScreenTextDispMessage.supplyArrayListToStr(msgAnswer, msgBuf, msgTokens, msgTokensModel);
                        if (item != null)
                        {
                            item.ommit = lastMsgOmmit;
                            textMsg.add(item);
                            lastMsgOmmit = false;
                            //supplyLine(item.debugText());
                        }

                        msgBuf.clear();
                        msgTokens = CommonTools.strToInt(ConChat.modelInfoFromMessage(S.substring(3, S.length() - 3), 8).get(), -1);
                        msgTokensModel = ConChat.modelInfoFromMessage(S.substring(3, S.length() - 3), 0).get();

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
                    msgBuf.add(new StringUTF(S));
                }
            }

            supplyLine(new StringUTF(S));

        }
        {
            ScreenTextDispMessage item = ScreenTextDispMessage.supplyArrayListToStr(msgAnswer, msgBuf, msgTokens, msgTokensModel);
            if (item != null)
            {
                item.ommit = lastMsgOmmit;
                textMsg.add(item);
                //supplyLine(item.debugText());
            }
        }
    }

    public void supplyFinish()
    {
        if (textRaw.size() < 1)
        {
            return;
        }
        boolean isBold = false;
        boolean isItalic = false;
        boolean isQuote = false;
        ScreenTextDispRawItem item = null;
        for (int i = 0; i < textRaw.size(); i++)
        {
            item = textRaw.get(i);
            for (int ii = 0; ii < item.cmdTxt.size(); ii++)
            {
                switch (item.cmdTxt.get(ii))
                {
                    case 11: isBold = true; break;
                    case 10: isBold = false; break;
                    case 21: isItalic = true; break;
                    case 20: isItalic = false; break;
                    case 31: isQuote = true; break;
                    case 30: isQuote = false; break;
                }
            }
        }
        item = textRaw.get(textRaw.size() - 1);
        if (isBold)
        {
            item.setCommand(-1, 10);
        }
        if (isItalic)
        {
            item.setCommand(-1, 20);
        }
        if (isQuote)
        {
            item.setCommand(-1, 30);
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
        MarkdownAlignRight = false;
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

    public void supply(StringUTF raw)
    {
        if ((fileName.length() > 0) && (!supplyPointTemp))
        {
            CommonTools.fileSaveText(fileName, raw.get());
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
                textRaw.get(t).append(chrN);
            }
        }
    }


    public void supplyLine(StringUTF raw)
    {
        raw.append('\n');
        supply(raw);
        raw.remove(raw.length() - 1, 1);
    }

    public void supply(String raw)
    {
        supply(new StringUTF(raw));
    }

    public void supplyLine(String raw)
    {
        supplyLine(new StringUTF(raw));
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
                    return false;
                }
                
                int maxOffset = textRaw.get(displayOffset).textLineCells;

                int idx1 = displayOffset;
                while ((idx1 >= 0) && (id == textRaw.get(idx1).blockId))
                {
                    maxOffset = Math.max(maxOffset, textRaw.get(idx1).textLineCells);
                    idx1--;
                }
                idx1++;
                int idx2 = displayOffset;
                while ((idx2 < textRaw.size()) && (id == textRaw.get(idx2).blockId))
                {
                    maxOffset = Math.max(maxOffset, textRaw.get(idx2).textLineCells);
                    idx2++;
                }
                idx2--;
                
                maxOffset = maxOffset - textWidth;

                if ((n > 0) && (textRaw.get(displayOffset).blockOffset >= maxOffset))
                {
                    return false;
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

    
    public static StringUTF convSingleToMulti(StringUTF str)
    {
        StringUTF str_ = new StringUTF();
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
        return str_;
    }

    public static StringUTF convMultiToSingle(StringUTF str)
    {
        StringUTF str_ = new StringUTF();
        for (int i = 0; i < str.length(); i++)
        {
            int chr = str.charAt(i);
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
        return str_;
    }
    
    public static StringUTF convPlainToMarkdown(StringUTF str)
    {
        StringUTF sb = new StringUTF();
        for (int i = 0; i < str.length(); i++)
        {
            int chr = str.charAt(i);
            if (CommonTools.isChar(chr, false, false, false, true))
            {
                if ((chr != '[') && (chr != ']'))
                {
                    sb.append("\\");
                }
            }
            sb.append(chr);
        }
        return sb;
    }
    
    private static ScreenTextDisp convMarkdownToPlain_ = null;
    
    public static StringUTF convMarkdownToPlain(StringUTF str)
    {
        if (convMarkdownToPlain_ == null)
        {
            convMarkdownToPlain_ = new ScreenTextDisp(null, null);
            convMarkdownToPlain_.parseMarkdown = true;
        }
        convMarkdownToPlain_.textWidth = str.length() + 10;
        convMarkdownToPlain_.textMessageWidth = str.length() + 10;
        convMarkdownToPlain_.clear(false);
        convMarkdownToPlain_.supplyLine(str);
        StringUTF sb = new StringUTF();
        for (int i = 0; i < (convMarkdownToPlain_.textRaw.size() - 1); i++)
        {
            if (i > 0) sb.append("\n");
            sb.append(convMarkdownToPlain_.textRaw.get(i).textLine.get());
        }
        return sb;
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
