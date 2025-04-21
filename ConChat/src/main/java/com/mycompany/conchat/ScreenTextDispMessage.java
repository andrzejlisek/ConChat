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
    public String message;
    public boolean isAnswer;
    public boolean ommit;
    

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
                    return wordCount;
                }
            case 1:
                {
                    int charCount = 0;
                    for (int i = 0; i < message.length(); i++)
                    {
                        if (CommonTools.isChar(message.charAt(i), false, true, true, true))
                        {
                            charCount++;
                        }
                    }
                    return charCount;
                }
            case 2:
                return 1;
            case 3:
                return tokens;
        }
        return 0;
    }
    
    public ScreenTextDispMessage(boolean isAnswer_, String message_, int tokens_, String model_)
    {
        isAnswer = isAnswer_;
        message = message_;
        tokens = tokens_;
        model = model_;
        ommit = false;
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
    
    public static ScreenTextDispMessage supplyArrayListToStr(boolean isAnswer_, ArrayList<String> msgBuf, int tokens_, String model_)
    {
        if (tokens_ < 0)
        {
            return null;
        }
        
        while ((msgBuf.size() > 0) && (msgBuf.get(msgBuf.size() - 1).trim().isEmpty()))
        {
            msgBuf.remove(msgBuf.size() - 1);
        }
        while ((msgBuf.size() > 0) && (msgBuf.get(0).trim().isEmpty()))
        {
            msgBuf.remove(0);
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < msgBuf.size(); i++)
        {
            if (i > 0)
            {
                sb.append("\n");
            }
            sb.append(msgBuf.get(i));
        }
        String sb_s = sb.toString();
        if (!isAnswer_) sb_s = ScreenTextDisp.convMarkdownToPlain(sb_s);
        if (sb_s.length() > 0)
        {
            return new ScreenTextDispMessage(isAnswer_, sb_s, tokens_, model_);
        }
        
        return null;
    }
}
