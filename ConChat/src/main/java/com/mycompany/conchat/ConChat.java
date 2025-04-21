/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conchat;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 *
 * @author xxx
 */
public class ConChat
{
    static ScreenTextInput ScreenTextInput_;
    static ScreenTextDisp[] ScreenTextDisp_;
    static ConsoleInputOutput ConsoleInputOutput_;
    
    static int inputBoxHeight = 2;
    static int waitSignState = 0;
    static int waitTime = 0;
    static String waitInfo = "";
    static String waitTimeStr = "";
    static Instant waitTimeStart;
    
    static String archFileLast = "";
    
    static void waitIndicate()
    {
        if (waitSignState > 0)
        {
            ConsoleInputOutput_.setCursorPos(0, ConsoleInputOutput_.screenHeight - inputBoxHeight);
            switch (waitSignState)
            {
                case 1: ConsoleInputOutput_.printChar('-'); waitSignState++; break;
                case 2: ConsoleInputOutput_.printChar('\\'); waitSignState++; break;
                case 3: ConsoleInputOutput_.printChar('|'); waitSignState++; break;
                case 4: ConsoleInputOutput_.printChar('/'); waitSignState = 1; break;
            }
            if (!waitInfo.isEmpty())
            {
                ConsoleInputOutput_.printString(waitInfo);
            }
            if (waitTime > 0)
            {
                int waitTimeVal = waitTime - (int)Duration.between(waitTimeStart, Instant.now()).toSeconds();
                if (waitTimeVal > 0)
                {
                    int waitTimePad = waitTimeStr.length();
                    waitTimeStr = " " + waitTimeVal;
                    waitTimePad = waitTimePad - waitTimeStr.length();
                    ConsoleInputOutput_.printString(waitTimeStr);
                    if (waitTimePad > 0)
                    {
                        ConsoleInputOutput_.printString(CommonTools.stringIndent(waitTimePad, ' '));
                    }
                }
                else
                {
                    if (waitTimeStr.length() > 0)
                    {
                        ConsoleInputOutput_.printString(CommonTools.stringIndent(waitTimeStr.length(), ' '));
                        waitTimeStr = "";
                    }
                }
            }
            ConsoleInputOutput_.printFlush();
        }
    }
    
