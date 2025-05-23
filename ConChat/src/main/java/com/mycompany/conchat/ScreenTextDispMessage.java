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
public class ScreenTextDispMessage
{
    public int tokens;
    public String model;
    public StringUTF message;
    public boolean isAnswer;
    public boolean ommit;
    
    public int measuredLength0;
    public int measuredLength1;
    

    public int unitLength(ConfigFile CF_)
    {
        return unitLength(CF_.ParamGetI("HistoryUnit"));
    }

    public int unitLength(int unit)
    {
        switch (unit)
        {
            case 0:
                {
                    if (measuredLength0 > 0)
                    {
                        return measuredLength0;
                    }
                    int wordCount = 0;
                    boolean wordChar = false;
                    for (int i = 0; i < message.length(); i++)
                    {
                        if (CommonTools.isChar(message.charAt(i), false, true, true, true))
                        {
                            if (!wordChar)
                            {
                                wordCount++;
                                wordChar = true;
                            }
                        }
                        else
                        {
                            if (wordChar)
                            {
                                wordChar = false;
                            }
                        }
                    }
                    measuredLength0 = wordCount;
                    return wordCount;
                }
            case 1:
                {
                    if (measuredLength1 > 0)
                    {
                        return measuredLength1;
                    }
                    int charCount = 0;
                    for (int i = 0; i < message.length(); i++)
                    {
                        if (CommonTools.isChar(message.charAt(i), false, true, true, true))
                        {
                            charCount++;
                        }
                    }
                    measuredLength1 = charCount;
                    return charCount;
                }
            case 2:
                return 1;
            case 3:
                return tokens;
        }
        return 0;
    }
    
    public ScreenTextDispMessage(boolean isAnswer_, StringUTF message_, int tokens_, String model_)
    {
        isAnswer = isAnswer_;
        message = new StringUTF(message_);
        tokens = tokens_;
        model = model_;
        ommit = false;
        measuredLength0 = 0;
        measuredLength1 = 0;
    }
    
    public String debugText()
    {
        StringBuilder sb = new StringBuilder();
        if (isAnswer)
        {
            sb.append("{>");
            sb.append(tokens);
            sb.append(">");
        }
        else
        {
            sb.append("{<");
            sb.append(tokens);
            sb.append("<");
        }
        sb.append(message);
        sb.append("}");
        return sb.toString();
    }
    
    public static ScreenTextDispMessage supplyArrayListToStr(boolean isAnswer_, ArrayList<StringUTF> msgBuf, int tokens_, String model_)
    {
        if (tokens_ < 0)
        {
            return null;
        }
        
        while ((msgBuf.size() > 0) && (msgBuf.get(msgBuf.size() - 1).isSpacesOnly()))
        {
            msgBuf.remove(msgBuf.size() - 1);
        }
        while ((msgBuf.size() > 0) && (msgBuf.get(0).isSpacesOnly()))
        {
            msgBuf.remove(0);
        }
        
        StringUTF sb = new StringUTF();
        for (int i = 0; i < msgBuf.size(); i++)
        {
            if (i > 0)
            {
                sb.append("\n");
            }
            sb.append(msgBuf.get(i));
        }
        if (!isAnswer_) sb = ScreenTextDisp.convMarkdownToPlain(sb);
        if (sb.length() > 0)
        {
            return new ScreenTextDispMessage(isAnswer_, sb, tokens_, model_);
        }
        
        return null;
    }
}
