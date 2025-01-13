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
public class ScreenTextDispRawItem
{
    enum textTypeDef { normal, code, line, table, hidden };
    
    int lineNumber;
    int MessageIdx;
    textTypeDef textType = textTypeDef.normal;
    String textLine;
    String textLineWrap;
    ArrayList<Integer> cmdIdx;
    ArrayList<Integer> cmdTxt;
    int lineFormat;
    int indent;
    int indentNext;
    int blockId;
    int blockOffset;

    public ScreenTextDispRawItem(int lineNumber_, int indent_)
    {
        textType = textTypeDef.normal;
        lineFormat = 0;
        textLine = "";
        textLineWrap = "";
        lineNumber = lineNumber_;
        cmdIdx = new ArrayList<>();
        cmdTxt = new ArrayList<>();
        indent = indent_;
        indentNext = indent_;
        blockId = 0;
        blockOffset = 0;
        MessageIdx = -1;
    }

    public ScreenTextDispRawItem(ScreenTextDispRawItem item, boolean clone)
    {
        textType = item.textType;
        lineFormat = item.lineFormat;
        textLine = "";
        textLineWrap = "";
        lineNumber = item.lineNumber;
        cmdIdx = new ArrayList<>();
        cmdTxt = new ArrayList<>();
        indent = item.indentNext;
        indentNext = item.indentNext;
        blockId = item.blockId;
        blockOffset = item.blockOffset;
        MessageIdx = item.MessageIdx;
        if (clone)
        {
            textLine = item.textLine;
            textLineWrap = item.textLineWrap;
            for (int i = 0; i < item.cmdIdx.size(); i++)
            {
                cmdIdx.add(item.cmdIdx.get(i));
                cmdTxt.add(item.cmdTxt.get(i));
            }
        }
    }

    public void cmdTrim()
    {
        for (int i = 0; i < cmdIdx.size(); i++)
        {
            if (cmdIdx.get(i) > textLine.length())
            {
                cmdIdx.set(i, textLine.length());
            }
        }
    }
    
    public void append(char chr)
    {
        textLine = textLine + chr;
    }

    public void insert(int idx, String str, boolean insPad)
    {
        if (idx == 0)
        {
            textLine = str + textLine;
            for (int i = 0; i < cmdIdx.size(); i++)
            {
                if (insPad)
                {
                    if (cmdIdx.get(i) > idx)
                    {
                        cmdIdx.set(i, cmdIdx.get(i) + str.length());
                    }
                }
                else
                {
                    if (cmdIdx.get(i) >= idx)
                    {
                        cmdIdx.set(i, cmdIdx.get(i) + str.length());
                    }
                }
            }
        }
        else
        {
            if (idx == textLine.length())
            {
                textLine = textLine + str;
            }
            else
            {
                textLine = textLine.substring(0, idx) + str + textLine.substring(idx);
                for (int i = 0; i < cmdIdx.size(); i++)
                {
                    if (insPad)
                    {
                        if (cmdIdx.get(i) > idx)
                        {
                            cmdIdx.set(i, cmdIdx.get(i) + str.length());
                        }
                    }
                    else
                    {
                        if (cmdIdx.get(i) >= idx)
                        {
                            cmdIdx.set(i, cmdIdx.get(i) + str.length());
                        }
                    }
                }
            }
        }
    }
    
    public void remove(int idx, int n)
    {
        if (n < 0)
        {
            remove(idx, textLine.length() - idx);
            return;
        }
        
        textLine = CommonTools.stringRemove(textLine, idx, n);
        for (int i = 0; i < cmdIdx.size(); i++)
        {
            if (cmdIdx.get(i) > (idx + n))
            {
                cmdIdx.set(i, cmdIdx.get(i) - n);
            }
            else
            {
                if (cmdIdx.get(i) > (idx))
                {
                    cmdIdx.set(i, idx);
                }
            }
        }
    }
    
    public void trim(boolean l, boolean r)
    {
        if (l)
        {
            int i = 0;
            while ((textLine.length() > i) && (textLine.charAt(i) == ' '))
            {
                i++;
            }
            if (i > 0)
            {
                remove(0, i);
            }
        }
        if (r)
        {
            int i = textLine.length() - 1;
            while ((0 <= i) && (textLine.charAt(i) == ' '))
            {
                i--;
            }
            if (i < textLine.length())
            {
                remove(i + 1, -1);
            }
        }
    }

    public int length()
    {
        return textLine.length();
    }

    public void setCommand(int idx, int txt)
    {
        if (idx < 0)
        {
            idx = textLine.length() + idx + 1;
        }
        cmdIdx.add(idx);
        cmdTxt.add(txt);
    }
    
    public void unWrap(ScreenTextDispRawItem src)
    {
        int cmdOffset = textLine.length() + textLineWrap.length();
        textLine = textLine + textLineWrap + src.textLine;
        src.cmdTrim();
        for (int i = 0; i < src.cmdIdx.size(); i++)
        {
            cmdIdx.add(src.cmdIdx.get(i) + cmdOffset);
            cmdTxt.add(src.cmdTxt.get(i));
        }
        
        src.textLine = "";
        src.cmdIdx.clear();
        src.cmdTxt.clear();
    }

    public void moveSuffix(ScreenTextDispRawItem src, int split)
    {
        int l = textLine.length();
        textLine = textLine + src.textLine.substring(split);
        src.textLine = src.textLine.substring(0, split);
        boolean wrap1 = false;
        boolean wrap2 = false;
        boolean wrap3 = false;
        for (int i = 0; i < src.cmdIdx.size(); i++)
        {
            if (src.cmdIdx.get(i) >= split)
            {
                cmdIdx.add(src.cmdIdx.get(i) - split + l);
                cmdTxt.add(src.cmdTxt.get(i));

                src.cmdIdx.remove(i);
                src.cmdTxt.remove(i);
                i--;
            }
            else
            {
                switch (src.cmdTxt.get(i))
                {
                    case 11: wrap1 = true; break;
                    case 10: wrap1 = false; break;
                    case 21: wrap2 = true; break;
                    case 20: wrap2 = false; break;
                    case 31: wrap3 = true; break;
                    case 30: wrap3 = false; break;
                }
            }
        }
        if (wrap1) { cmdIdx.add(0, l); cmdTxt.add(0, 11); src.cmdIdx.add(src.textLine.length()); src.cmdTxt.add(10); }
        if (wrap2) { cmdIdx.add(0, l); cmdTxt.add(0, 21); src.cmdIdx.add(src.textLine.length()); src.cmdTxt.add(20); }
        if (wrap3) { cmdIdx.add(0, l); cmdTxt.add(0, 31); src.cmdIdx.add(src.textLine.length()); src.cmdTxt.add(30); }
    }

    public int getCommand(int idx)
    {
        for (int i = 0; i < cmdIdx.size(); i++)
        {
            if (cmdIdx.get(i) == idx) return cmdTxt.get(i);
        }
        return 0;
    }
}