    static void waitStart(int waitTime_, String waitInfo_)
    {
        ScreenTextInput_.reset();
        ConsoleInputOutput_.setTextAttrBold1();
        ConsoleInputOutput_.setTextAttrReverse1();

        waitTimeStr = "";
        waitTime = waitTime_;
        waitInfo = waitInfo_;
        waitTimeStart = Instant.now();
        waitSignState = 1;
        waitIndicate();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                waitIndicate();
            }
        }, 250, 250);
    }

    static void waitStopX()
    {
        waitSignState = 0;
        timer.cancel();
        ConsoleInputOutput_.setCursorPos(0, ConsoleInputOutput_.screenHeight - inputBoxHeight);
        ConsoleInputOutput_.printChar(' ');
        if (waitTimeStr.length() > 0)
        {
            ConsoleInputOutput_.printString(CommonTools.stringIndent(waitTimeStr.length(), ' '));
            waitTimeStr = "";
        }
        ConsoleInputOutput_.setCursorPos(0, ConsoleInputOutput_.screenHeight - inputBoxHeight);
        waitTime = 0;
    }
    
    static void waitStop()
    {
        waitSignState = 0;
        timer.cancel();
        ConsoleInputOutput_.setTextAttrReverse0();
        ConsoleInputOutput_.setTextAttrBold0();
        waitTime = 0;
    }
    
    public static String modelTalkListContextLimitModels = "";
    
    static boolean modelTalkListUpdate()
    {
        modelTalkList.clear();
        String modelTalkSeq = CF.ParamGetS("Model");
        boolean nums = true;
        for (int i = 0; i < modelTalkSeq.length(); i++)
        {
            if (!CommonTools.isChar(modelTalkSeq.charAt(i), false, false, true, false))
            {
                nums = false;
            }
        }
        if (nums)
        {
            int favIdx = 1;
            String[] fav = (";" + CF.ParamGetS("Favorite") + ";").split(";");
            modelTalkListContextLimitModels = "";

            for (int i = 0; i < fav.length; i++)
            {
                if (fav[i].length() > 0)
                {
                    int favPos = EngineName.indexOf(fav[i]);
                    if (favPos >= 0)
                    {
                        //String engineNameItem = EngineName.get(favPos);
                        //if (ChatEngine.isValidEngine(engineNameItem))
                        //{
                        //}
                        EngineFav.set(favPos, favIdx);
                        favIdx++;
                    }
                }
            }
            for (int i = 0; i < modelTalkSeq.length(); i++)
            {
                int N = modelTalkSeq.charAt(i) - 48;
                if (N == 0) { N = 10; }
                int Nidx = EngineFav.indexOf(N);
                if (Nidx >= 0)
                {
                    if (!modelTalkListContextLimitModels.isEmpty())
                    {
                        modelTalkListContextLimitModels = modelTalkListContextLimitModels + CommonTools.splitterInfoS;
                    }
                    modelTalkListContextLimitModels = modelTalkListContextLimitModels + EngineName.get(Nidx);
                    modelTalkList.add(EngineName.get(Nidx));
                }
            }
            return true;
        }
        else
        {
            modelTalkList.add(modelTalkSeq);
            modelTalkListContextLimitModels = "";
            return false;
        }
    }
    
    static String modelInfoFavName(String modelName_, boolean useZero)
    {
        int i = CommonTools.strToInt(modelName_, -1);
        if (useZero)
        {
            if (i == 0)
            {
                i = 10;
            }
            if (i > 10)
            {
                return "";
            }
        }
        if (i > 0)
        {
            int t = EngineFav.indexOf(i);
            if ((t >= 0) && (t < EngineName.size()))
            {
                return EngineName.get(t);
            }
        }
        return "";
    }
    
    static String modelInfoFavNumber(String modelName_, boolean useZero)
    {
        int t = EngineName.indexOf(modelName_);
        if ((t >= 0) && (t < EngineFav.size()))
        {
            if (useZero)
            {
                if (EngineFav.get(t) < 10)
                {
                    return "" + EngineFav.get(t) + "";
                }
                if (EngineFav.get(t) == 10)
                {
                    return "0";
                }
                return "";
            }
            else
            {
                return "" + EngineFav.get(t) + "";
            }
        }
        return "";
    }
    
    static String modelInfoFromMessage(String rawInfo, int infoType)
    {
        String infoArray[];
        if (infoType < 10)
        {
            infoArray = rawInfo.substring(3, rawInfo.length() - 3).split(CommonTools.splitterInfo + "");
        }
        else
        {
            infoArray = rawInfo.split(",");
        }
        StringBuilder sb = new StringBuilder();
        switch (infoType)
        {
            case 0: // Answer
                for (int i = 1; i < infoArray.length; i += 2)
                {
                    if (i > 1)
                    {
                        sb.append(",");
                    }
                    sb.append(infoArray[i]);
                }
                break;
            case 1: // User
                sb.append("user");
                break;
            case 2: // Info
                if (infoArray.length > 2)
                {
                    for (int i = 1; i < infoArray.length; i += 2)
                    {
                        String s = modelInfoFavNumber(infoArray[i], true);
                        if (s.isEmpty())
                        {
                            sb.append("[" + infoArray[i] + "]");
                        }
                        else
                        {
                            sb.append(s);
                        }
                    }
                }
                else
                {
                    if (infoArray.length == 2)
                    {
                        sb.append(infoArray[1]);
                    }
                    else
                    {
                        sb.append("?");
                    }
                }
                break;
            case 8: // Tokens
                {
                    int t = 0;
                    for (int i = 0; i < infoArray.length; i += 2)
                    {
                        t = t + CommonTools.strToInt(infoArray[i], 0);
                    }
                    sb.append(t);
                }
                break;
            case 9: // Raw text
                sb.append(rawInfo);
                break;
            case 10: // Model names to numbers
                if (infoArray.length > 1)
                {
                    for (int i = 0; i < infoArray.length; i += 1)
                    {
                        String s = modelInfoFavNumber(infoArray[i], true);
                        if (s.isEmpty())
                        {
                            sb.append("[" + infoArray[i] + "]");
                        }
                        else
                        {
                            sb.append(s);
                        }
                    }
                }
                else
                {
                    if (infoArray.length == 1)
                    {
                        sb.append(infoArray[0]);
                    }
                    else
                    {
                        sb.append("?");
                    }
                }
                break;
            case 11: // Model numbers to names
                {
                    if (CommonTools.strOnlyDigits(rawInfo))
                    {
                        for (int i = 0; i < rawInfo.length(); i++)
                        {
                            if (i > 0)
                            {
                                sb.append(",");
                            }
                            sb.append(modelInfoFavName(rawInfo.substring(i, i + 1), true));
                        }
                    }
                    else
                    {
                        sb.append(rawInfo);
                    }
                }
                break;
        }
        return sb.toString();
    }
    
    
    
    static ConfigFile CF;
    static ConfigFile CFC;

    
    static ArrayList<String> EngineName;
    static ArrayList<Integer> EngineFav;
    
    static ArrayList<String> archFileName;
    static ArrayList<String> archFileId;
    static ArrayList<Integer> archFileNumber;
    
    static ArrayList<String> modelTalkList;

    static int getTokenCounterSize()
    {
        switch (CF.ParamGetI("Counter"))
        {
            default:
                return 10;
            case 1:
                return 10;
            case 2:
                return 6;
        }
    }
    
    static String getTokenCounter(String engineName_, int infoType)
    {
        switch (CF.ParamGetI("Counter"))
        {
            default:
                {
                    switch (infoType)
                    {
                        case 0:
                            return "2";
                        case 1:
                            return String.valueOf(CFC.ParamGetI(engineName_ + "-i", 0));
                        case 2:
                            return String.valueOf(CFC.ParamGetI(engineName_ + "-o", 0));
                        case 3:
                            return "0";
                    }
                }
                break;
            case 1:
                {
                    int tokenPrice = 0;
                    switch (infoType)
                    {
                        case 0:
                            return "2";
                        case 1:
                            tokenPrice = CFC.ParamGetI(engineName_ + "-ii", 0);
                            break;
                        case 2:
                            tokenPrice = CFC.ParamGetI(engineName_ + "-oo", 0);
                            break;
                        case 3:
                            return "0";
                    }
                    if (tokenPrice > 0)
                    {
                        return CommonTools.intToDec(tokenPrice, 0, 4);
                    }
                    else
                    {
                        return CommonTools.intToDec(-1, 0, 4);
                    }
                }
            case 2:
                {
                    long tokenCost = 0;
                    long tokenAmount1 = CFC.ParamGetI(engineName_ + "-i", 0);
                    long tokenPrice1 = CFC.ParamGetI(engineName_ + "-ii", 0);
                    long tokenAmount2 = CFC.ParamGetI(engineName_ + "-o", 0);
                    long tokenPrice2 = CFC.ParamGetI(engineName_ + "-oo", 0);
                    switch (infoType)
                    {
                        case 0:
                            return "3";
                        case 1:
                            tokenCost = (tokenAmount1 * tokenPrice1);
                            if (tokenPrice1 <= 0) tokenCost = -1;
                            break;
                        case 2:
                            tokenCost = (tokenAmount2 * tokenPrice2);
                            if (tokenPrice2 <= 0) tokenCost = -1;
                            break;
                        case 3:
                            tokenCost = (tokenAmount1 * tokenPrice1) + (tokenAmount2 * tokenPrice2);
                            if (tokenPrice1 <= 0) tokenCost = -1;
                            if (tokenPrice2 <= 0) tokenCost = -1;
                            break;
                    }
                    if (tokenCost >= 0)
                    {
                        return CommonTools.intToDec(tokenCost, 8, 2);
                    }
                    else
                    {
                        return CommonTools.intToDec(-1, 0, 2);
                    }
                }
        }
        return "";
    }
    
    static void refreshSettingText()
    {
        String fav = ";" + CF.ParamGetS("Favorite") + ";";


        int ctxSummaryMsg = 0;
        int ctxSummaryWrd = 0;
        int ctxSummaryChr = 0;
        int ctxSummaryMsgUsed = 0;
        int ctxSummaryWrdUsed = 0;
        int ctxSummaryChrUsed = 0;
        ArrayList<ScreenTextDispMessage> ctxMsg = ScreenTextDisp_[workContext].textMsg;
        int contextBeginIdx = ChatEngine.contextBeginIdx(ctxMsg, modelTalkListContextLimitModels, CF, true);
        for (int i = 0; i < ctxMsg.size(); i++)
        {
            if ((!ctxMsg.get(i).ommit) && (ctxMsg.get(i).unitLength(CF) > 0))
            {
                if (contextBeginIdx <= i)
                {
                    ctxSummaryMsgUsed++;
                    ctxSummaryWrdUsed += ctxMsg.get(i).unitLength(0);
                    ctxSummaryChrUsed += ctxMsg.get(i).unitLength(1);
                }
            }
            ctxSummaryMsg++;
            ctxSummaryWrd += ctxMsg.get(i).unitLength(0);
            ctxSummaryChr += ctxMsg.get(i).unitLength(1);
        }

        modelTalkListUpdate();
        String engineName = CF.ParamGetS("Model");

        int msgLength0 = ScreenTextDisp_[workContext].getMessageLength(0);
        int msgLength1 = ScreenTextDisp_[workContext].getMessageLength(1);
        int msgLength2 = ScreenTextDisp_[workContext].getMessageLength(2);
        int msgLength3 = ScreenTextDisp_[workContext].getMessageLength(3);

        
        ScreenTextDisp_[10].clear(true);
        ScreenTextDisp_[10].supplyLine("# " + workContext + " " + engineName);
        if (!CF.ParamGetS("Hint" + workContext).isBlank())
        {
            ScreenTextDisp_[10].supplyLine(CF.ParamGetS("Hint" + workContext));
        }
        ScreenTextDisp_[10].supplyLine("");
        switch (CF.ParamGetI("HistoryUnit"))
        {
            case 0:
                ScreenTextDisp_[10].supplyLine("$$Stats     $$`Words`$$  Characters  Messages$$");
                break;
            case 1:
                ScreenTextDisp_[10].supplyLine("$$Stats     Words  $$`Characters`$$  Messages$$");
                break;
            case 2:
                ScreenTextDisp_[10].supplyLine("$$Stats     Words  Characters  $$`Messages`");
                break;
        }
        ScreenTextDisp_[10].supplyLine("$$Current " + CommonTools.intToStr(msgLength0, 7) + "   " + CommonTools.intToStr(msgLength1, 9) + "   " + CommonTools.intToStr(msgLength2, 7) + "   $$" + modelInfoFromMessage(ScreenTextDisp_[workContext].getMessageInfo(modelInfoFromMessage(engineName, 11)), 10));
        ScreenTextDisp_[10].supplyLine("$$History " + CommonTools.intToStr(ctxSummaryWrdUsed, 7) + "   " + CommonTools.intToStr(ctxSummaryChrUsed, 9) + "   " + CommonTools.intToStr(ctxSummaryMsgUsed, 7) + "$$");
        ScreenTextDisp_[10].supplyLine("$$Context " + CommonTools.intToStr(ctxSummaryWrd, 7) + "   " + CommonTools.intToStr(ctxSummaryChr, 9) + "   " + CommonTools.intToStr(ctxSummaryMsg, 7) + "$$");
        ScreenTextDisp_[10].supplyLine("");

        int engineNameLength = 0;
        int counterSizeI = getTokenCounterSize();
        int counterSizeO = getTokenCounterSize();
        int counterSizeT = getTokenCounterSize();
        for (int i = 0; i < EngineName.size(); i++)
        {
            String engineNameItem = EngineName.get(i);
            if (ChatEngine.isValidEngine(engineNameItem))
            {
                engineNameLength = Math.max(engineNameLength, engineNameItem.length());
                EngineFav.set(i, -1);
                counterSizeI = Math.max(counterSizeI, getTokenCounter(engineNameItem, 1).length());
                counterSizeO = Math.max(counterSizeO, getTokenCounter(engineNameItem, 2).length());
                counterSizeT = Math.max(counterSizeT, getTokenCounter(engineNameItem, 3).length());
            }
        }

        
        
        int favIdx = 1;
        String[] fav_ = fav.split(";");
        for (int i = 0; i < fav_.length; i++)
        {
            if (fav_[i].length() > 0)
            {
                int favPos = EngineName.indexOf(fav_[i]);
                if (favPos >= 0)
                {
                    String engineNameItem = EngineName.get(favPos);
                    ScreenTextDisp_[10].supply(String.valueOf(favIdx));
                    if (favIdx < 10)
                    {
                        ScreenTextDisp_[10].supply(".   ");
                    }
                    else
                    {
                        if (favIdx < 100)
                        {
                            ScreenTextDisp_[10].supply(".  ");
                        }
                        else
                        {
                            ScreenTextDisp_[10].supply(". ");
                        }
                    }
                    if (ChatEngine.isValidEngine(engineNameItem))
                    {
                        if (modelTalkList.contains(engineNameItem))
                        {
                            ScreenTextDisp_[10].supply("`");
                        }
                        else
                        {
                            ScreenTextDisp_[10].supply("$$");
                        }
                        ScreenTextDisp_[10].supply(engineNameItem);
                        ScreenTextDisp_[10].supply(CommonTools.stringIndent(engineNameLength - engineNameItem.length(), ' '));
                        ScreenTextDisp_[10].supply(" " + CommonTools.intToStr(getTokenCounter(engineNameItem, 1), counterSizeI));
                        ScreenTextDisp_[10].supply(" " + CommonTools.intToStr(getTokenCounter(engineNameItem, 2), counterSizeO));
                        if (getTokenCounter("", 0) == "3")
                        {
                            ScreenTextDisp_[10].supply(" " + CommonTools.intToStr(getTokenCounter(engineNameItem, 3), counterSizeT));
                        }
                        if (modelTalkList.contains(engineNameItem))
                        {
                            ScreenTextDisp_[10].supply("`");
                        }
                        else
                        {
                            ScreenTextDisp_[10].supply("$$");
                        }
                    }
                    else
                    {
                        ScreenTextDisp_[10].supply("`" + engineNameItem + "`");
                    }
                    ScreenTextDisp_[10].supplyLine("");

                    EngineFav.set(favPos, favIdx);
                    favIdx++;
                }
            }
        }
        archFileName.clear();
        archFileId.clear();
        archFileNumber.clear();
        ArrayList<String> fileList = CommonTools.fileList(CommonTools.applDir);
        for (int i = 0; i < fileList.size(); i++)
        {
            String tempFileName = fileList.get(i);
            if (ChatEngine.contextFile(tempFileName).length() > 0)
            {
                archFileName.add(tempFileName);
                archFileId.add(ChatEngine.contextFile(tempFileName));
                archFileNumber.add(favIdx);

                ScreenTextDisp_[10].supply(String.valueOf(favIdx));
                if (favIdx < 10)
                {
                    ScreenTextDisp_[10].supply(".   ");
                }
                else
                {
                    if (favIdx < 100)
                    {
                        ScreenTextDisp_[10].supply(".  ");
                    }
                    else
                    {
                        ScreenTextDisp_[10].supply(". ");
                    }
                }


                if (archFileLast.equals(tempFileName))
                {
                    ScreenTextDisp_[10].supplyLine('`' + ChatEngine.contextFile(tempFileName) + '`');
                }
                else
                {
                    ScreenTextDisp_[10].supplyLine("$$" + ChatEngine.contextFile(tempFileName) + "$$");
                }
                favIdx++;
            }
        }


        ScreenTextDisp_[10].supplyLine("");
        ScreenTextDisp_[10].supplyLine("Commands:");
        ScreenTextDisp_[10].supplyLine(" `clear` - clear the current context");
        ScreenTextDisp_[10].supplyLine(" `exit` - exit from this application");
        ScreenTextDisp_[10].supplyLine(" `repaint` - repaint the interface after terminal resize or change cell width");
        ScreenTextDisp_[10].supplyLine(" `copy` - copy the last question to edit field");
        ScreenTextDisp_[10].supplyLine(" `historyunit` - change the history message count unit");
        ScreenTextDisp_[10].supplyLine(" `counter` - switch display between counter, price and cost");
        ScreenTextDisp_[10].supplyLine(" `counterreset` - reset the token counter for the selected model");
        ScreenTextDisp_[10].supplyLine(" `archive` - archive the current context");
        ScreenTextDisp_[10].supplyLine(" `archdelete` - delete last created or restored archive");
        ScreenTextDisp_[10].supplyLine("");
        ScreenTextDisp_[10].supplyLine("Letter with number:");
        switch (CF.ParamGetI("HistoryUnit"))
        {
            case 0:
                ScreenTextDisp_[10].supplyLine(" `h_` - history words limit: " + CommonTools.intLimited(CF.ParamGetI("HistoryLimit")));
                break;
            case 1:
                ScreenTextDisp_[10].supplyLine(" `h_` - history characters limit: " + CommonTools.intLimited(CF.ParamGetI("HistoryLimit")));
                break;
            case 2:
                ScreenTextDisp_[10].supplyLine(" `h_` - history messages limit: " + CommonTools.intLimited(CF.ParamGetI("HistoryLimit")));
                break;
        }
        ScreenTextDisp_[10].supplyLine(" `a_` - answer tokens limit: " + CommonTools.intLimited(CF.ParamGetI("AnswerLimit")));
        ScreenTextDisp_[10].supplyLine(" `f_` - field size: " + CF.ParamGetI("FieldSize"));
        ScreenTextDisp_[10].supplyLine(" `t_` - temperature x100 (from 0 to 200): " + CommonTools.intIsSpecified(CF.ParamGetI("Temperature"), 0, 200));
        ScreenTextDisp_[10].supplyLine(" `n_` - nucleus sampling x100 (from 0 to 100): " + CommonTools.intIsSpecified(CF.ParamGetI("TopP"), 0, 100));
        ScreenTextDisp_[10].supplyLine(" `w_` - waiting timeout: " + CommonTools.intLimited(CF.ParamGetI("WaitTimeout")));
        ScreenTextDisp_[10].supplyLine(" `m_` - message width percent: " + CommonTools.intLimited(CF.ParamGetI("MarkdownMessageWidth")));
        ScreenTextDisp_[10].supplyLine(" `c_` - cell width in table: " + CommonTools.intLimited(CF.ParamGetI("MarkdownCellWidth")));
        //ScreenTextDisp_[10].supplyLine(" `l_` - log requests/responses (0, 1, 2): " + (CF.ParamGetB("Log") ? "Yes" : "No") + ((CF.ParamGetI("Log") == 2) ? ", clear" : ""));
        ScreenTextDisp_[10].supplyLine("");


        
        
        for (int i = 0; i < EngineName.size(); i++)
        {
            String engineNameItem = EngineName.get(i);
            String engineNameFavNum = modelInfoFavNumber(engineNameItem, false);
            if (engineNameFavNum.equals("-1"))
            {
                ScreenTextDisp_[10].supply("\\*$$    ");
            }
            else
            {
                ScreenTextDisp_[10].supply(engineNameFavNum);
                ScreenTextDisp_[10].supply("$$");
                ScreenTextDisp_[10].supply(CommonTools.stringIndent(5 - engineNameFavNum.length(), ' '));
            }
            
            if (ChatEngine.isValidEngine(engineNameItem))
            {
                if (modelTalkList.contains(engineNameItem))
                {
                    ScreenTextDisp_[10].supply("$$`");
                }
                else
                {
                    ScreenTextDisp_[10].supply("");
                }
                ScreenTextDisp_[10].supply(engineNameItem);
                ScreenTextDisp_[10].supply(CommonTools.stringIndent(engineNameLength - engineNameItem.length(), ' '));
                ScreenTextDisp_[10].supply(" " + CommonTools.intToStr(getTokenCounter(engineNameItem, 1), counterSizeI));
                ScreenTextDisp_[10].supply(" " + CommonTools.intToStr(getTokenCounter(engineNameItem, 2), counterSizeO));
                if (getTokenCounter("", 0) == "3")
                {
                    ScreenTextDisp_[10].supply(" " + CommonTools.intToStr(getTokenCounter(engineNameItem, 3), counterSizeT));
                }
                if (modelTalkList.contains(engineNameItem))
                {
                    ScreenTextDisp_[10].supply("`");
                }
                else
                {
                    ScreenTextDisp_[10].supply("$$");
                }
            }
            else
            {
                ScreenTextDisp_[10].supply("$$`" + engineNameItem + "`");
            }
            ScreenTextDisp_[10].supplyLine("");
        }
    }
    
    static void contextReload(int i, boolean preservePositon)
    {
        String dataFileName = ScreenTextDisp_[i].fileName;
        int dispLineNumber = -1;
        if (ScreenTextDisp_[i].textRaw.size() > 1)
        {
            if (preservePositon)
            {
                dispLineNumber = ScreenTextDisp_[i].textRaw.get(ScreenTextDisp_[i].displayOffset).lineNumber;
            }
        }
        ScreenTextDisp_[i].fileName = "";
        ScreenTextDisp_[i].clear(false);
        ScreenTextDisp_[i].supplyFile(dataFileName, true);
        ScreenTextDisp_[i].fileName = dataFileName;
        if (dispLineNumber >= 0)
        {
            while ((ScreenTextDisp_[i].displayOffset > 0) && (ScreenTextDisp_[i].textRaw.get(ScreenTextDisp_[i].displayOffset).lineNumber > dispLineNumber))
            {
                ScreenTextDisp_[i].displayOffset--;
            }
            while ((ScreenTextDisp_[i].displayOffset < (ScreenTextDisp_[i].textRaw.size() - 1)) && (ScreenTextDisp_[i].textRaw.get(ScreenTextDisp_[i].displayOffset).lineNumber < dispLineNumber))
            {
                ScreenTextDisp_[i].displayOffset++;
            }
        }
        else
        {
            ScreenTextDisp_[i].displayScrollDn(-9);
        }
    }
    
    static boolean isStandardCommand(String cmd)
    {
        cmd = cmd.trim().toLowerCase();
        if (cmd.equals("clear"))
        {
            ScreenTextDisp_[workContext].clear(false);
            refreshSettingText();
            return false;
        }
        if (cmd.equals("exit"))
        {
            progWork = false;
            return false;
        }
        if (cmd.equals("historyunit"))
        {
            int temp = CF.ParamGetI("HistoryUnit");
            temp++;
            if (temp == 3)
            {
                temp = 0;
            }
            CF.ParamSet("HistoryUnit", temp);
            CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
            refreshSettingText();
            return false;
        }
        if (cmd.equals("counter"))
        {
            int t = CF.ParamGetI("Counter");
            t = t + 1;
            if (t == 3) t = 0;
            CF.ParamSet("Counter", t);
            CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
            refreshSettingText();
            return false;
        }
        if (cmd.equals("counterreset"))
        {
            CFC.ParamRemove(CF.ParamGetS("Model") + "-i");
            CFC.ParamRemove(CF.ParamGetS("Model") + "-o");
            CFC.FileSave(CommonTools.applDir + CommonTools.counterFileName);
            refreshSettingText();
            return false;
        }
        if (cmd.startsWith("pricei"))
        {
            int priceVal = CommonTools.strToInt(cmd.substring(6), 0);
            if (priceVal > 0)
            {
                CFC.ParamSet(CF.ParamGetS("Model") + "-ii", priceVal);
            }
            else
            {
                CFC.ParamRemove(CF.ParamGetS("Model") + "-ii");
            }
            CFC.FileSave(CommonTools.applDir + CommonTools.counterFileName);
            refreshSettingText();
            return false;
        }
        if (cmd.startsWith("priceo"))
        {
            int priceVal = CommonTools.strToInt(cmd.substring(6), 0);
            if (priceVal > 0)
            {
                CFC.ParamSet(CF.ParamGetS("Model") + "-oo", priceVal);
            }
            else
            {
                CFC.ParamRemove(CF.ParamGetS("Model") + "-oo");
            }
            CFC.FileSave(CommonTools.applDir + CommonTools.counterFileName);
            refreshSettingText();
            return false;
        }
        if (cmd.equals("copy"))
        {
            ScreenTextInput_.reset();
            if (workState == 0)
            {
                ScreenTextInput_.textValue = ScreenTextDisp.convMultiToSingle(ScreenTextDisp_[workContext].getLastQuestion(false));
            }
            if (workState == 1)
            {
                ScreenTextInput_.textValue = "~" + CF.ParamGetS("Hint" + workContext);
            }
            ScreenTextInput_.textPos = -1;
            return false;
        }
        if (cmd.equals("archdelete"))
        {
            if (!archFileLast.isBlank())
            {
                CommonTools.fileClear(CommonTools.applDir + archFileLast);
            }
            refreshSettingText();
            return false;
        }
        if (cmd.equals("archive"))
        {
            long archFileNameI = Long.valueOf(CommonTools.timeStamp());
            archFileLast = CommonTools.contextFileNamePrefix + archFileNameI + CommonTools.contextFileNameSuffix;
            CommonTools.fileCopy(ScreenTextDisp_[workContext].fileName, CommonTools.applDir + archFileLast);
            refreshSettingText();
            return false;
        }
        if (cmd.equals("repaint") || cmd.equals("repaint_1") || cmd.equals("repaint_2"))
        {
            boolean repaint1 = cmd.equals("repaint") || cmd.equals("repaint_1");
            boolean repaint2 = cmd.equals("repaint") || cmd.equals("repaint_2");
            if (repaint1)
            {
                ConsoleInputOutput_.screenClear();
                ConsoleInputOutput_.getScreenSize();
                ConsoleInputOutput_.screenWidth -= (ConsoleInputOutput_.screenWidth % 2);
                int inputBoxHeight_ = CF.ParamGetI("FieldSize");
                if (inputBoxHeight_ < 1) inputBoxHeight_ = 1;
                if ((ConsoleInputOutput_.screenHeight % 2) == 0)
                {
                    inputBoxHeight = inputBoxHeight_ + ((inputBoxHeight_ + 1) % 2);
                }
                else
                {
                    inputBoxHeight = inputBoxHeight_ + (inputBoxHeight_ % 2);
                }

                ConsoleInputOutput_.charSizeReset(CF.ParamGetB("MarkdownDuospace"));
                int messageWidth = CF.ParamGetI("MarkdownMessageWidth");
                if ((messageWidth <= 0) || (messageWidth > 100))
                {
                    messageWidth = 100;
                    CF.ParamSet("MarkdownMessageWidth", 100);
                }
                ScreenTextDisp.displayResize(ConsoleInputOutput_.screenHeight - inputBoxHeight);
            }
            if (repaint2)
            {
                modelTalkListUpdate();
                int messageWidth = CF.ParamGetI("MarkdownMessageWidth");
                if ((messageWidth <= 0) || (messageWidth > 100))
                {
                    messageWidth = 100;
                    CF.ParamSet("MarkdownMessageWidth", 100);
                }
                for (int i = 0; i < (workContextCount + 1); i++)
                {
                    ScreenTextDisp_[i].textWidth = ConsoleInputOutput_.screenWidth;
                    if (i == workContextCount)
                    {
                        ScreenTextDisp_[i].textMessageWidth = ScreenTextDisp_[i].textWidth;
                    }
                    else
                    {
                        ScreenTextDisp_[i].textMessageWidth = (ScreenTextDisp_[i].textWidth * messageWidth) / 100;
                        ScreenTextDisp_[i].textMessageWidth -= (ScreenTextDisp_[i].textMessageWidth % 2);
                    }
                    ScreenTextDisp_[i].textHeight = ConsoleInputOutput_.screenHeight - inputBoxHeight;
                    ScreenTextDisp_[i].textOffsetLine = ((ScreenTextDisp_[i].textHeight - 1) / 2);
                    if (i < workContextCount)
                    {
                        contextReload(i, true);
                    }
                }


                ScreenTextInput_.fieldPos = ConsoleInputOutput_.screenHeight - inputBoxHeight;
                ScreenTextInput_.fieldSize = inputBoxHeight;
                ScreenTextInput_.reset();
            }
            return false;
        }
        if (cmd.equals("markdowntest"))
        {
            modelTalkListUpdate();
            for (int i = 0; i < (workContextCount + 1); i++)
            {
                ScreenTextDisp_[i].parseMarkdown = !ScreenTextDisp_[i].parseMarkdown;
                if (i < workContextCount)
                {
                    contextReload(i, true);
                }
            }
            refreshSettingText();
            return false;
        }
        if (cmd.equals("markdownheader1") || cmd.equals("markdownheader2") || cmd.equals("markdownheader3") || cmd.equals("markdownheader4") || cmd.equals("markdownheader5") || cmd.equals("markdownheader6") || cmd.equals("markdownheader7"))
        {
            CF.ParamSet("MarkdownHeader", cmd.substring(14));
            CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
            modelTalkListUpdate();
            for (int i = 0; i < workContextCount; i++)
            {
                contextReload(i, true);
            }
            return false;
        }
        if (cmd.equals("markdownduospace0"))
        {
            CF.ParamSet("MarkdownDuospace", false);
            CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
            ConsoleInputOutput_.charSizeReset(false);
            modelTalkListUpdate();
            for (int i = 0; i < workContextCount; i++)
            {
                contextReload(i, true);
            }
            return false;
        }
        if (cmd.equals("markdownduospace1"))
        {
            CF.ParamSet("MarkdownDuospace", true);
            CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
            ConsoleInputOutput_.charSizeReset(true);
            modelTalkListUpdate();
            for (int i = 0; i < workContextCount; i++)
            {
                contextReload(i, true);
            }
            return false;
        }

        return true;
    }
    
    static void updateEngineList(boolean getFromServer, boolean updateListFile, ChatEngine e1, ChatEngine e2, ChatEngine e3, ChatEngine ex)
    {
        waitStart(0, "");

        if (updateListFile)
        {
            CommonTools.fileClear(CommonTools.applDir + CommonTools.modelsFileName);
        }

        EngineName.clear();
        EngineFav.clear();
        String testMsg = CF.ParamGetS("TestModel");
        if (!getFromServer)
        {
            CF.ParamSet("TestModel", "");
        }

        boolean engineListError = false;
        
        ChatEngine eList[] = new ChatEngine[4];
        String eListS[] = new String[4];
        eList[0] = ex;
        eList[1] = e1;
        eList[2] = e2;
        eList[3] = e3;
        eListS[0] = "0";
        eListS[1] = "1";
        eListS[2] = "2";
        eListS[3] = "3";
        
        for (int eListI = 0; eListI < 4; eListI++)
        {
            ArrayList<String> engines = eList[eListI].getEngines(getFromServer);
            for (int i = 0; i < engines.size(); i++)
            {
                if (ChatEngine.isValidEngine(engines.get(i)))
                {
                    if (eList[eListI].testEngine(engines.get(i)))
                    {
                        if (updateListFile)
                        {
                            CommonTools.fileSaveText(CommonTools.applDir + CommonTools.modelsFileName, eListS[eListI] + engines.get(i) + "\n");
                        }
                        EngineName.add(engines.get(i));
                        EngineFav.add(-1);
                    }
                    else
                    {
                        if (updateListFile)
                        {
                            CommonTools.fileSaveText(CommonTools.applDir + CommonTools.modelsFileName, "~" + eListS[eListI] + engines.get(i) + "\n");
                        }
                    }
                }
                else
                {
                    EngineName.add(engines.get(i));
                    EngineFav.add(-1);
                    engineListError = true;
                }
            }
        }

        if (updateListFile && engineListError)
        {
            CommonTools.fileClear(CommonTools.applDir + CommonTools.modelsFileName);
        }
        
        if (!getFromServer)
        {
            CF.ParamSet("TestModel", testMsg);
        }

        Collections.sort(EngineName);
        waitStop();

        refreshSettingText();
    }
    
    static void setEngine(String cmd, ChatEngine e1, ChatEngine e2, ChatEngine e3, ChatEngine ex, boolean configSave)
    {
        e1.setEngine(cmd.trim());
        e2.setEngine(cmd.trim());
        e3.setEngine(cmd.trim());
        ex.setEngine(cmd.trim());
        
        e1.setHint(CF.ParamGetS("Hint" + workContext));
        e2.setHint(CF.ParamGetS("Hint" + workContext));
        e3.setHint(CF.ParamGetS("Hint" + workContext));
        ex.setHint(CF.ParamGetS("Hint" + workContext));

        if (e1.isActive)
        {
            e2.isActive = false;
            e3.isActive = false;
            ex.isActive = false;
            CF.ParamSet("Model", e1.engineName);
            if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
        }
        else
        {
            if (e2.isActive)
            {
                e3.isActive = false;
                ex.isActive = false;
                CF.ParamSet("Model", e2.engineName);
                if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
            }
            else
            {
                if (e3.isActive)
                {
                    ex.isActive = false;
                    CF.ParamSet("Model", e3.engineName);
                    if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                }
                else
                {
                    ex.isActive = true;
                    CF.ParamSet("Model", ex.engineName);
                    if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                }
            }
        }
    }
    
    static void sendSettingsCommand(String cmd, ChatEngine e1, ChatEngine e2, ChatEngine e3, ChatEngine ex, boolean configSave)
    {
        int opt = 0;
        int S_num = 0;

        if (cmd.length() > 1)
        {
            S_num = CommonTools.strToInt(cmd.substring(1), -1);
            char cmdChar = cmd.toLowerCase().charAt(0);
            switch (cmdChar)
            {
                case 'f':
                    if ((S_num > 0) && (S_num <= ConsoleInputOutput_.screenHeight))
                    {
                        CF.ParamSet("FieldSize", S_num);
                        if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                    }
                    break;
                case 't':
                    if (S_num >= 0)
                    {
                        CF.ParamSet("Temperature", S_num);
                        if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                    }
                    break;
                case 'n':
                    if (S_num >= 0)
                    {
                        CF.ParamSet("TopP", S_num);
                        if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                    }
                    break;
                case 'h':
                    if (S_num >= 0)
                    {
                        CF.ParamSet("HistoryLimit", S_num);
                        if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                    }
                    break;
                case 'a':
                    if (S_num >= 0)
                    {
                        CF.ParamSet("AnswerLimit", S_num);
                        if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                    }
                    break;
                case 'w':
                    if (S_num >= 0)
                    {
                        CF.ParamSet("WaitTimeout", S_num);
                        if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                    }
                    break;
                /*case 'l':
                    if (S_num >= 0)
                    {
                        CF.ParamSet("Log", S_num);
                        if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                    }
                    if (S_num == 2)
                    {
                        CommonTools.fileClear(CommonTools.applDir + CommonTools.logFileName);
                    }
                    break;*/
                case 'm':
                    if (S_num >= 0)
                    {
                        CF.ParamSet("MarkdownMessageWidth", S_num);
                        if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                    }
                    break;
                case 'c':
                    if (S_num >= 0)
                    {
                        CF.ParamSet("MarkdownCellWidth", S_num);
                        if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                        for (int i = 0; i < (workContextCount + 1); i++)
                        {
                            ScreenTextDisp_[i].tableCellWidth = CF.ParamGetI("MarkdownCellWidth");
                        }
                    }
                    break;
                case '.':
                case ',':
                    {
                        String modelList = "";
                        boolean modelListValid = true;
                        for (int i = 1; i < cmd.length(); i++)
                        {
                            if (CommonTools.isChar(cmd.charAt(i), false, false, true, false))
                            {
                                modelList = modelList + cmd.charAt(i);
                            }
                            else
                            {
                                modelListValid = false;
                            }
                        }
                        if ((modelListValid) && (!modelList.isEmpty()))
                        {
                            CF.ParamSet("Model", modelList);
                            if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                            opt = 9;
                        }
                    }
                    break;
            }
            if ((cmdChar >= 'a') && (cmdChar <= 'z'))
            {
                if (S_num >= 0)
                {
                    opt = 9;
                }
            }
        }
        switch (opt)
        {
            case 0:
                S_num = CommonTools.strToInt(cmd, -1);
                if (S_num > 0)
                {
                    for (int i = 0; i < EngineName.size(); i++)
                    {
                        if (EngineFav.get(i) == S_num)
                        {
                            cmd = EngineName.get(i);
                        }
                    }
                    for (int i = 0; i < archFileNumber.size(); i++)
                    {
                        if (archFileNumber.get(i) == S_num)
                        {
                            cmd = archFileId.get(i);
                        }
                    }
                }
                else
                {
                    if (S_num == 0)
                    {
                        if (!ScreenTextDisp_[workContext].getMessageInfo("").isBlank())
                        {
                            cmd = modelInfoFromMessage(ScreenTextDisp_[workContext].getMessageInfo(""), 10);
                            if (CommonTools.strOnlyDigits(cmd))
                            {
                                CF.ParamSet("Model", cmd);
                                if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                                cmd = "";
                            }
                        }
                        else
                        {
                            cmd = "";
                        }
                    }
                }
                
                if (!cmd.isBlank())
                {
                    cmd = CommonTools.modelNameBlankCharRemove(cmd);
                    setEngine(cmd, e1, e2, e3, ex, configSave);
                }
                
                
                for (int i = 0; i < archFileId.size(); i++)
                {
                    if (archFileId.get(i).equals(cmd))
                    {
                        modelTalkListUpdate();
                        archFileLast = archFileName.get(i);
                        ScreenTextDisp_[workContext].clear(false);
                        CommonTools.fileCopy(CommonTools.applDir + archFileLast, ScreenTextDisp_[workContext].fileName);
                        contextReload(workContext, false);
                    }
                }
                
                break;
        }
        
        refreshSettingText();
    }
    
    
    static boolean progWork = true;
    static Timer timer;
    static int workState = 0;
    
    static int workContext;
    static int workContextCount = 10;
    
    
    static void selectContext(int ctx, boolean configSave)
    {
        workContext = ctx;
        CF.ParamSet("Context", ctx);
        if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
        refreshSettingText();
        ScreenTextDisp_[workContext].displayAll();
    }
    
    static boolean questionIsHint(String Hint)
    {
        if (Hint.length() < 1)
        {
            return false;
        }
        char hintChar = Hint.charAt(0);
        boolean isHint = false;
        if (hintChar == '`') isHint = true;
        if (hintChar == '~') isHint = true;
        if (isHint)
        {
            for (int i = 1; i < Hint.length(); i++)
            {
                if (Hint.charAt(i) == hintChar) return false;
            }
        }
        return isHint;
    }
    
    static String questionGetHint(String Hint)
    {
        if (Hint.length() < 1)
        {
            return "";
        }
        return Hint.substring(1).trim();
    }
    
    static void questionSetHint(String Question_)
    {
        String Hint = questionGetHint(Question_);
        if ((Hint.length() == 2) && CommonTools.strOnlyDigits(Hint))
        {
            int ctxSrc = (((int)Hint.charAt(0)) - 48);
            int ctxDst = (((int)Hint.charAt(1)) - 48);
            String h = CF.ParamGetS("Hint" + ctxSrc);
            CF.ParamSet("Hint" + ctxDst, h);
        }
        else
        {
            CF.ParamSet("Hint" + workContext, Hint);
        }
        CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
        if (workState == 1)
        {
            refreshSettingText();
            ScreenTextDisp_[workContextCount].displayAll();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        if (args.length > 0)
        {
            CommonTools.applDir = CommonTools.correctDir(args[0]);
        }
        else
        {
            CommonTools.applDir = CommonTools.correctDir(CommonTools.getApplDir(0));
        }
        
        CF = new ConfigFile();
        CF.FileLoad(CommonTools.applDir + CommonTools.configFileName);
        CFC = new ConfigFile();
        CFC.FileLoad(CommonTools.applDir + CommonTools.counterFileName);
        
        boolean appUnusable = false;

        if (CF.ParamGetS("KeyGpt").isBlank() && CF.ParamGetS("KeyGemini").isBlank() && CF.ParamGetS("KeyClaude").isBlank())
        {
            appUnusable = true;
        }
        
        if (CF.ParamGetI("MarkdownHeader") < 1)
        {
            CF.ParamSet("MarkdownHeader", 4);
        }
        
        if (appUnusable)
        {
            System.out.println("Work path:");
            System.out.println(CommonTools.applDir);
            System.out.println("");
            System.out.println("The config.txt file not found or the file does not contain any API key.");
            return;
        }

        EngineName = new ArrayList<>();
        EngineFav = new ArrayList<>();
        archFileName = new ArrayList<>();
        archFileId = new ArrayList<>();
        archFileNumber = new ArrayList<>();
        modelTalkList = new ArrayList<>();
        
        try
        {
            ConsoleInputOutput_ = new ConsoleInputOutput();
            ConsoleInputOutput_.charSizeReset(CF.ParamGetB("MarkdownDuospace"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
        
        if (CF.ParamGetI("Log") == 2)
        {
            CommonTools.fileClear(CommonTools.applDir + CommonTools.logFileName);
        }
        
        
        ConsoleInputOutput_.screenClear();
        
        ScreenTextDisp_ = new ScreenTextDisp[workContextCount + 1];
        for (int i = 0; i < (workContextCount + 1); i++)
        {
            ScreenTextDisp_[i] = new ScreenTextDisp(ConsoleInputOutput_, CF);
            ScreenTextDisp_[i].tableCellWidth = CF.ParamGetI("MarkdownCellWidth");
            ScreenTextDisp_[i].clear(false);
            if (i < workContextCount)
            {
                ScreenTextDisp_[i].fileName = CommonTools.applDir + CommonTools.contextFileNamePrefix + i + CommonTools.contextFileNameSuffix;
            }
        }
        ScreenTextDisp_[workContextCount].displayOffset = 0;
        ScreenTextInput_ = new ScreenTextInput(ConsoleInputOutput_);

        workContext = CF.ParamGetI("Context");
        if ((workContext < 0) || (workContext > 9)) workContext = 0;


        // Read Markdown file for test and debug purposes
        if (args.length > 1)
        {
            String ViewFileName = args[1];
            if (CommonTools.fileGetSize(ViewFileName) > 0)
            {
                ScreenTextDisp_[workContext].clear(false);
                CommonTools.fileCopy(ViewFileName, ScreenTextDisp_[workContext].fileName);
            }
        }

        
        
        ChatEngine ChatEngineGpt_ = new ChatEngineGpt(CF, CFC);
        ChatEngine ChatEngineGemini_ = new ChatEngineGemini(CF, CFC);
        ChatEngine ChatEngineClaude_ = new ChatEngineClaude(CF, CFC);
        ChatEngine ChatEngineDummy_ = new ChatEngine(CF, CFC);

        isStandardCommand("repaint_1");
        
        ArrayList<String> fileModelNames = CommonTools.fileLoadText(CommonTools.applDir + CommonTools.modelsFileName, false);
        if (fileModelNames.size() > 0)
        {
            ChatEngineGpt_.setEngineItem(null);
            ChatEngineGemini_.setEngineItem(null);
            ChatEngineClaude_.setEngineItem(null);
            ChatEngineDummy_.setEngineItem(null);
            for (int i = 0; i < fileModelNames.size(); i++)
            {
                String engineNameItem = fileModelNames.get(i);
                if ((engineNameItem.length() > 1) && (ChatEngine.isValidEngine(engineNameItem.substring(1))))
                {
                    if (engineNameItem.startsWith("0"))
                    {
                        ChatEngineDummy_.setEngineItem(engineNameItem.substring(1));
                    }
                    if (engineNameItem.startsWith("1"))
                    {
                        ChatEngineGpt_.setEngineItem(engineNameItem.substring(1));
                    }
                    if (engineNameItem.startsWith("2"))
                    {
                        ChatEngineGemini_.setEngineItem(engineNameItem.substring(1));
                    }
                    if (engineNameItem.startsWith("3"))
                    {
                        ChatEngineClaude_.setEngineItem(engineNameItem.substring(1));
                    }
                }
            }
            updateEngineList(false, false, ChatEngineGpt_, ChatEngineGemini_, ChatEngineClaude_, ChatEngineDummy_);
        }
        else
        {
            updateEngineList(true, true, ChatEngineGpt_, ChatEngineGemini_, ChatEngineClaude_, ChatEngineDummy_);
        }
        
        isStandardCommand("repaint_2");
        

        sendSettingsCommand(CommonTools.modelNameBlankCharS + CF.ParamGetS("Model") + CommonTools.modelNameBlankCharS, ChatEngineGpt_, ChatEngineGemini_, ChatEngineClaude_, ChatEngineDummy_, false);
        
        
        ConsoleInputOutput_.ringBell();

        
        ArrayList<TalkObject> engineTalkList = new ArrayList<>();
        
        
        ScreenTextDisp_[workContext].displayAll();
        while (progWork)
        {
            int ctx = Math.min(workContext + workState * workContextCount, workContextCount);
            boolean work = true;
            if (ScreenTextInput_.textPos >= 0)
            {
                ScreenTextInput_.reset();
            }
            else
            {
                ScreenTextInput_.textPos = ScreenTextInput_.textValue.length();
                ScreenTextInput_.keyEvent(0);
            }
            while (work)
            {
                ScreenTextInput_.repaintCursor();
                int key = ConsoleInputOutput_.getKey();
                
                //int pageSize = ScreenTextDisp_[ctx].textHeight - 1;
                int pageSize = (ScreenTextDisp_[ctx].textHeight - 1) / 2;
                int scrollColumnSize = ScreenTextDisp_[ctx].textWidth / 10;
                if (scrollColumnSize < 1) scrollColumnSize = 1;
                switch (key)
                {
                    case (ConsoleInputOutput.keySpecialNum + 1):
                        ScreenTextDisp_[ctx].displayScrollUp(1);
                        break;
                    case (ConsoleInputOutput.keySpecialNum + 2):
                        ScreenTextDisp_[ctx].displayScrollDn(1);
                        break;
                    case (ConsoleInputOutput.keySpecialNum + 3):
                        if (!ScreenTextDisp_[ctx].blockScroll(scrollColumnSize))
                        {
                            work = ScreenTextInput_.keyEvent(key);
                        }
                        break;
                    case (ConsoleInputOutput.keySpecialNum + 4):
                        if (!ScreenTextDisp_[ctx].blockScroll(0 - scrollColumnSize))
                        {
                            work = ScreenTextInput_.keyEvent(key);
                        }
                        break;
                    case (ConsoleInputOutput.keySpecialNum + 15):
                        ScreenTextDisp_[ctx].displayScrollUp(pageSize);
                        break;
                    case (ConsoleInputOutput.keySpecialNum + 16):
                        ScreenTextDisp_[ctx].displayScrollDn(pageSize);
                        break;
                    case (ConsoleInputOutput.keySpecialNum + 13):
                        ScreenTextDisp_[ctx].displayScrollUp(-1);
                        break;
                    case (ConsoleInputOutput.keySpecialNum + 14):
                        ScreenTextDisp_[ctx].displayScrollDn(-1);
                        break;
                    case 9:
                        if (workState == 0)
                        {
                            ScreenTextDisp_[ctx].ommitSwitch(-1);
                        }
                        break;
                    default:
                        work = ScreenTextInput_.keyEvent(key);
                        break;
                    case -1:
                        work = false;
                        progWork = false;
                        break;
                }
            }
            String S = ScreenTextInput_.textValue.trim();
            String S_ = ScreenTextInput_.textValue;
            ScreenTextInput_.reset();

            if (S.length() == 0)
            {
                workState++;
                if (workState == 2)
                {
                    workState = 0;
                }
                switch (workState)
                {
                    case 0:
                        ScreenTextDisp_[workContext].displayAll();
                        break;
                    case 1:
                        refreshSettingText();
                        ScreenTextDisp_[workContextCount].displayAll();
                        break;
                }
            }
            if (S.length() > 0)
            {
                if (isStandardCommand(S))
                {
                    if (questionIsHint(S))
                    {
                        questionSetHint(S);
                    }
                    else
                    {
                        switch (workState)
                        {
                            case 0: // Use chat
                                if (S.length() == 1)
                                {
                                    if ((S.charAt(0) >= 48) && (S.charAt(0) <= 57))
                                    {
                                        selectContext(((int)S.charAt(0)) - 48, true);
                                    }
                                }
                                else
                                {
                                    S = ScreenTextDisp.convSingleToMulti(S_);
                                    S = ScreenTextDisp.convPlainToMarkdown(S);
                                    S = ScreenTextDisp.convMarkdownToPlain(S);

                                    boolean engineMulti = modelTalkListUpdate();
                                    String totalTokensInfo = "";
                                    for (int engineI = 0; engineI < modelTalkList.size(); engineI++)
                                    {
                                        if (engineI > 0)
                                        {
                                            totalTokensInfo = totalTokensInfo + CommonTools.splitterInfo;
                                        }
                                        totalTokensInfo = totalTokensInfo + "1" + CommonTools.splitterInfo + modelTalkList.get(engineI);
                                    }


                                    int questionPrevIdx = CommonTools.strToInt(ScreenTextDisp_[workContext].getLastQuestion(true), -1);
                                    String questionPrev = ScreenTextDisp.convMultiToSingle(ScreenTextDisp_[workContext].getLastQuestion(false));
                                    String questionNext = ScreenTextDisp.convMultiToSingle(S);
                                    boolean questionTheSame = questionPrev.equals(questionNext);


                                    if (questionTheSame)
                                    {
                                        ScreenTextDisp_[ctx].supplyPointSave();
                                        ScreenTextDisp_[ctx].displayScrollDn(-1);
                                        ScreenTextDisp_[ctx].supplyPointRestore();
                                    }
                                    else
                                    {
                                        ScreenTextDisp_[ctx].supplyPointSave();
                                        ScreenTextDisp_[ctx].supplyLine("");
                                        ScreenTextDisp_[ctx].messageIdxCounter = -1;
                                        ScreenTextDisp_[ctx].supplyLine("___<<<" + totalTokensInfo + ">>>___");
                                        ScreenTextDisp_[ctx].supplyLine("");
                                        ScreenTextDisp_[ctx].supplyLine(ScreenTextDisp.convPlainToMarkdown(S));
                                        ScreenTextDisp_[ctx].supplyLine("");
                                        ScreenTextDisp_[ctx].displayScrollDn(-1);
                                        ScreenTextDisp_[ctx].supplyPointRestore();
                                    }


                                    while (engineTalkList.size() < modelTalkList.size())
                                    {
                                        engineTalkList.add(new TalkObject(modelTalkList.get(engineTalkList.size()), ChatEngineGpt_, ChatEngineGemini_, ChatEngineClaude_, ChatEngineDummy_));
                                    }
                                    while (engineTalkList.size() > modelTalkList.size())
                                    {
                                        engineTalkList.remove(modelTalkList.size());
                                    }
                                    for (int iii = 0; iii < modelTalkList.size(); iii++)
                                    {
                                        engineTalkList.get(iii).engineName = modelTalkList.get(iii);
                                    }

                                    String engineCurrentModel = CF.ParamGetS("Model");

                                    // Auto ommit prevoius answers when the question is repeated
                                    ArrayList<Integer> questionTheSameOmmitList = null;
                                    if (questionTheSame)
                                    {
                                        questionTheSameOmmitList = new ArrayList<>();
                                        int iN = ScreenTextDisp_[ctx].textMsg.size();
                                        for (int i = questionPrevIdx; i < iN; i++)
                                        {
                                            if (!ScreenTextDisp_[ctx].textMsg.get(i).ommit)
                                            {
                                                questionTheSameOmmitList.add(i);
                                                ScreenTextDisp_[ctx].textMsg.get(i).ommit = true;
                                                //ScreenTextDisp_[ctx].ommitSwitch(i);
                                            }
                                        }
                                    }

                                    // Prepare talk
                                    for (int engineI = 0; engineI < engineTalkList.size(); engineI++)
                                    {
                                        setEngine(engineTalkList.get(engineI).engineName, ChatEngineGpt_, ChatEngineGemini_, ChatEngineClaude_, ChatEngineDummy_, false);
                                        engineTalkList.get(engineI).talkPrepare(ScreenTextDisp_[ctx].textMsg, engineMulti ? (engineTalkList.get(engineI).engineName) : "", S);
                                    }



                                    // Execute talk
                                    /*for (int engineI = 0; engineI < engineTalkList.size(); engineI++)
                                    {
                                        String waitInfo_ = engineMulti ? (" " + (engineI + 1) + "/" + engineTalkList.size()) : "";
                                        waitStart(CF.ParamGetI("WaitTimeout"), waitInfo_);
                                        engineTalkList.get(engineI).talk();
                                        waitStopX();
                                    }*/

                                    waitStart(CF.ParamGetI("WaitTimeout"), "");
                                    ExecutorService ExecutorService_ = Executors.newCachedThreadPool();

                                    for (int engineI = 0; engineI < engineTalkList.size(); engineI++)
                                    {
                                        engineTalkList.get(engineI).talkThreadStart(ExecutorService_);
                                    }

                                    for (int engineI = 0; engineI < engineTalkList.size(); engineI++)
                                    {
                                        engineTalkList.get(engineI).talkThreadWait();
                                    }

                                    ExecutorService_.shutdown();
                                    waitStopX();



                                    // Measure new characters to avid display glitches
                                    for (int engineI = 0; engineI < engineTalkList.size(); engineI++)
                                    {
                                        for (int i = 0; i < engineTalkList.get(engineI).answer.length(); i++)
                                        {
                                            ConsoleInputOutput_.charSize(engineTalkList.get(engineI).answer.charAt(i));
                                        }
                                    }                                


                                    waitStart(0, "");

                                    if (questionTheSame)
                                    {
                                        for (int i = 0; i < questionTheSameOmmitList.size(); i++)
                                        {
                                            if (ScreenTextDisp_[ctx].textMsg.get(questionTheSameOmmitList.get(i)).ommit)
                                            {
                                                ScreenTextDisp_[ctx].textMsg.get(questionTheSameOmmitList.get(i)).ommit = false;
                                                //ScreenTextDisp_[ctx].ommitSwitch(questionTheSameOmmitList.get(i));
                                            }
                                        }
                                    }

                                    // Rendering question
                                    ScreenTextDisp_[ctx].messageIdxCounter = ScreenTextDisp_[ctx].textMsg.size() - 1;

                                    totalTokensInfo = "";
                                    for (int engineI = 0; engineI < engineTalkList.size(); engineI++)
                                    {
                                        if (engineI > 0)
                                        {
                                            totalTokensInfo = totalTokensInfo + CommonTools.splitterInfo;
                                        }
                                        totalTokensInfo = totalTokensInfo + engineTalkList.get(engineI).tokensI + "" + CommonTools.splitterInfo + engineTalkList.get(engineI).engineName;
                                    }

                                    if (!questionTheSame)
                                    {
                                        ScreenTextDisp_[ctx].supplyLine("");
                                        ScreenTextDisp_[ctx].messageIdxCounter = ScreenTextDisp_[ctx].textMsg.size();
                                    }
                                    int tempMsgTokens = CommonTools.strToInt(ConChat.modelInfoFromMessage("!!!" + totalTokensInfo + "!!!", 8), -1);
                                    String tempMsgModel = ConChat.modelInfoFromMessage("!!!" + totalTokensInfo + "!!!", 0);
                                    if (!questionTheSame)
                                    {
                                        ScreenTextDisp_[ctx].textMsg.add(new ScreenTextDispMessage(false, S, tempMsgTokens, tempMsgModel));
                                        ScreenTextDisp_[ctx].supplyLine("___<<<" + totalTokensInfo + "<<<___");
                                        ScreenTextDisp_[ctx].supplyLine("");
                                        ScreenTextDisp_[ctx].supplyLine(ScreenTextDisp.convPlainToMarkdown(S));
                                    }


                                    // Rendering answer
                                    for (int engineI = 0; engineI < engineTalkList.size(); engineI++)
                                    {
                                        ScreenTextDisp_[ctx].supplyLine("");
                                        ScreenTextDisp_[ctx].messageIdxCounter = ScreenTextDisp_[ctx].textMsg.size();
                                        ScreenTextDisp_[ctx].textMsg.add(new ScreenTextDispMessage(true, engineTalkList.get(engineI).answer, engineTalkList.get(engineI).tokensO, engineTalkList.get(engineI).tokensE));
                                        ScreenTextDisp_[ctx].supplyLine("___>>>" + engineTalkList.get(engineI).tokensO + "" + CommonTools.splitterInfo + engineTalkList.get(engineI).tokensE + ">>>___");
                                        ScreenTextDisp_[ctx].supplyLine("");
                                        ScreenTextDisp_[ctx].supplyLine(engineTalkList.get(engineI).answer);
                                    }                                


                                    waitStop();

                                    CF.ParamSet("Model", engineCurrentModel);

                                    ScreenTextDisp_[ctx].displayAll();
                                }
                                break;
                            case 1: // Settings
                                {
                                    S = S.trim().toLowerCase();
                                    sendSettingsCommand(S, ChatEngineGpt_, ChatEngineGemini_, ChatEngineClaude_, ChatEngineDummy_, true);
                                    ScreenTextDisp_[workContextCount].displayAll();
                                }
                                break;

                        }
                    }
                }
                else
                {
                    ScreenTextDisp_[ctx].displayAll();
                }
                ConsoleInputOutput_.ringBell();
            }
        }
        ConsoleInputOutput_.setTextAttrReset();
        ConsoleInputOutput_.screenClear();
        ConsoleInputOutput_.printFlush();
    }
}
