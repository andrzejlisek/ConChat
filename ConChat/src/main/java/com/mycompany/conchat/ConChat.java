/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conchat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;


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
            ConsoleInputOutput_.printFlush();
        }
    }
    
    static void waitStart()
    {
        ScreenTextInput_.reset();
        ConsoleInputOutput_.setTextAttrBold1();
        ConsoleInputOutput_.setTextAttrReverse1();

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
    
    static void waitStop()
    {
        waitSignState = 0;
        timer.cancel();
        ConsoleInputOutput_.setTextAttrReverse0();
        ConsoleInputOutput_.setTextAttrBold0();
    }
    
    static ConfigFile CF;
    static ConfigFile CFC;

    
    static ArrayList<String> EngineName;
    static ArrayList<Integer> EngineFav;
    
    static ArrayList<String> archFileName;
    static ArrayList<String> archFileId;
    static ArrayList<Integer> archFileNumber;
    
    static void refreshSettingText()
    {
        String fav = ";" + CF.ParamGetS("Favorite") + ";";


        int ctxSummaryMsg = 0;
        int ctxSummaryTok = 0;
        int ctxSummaryMsgUsed = 0;
        int ctxSummaryTokUsed = 0;
        ArrayList<ScreenTextDispMessage> ctxMsg = ScreenTextDisp_[workContext].textMsg;
        int contextBeginIdx = ChatEngine.contextBeginIdx(ctxMsg, CF);
        for (int i = 0; i < ctxMsg.size(); i++)
        {
            if ((!ctxMsg.get(i).ommit) && (ctxMsg.get(i).tokens > 0))
            {
                if (contextBeginIdx <= i)
                {
                    ctxSummaryMsgUsed++;
                    ctxSummaryTokUsed += ctxMsg.get(i).tokens;
                }
            }
            ctxSummaryMsg++;
            ctxSummaryTok += ctxMsg.get(i).tokens;
        }
        String ctxSummaryMsgInfo = (ctxSummaryMsgUsed + "/" + ctxSummaryMsg);
        String ctxSummaryTokInfo = (ctxSummaryTokUsed + "/" + ctxSummaryTok);

        String engineName = CF.ParamGetS("Model");
        
        ScreenTextDisp_[10].clear(true);
        ScreenTextDisp_[10].supplyLine("Current context: " + workContext + "    " + ctxSummaryMsgInfo + " messages    " + ctxSummaryTokInfo + " tokens");
        ScreenTextDisp_[10].supplyLine("Selected model: " + engineName);
        ScreenTextDisp_[10].supplyLine("");
        ScreenTextDisp_[10].supplyLine("Commands:");
        ScreenTextDisp_[10].supplyLine(" `clear` - clear the current context");
        ScreenTextDisp_[10].supplyLine(" `exit` - exit from this application");
        ScreenTextDisp_[10].supplyLine(" `resize` - repaint the interface after resize or change cell width");
        ScreenTextDisp_[10].supplyLine(" `copy` - copy the current message to edit field");
        ScreenTextDisp_[10].supplyLine(" `counterreset` - reset the token counter for the selected model");
        ScreenTextDisp_[10].supplyLine(" `archive` - archive the current context");
        ScreenTextDisp_[10].supplyLine(" `archdelete` - delete last created or restored archive");
        ScreenTextDisp_[10].supplyLine("");
        ScreenTextDisp_[10].supplyLine("Letter with number:");
        ScreenTextDisp_[10].supplyLine(" `f_` - field size: " + CF.ParamGetI("FieldSize"));
        ScreenTextDisp_[10].supplyLine(" `t_` - temperature x100 (from 0 to 200): " + CommonTools.intIsSpecified(CF.ParamGetI("Temperature"), 0, 200));
        ScreenTextDisp_[10].supplyLine(" `n_` - nucleus sampling x100 (from 0 to 100): " + CommonTools.intIsSpecified(CF.ParamGetI("TopP"), 0, 100));
        ScreenTextDisp_[10].supplyLine(" `h_` - history token limit: " + CommonTools.intLimited(CF.ParamGetI("HistoryTokens")));
        ScreenTextDisp_[10].supplyLine(" `a_` - answer token limit: " + CommonTools.intLimited(CF.ParamGetI("AnswerTokens")));
        ScreenTextDisp_[10].supplyLine(" `w_` - waiting timeout: " + CommonTools.intLimited(CF.ParamGetI("WaitTimeout")));
        ScreenTextDisp_[10].supplyLine(" `c_` - cell width in table: " + CommonTools.intLimited(CF.ParamGetI("CellWidth")));
        ScreenTextDisp_[10].supplyLine(" `l_` - log requests/responses (0, 1, 2): " + (CF.ParamGetB("Log") ? "Yes" : "No") + ((CF.ParamGetI("Log") == 2) ? ", clear" : ""));
        ScreenTextDisp_[10].supplyLine("");
        
        int favIdx = 1;
        int engineNameLength = 0;

        for (int i = 0; i < EngineName.size(); i++)
        {
            String engineNameItem = EngineName.get(i);
            if (ChatEngine.isValidEngine(engineNameItem))
            {
                engineNameLength = Math.max(engineNameLength, engineNameItem.length());
                EngineFav.set(i, -1);
            }
        }        
        
        int counterSizeH = CommonTools.counterSize;
        int counterSizeQ = CommonTools.counterSize;
        int counterSizeA = CommonTools.counterSize;
        for (int i = 0; i < EngineName.size(); i++)
        {
            String engineNameItem = EngineName.get(i);
            if (ChatEngine.isValidEngine(engineNameItem))
            {
                int numLengthH = String.valueOf(CFC.ParamGetI(engineNameItem + "-h", 0)).length();
                int numLengthQ = String.valueOf(CFC.ParamGetI(engineNameItem + "-q", 0)).length();
                int numLengthA = String.valueOf(CFC.ParamGetI(engineNameItem + "-a", 0)).length();
                counterSizeH = Math.max(counterSizeH, numLengthH);
                counterSizeQ = Math.max(counterSizeQ, numLengthQ);
                counterSizeA = Math.max(counterSizeA, numLengthA);
            }
        }
        
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
                        if (engineName.equals(engineNameItem))
                        {
                            ScreenTextDisp_[10].supply("`");
                        }
                        else
                        {
                            ScreenTextDisp_[10].supply("$$");
                        }
                        ScreenTextDisp_[10].supply(engineNameItem);
                        ScreenTextDisp_[10].supply(CommonTools.stringIndent(engineNameLength - engineNameItem.length(), ' '));
                        ScreenTextDisp_[10].supply(" " + CommonTools.intToStr(CFC.ParamGetI(engineNameItem + "-h", 0), counterSizeH));
                        ScreenTextDisp_[10].supply(" " + CommonTools.intToStr(CFC.ParamGetI(engineNameItem + "-q", 0), counterSizeQ));
                        ScreenTextDisp_[10].supply(" " + CommonTools.intToStr(CFC.ParamGetI(engineNameItem + "-a", 0), counterSizeA));
                        if (engineName.equals(engineNameItem))
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
        
        for (int i = 0; i < EngineName.size(); i++)
        {
            String engineNameItem = EngineName.get(i);
            ScreenTextDisp_[10].supply("*    ");
            if (ChatEngine.isValidEngine(engineNameItem))
            {
                if (engineName.equals(engineNameItem))
                {
                    ScreenTextDisp_[10].supply("`");
                }
                else
                {
                    ScreenTextDisp_[10].supply("$$");
                }
                ScreenTextDisp_[10].supply(engineNameItem);
                ScreenTextDisp_[10].supply(CommonTools.stringIndent(engineNameLength - engineNameItem.length(), ' '));
                ScreenTextDisp_[10].supply(" " + CommonTools.intToStr(CFC.ParamGetI(engineNameItem + "-h", 0), counterSizeH));
                ScreenTextDisp_[10].supply(" " + CommonTools.intToStr(CFC.ParamGetI(engineNameItem + "-q", 0), counterSizeQ));
                ScreenTextDisp_[10].supply(" " + CommonTools.intToStr(CFC.ParamGetI(engineNameItem + "-a", 0), counterSizeA));
                if (engineName.equals(engineNameItem))
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
    
    static boolean isStandardCommand(String cmd, ChatEngine e1, ChatEngine e2)
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
        if (cmd.equals("counterreset"))
        {
            CFC.ParamRemove(CF.ParamGetS("Model") + "-h");
            CFC.ParamRemove(CF.ParamGetS("Model") + "-q");
            CFC.ParamRemove(CF.ParamGetS("Model") + "-a");
            CFC.FileSave(CommonTools.applDir + CommonTools.counterFileName);
            refreshSettingText();
            return false;
        }
        if (cmd.equals("copy"))
        {
            ScreenTextInput_.reset();
            ScreenTextInput_.textValue = ScreenTextDisp.convMultiToSingle(ScreenTextDisp_[workContext].getCurrentMessage());
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
        if (cmd.equals("resize"))
        {
            ConsoleInputOutput_.screenClear();
            ConsoleInputOutput_.getScreenSize();
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
            
            ScreenTextDisp.displayResize(ConsoleInputOutput_.screenHeight - inputBoxHeight);
            for (int i = 0; i < (workContextCount + 1); i++)
            {
                ScreenTextDisp_[i].textWidth = ConsoleInputOutput_.screenWidth;
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
            
            return false;
        }
        /*if (cmd.equals("models"))
        {
            updateEngineList(true, true, e1, e2);
            return false;
        }*/

        return true;
    }
    
    static void updateEngineList(boolean getFromServer, boolean updateListFile, ChatEngine e1, ChatEngine e2)
    {
        waitStart();

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
        
        ArrayList<String> engines1 = e1.getEngines(getFromServer);
        for (int i = 0; i < engines1.size(); i++)
        {
            if (ChatEngine.isValidEngine(engines1.get(i)))
            {
                if (e1.testEngine(engines1.get(i)))
                {
                    if (updateListFile)
                    {
                        CommonTools.fileSaveText(CommonTools.applDir + CommonTools.modelsFileName, "1" + engines1.get(i) + "\n");
                    }
                    EngineName.add(engines1.get(i));
                    EngineFav.add(-1);
                }
            }
            else
            {
                EngineName.add(engines1.get(i));
                EngineFav.add(-1);
                engineListError = true;
            }
        }

        ArrayList<String> engines2 = e2.getEngines(getFromServer);
        for (int i = 0; i < engines2.size(); i++)
        {
            if (ChatEngine.isValidEngine(engines2.get(i)))
            {
                if (e2.testEngine(engines2.get(i)))
                {
                    if (updateListFile)
                    {
                        CommonTools.fileSaveText(CommonTools.applDir + CommonTools.modelsFileName, "2" + engines2.get(i) + "\n");
                    }
                    EngineName.add(engines2.get(i));
                    EngineFav.add(-1);
                }
            }
            else
            {
                EngineName.add(engines2.get(i));
                EngineFav.add(-1);
                engineListError = true;
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
    
    static void sendSettingsCommand(String cmd, ChatEngine e1, ChatEngine e2, boolean configSave)
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
                        CF.ParamSet("HistoryTokens", S_num);
                        if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                    }
                    break;
                case 'a':
                    if (S_num >= 0)
                    {
                        CF.ParamSet("AnswerTokens", S_num);
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
                case 'l':
                    if (S_num >= 0)
                    {
                        CF.ParamSet("Log", S_num);
                        if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                    }
                    if (S_num == 2)
                    {
                        CommonTools.fileClear(CommonTools.applDir + CommonTools.logFileName);
                    }
                    break;
                case 'c':
                    if (S_num >= 0)
                    {
                        CF.ParamSet("CellWidth", S_num);
                        if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                        for (int i = 0; i < (workContextCount + 1); i++)
                        {
                            ScreenTextDisp_[i].tableCellWidth = CF.ParamGetI("CellWidth");
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
                if (S_num >= 0)
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
                e1.setEngine(cmd);
                e2.setEngine(cmd);

                if (e1.isActive)
                {
                    e2.isActive = false;
                    CF.ParamSet("Model", e1.engineName);
                    if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                }
                else
                {
                    if (e2.isActive)
                    {
                        CF.ParamSet("Model", e2.engineName);
                        if (configSave) CF.FileSave(CommonTools.applDir + CommonTools.configFileName);
                    }
                }
                
                
                for (int i = 0; i < archFileId.size(); i++)
                {
                    if (archFileId.get(i).equals(cmd))
                    {
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

        if (CF.ParamGetS("KeyGpt").isBlank() && CF.ParamGetS("KeyGemini").isBlank())
        {
            appUnusable = true;
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
        
        try
        {
            ConsoleInputOutput_ = new ConsoleInputOutput();
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
            ScreenTextDisp_[i].tableCellWidth = CF.ParamGetI("CellWidth");
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
        
        isStandardCommand("resize", null, null);
        
        ChatEngine ChatEngineGpt_ = new ChatEngineGpt(CF, CFC);
        ChatEngine ChatEngineGemini_ = new ChatEngineGemini(CF, CFC);

        
        ArrayList<String> fileModelNames = CommonTools.fileLoadText(CommonTools.applDir + CommonTools.modelsFileName, false);
        if (fileModelNames.size() > 0)
        {
            ChatEngineGpt_.setEngineItem(null);
            ChatEngineGemini_.setEngineItem(null);
            for (int i = 0; i < fileModelNames.size(); i++)
            {
                String engineNameItem = fileModelNames.get(i);
                if ((engineNameItem.length() > 1) && (ChatEngine.isValidEngine(engineNameItem.substring(1))))
                {
                    if (engineNameItem.startsWith("1"))
                    {
                        ChatEngineGpt_.setEngineItem(engineNameItem.substring(1));
                    }
                    if (engineNameItem.startsWith("2"))
                    {
                        ChatEngineGemini_.setEngineItem(engineNameItem.substring(1));
                    }
                }
            }
            updateEngineList(false, false, ChatEngineGpt_, ChatEngineGemini_);
        }
        else
        {
            updateEngineList(true, true, ChatEngineGpt_, ChatEngineGemini_);
        }
        
        

        sendSettingsCommand(CF.ParamGetS("Model"), ChatEngineGpt_, ChatEngineGemini_, false);
        
        
        ConsoleInputOutput_.ringBell();

        
        
        
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
                            ScreenTextDisp_[ctx].ommitSwitch();
                        }
                        break;
                    case (ConsoleInputOutput.keySpecialNum + 11):
                    case (ConsoleInputOutput.keySpecialNum + 112):
                        {
                            int l = ScreenTextDisp_[ctx].getMessageLength();
                            if (l > 0)
                            {
                                ScreenTextInput_.textWrite(l + "");
                            }
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
                if (isStandardCommand(S, ChatEngineGpt_, ChatEngineGemini_))
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
                                int tokensI = 0;
                                int tokensO = 0;
                                S = ScreenTextDisp.convSingleToMulti(S_);
                                
                                ScreenTextDisp_[ctx].supplyPointSave();
                                ScreenTextDisp_[ctx].supplyLine("");
                                ScreenTextDisp_[ctx].messageIdxCounter = -1;
                                ScreenTextDisp_[ctx].supplyLine("___");
                                ScreenTextDisp_[ctx].supplyLine("");
                                ScreenTextDisp_[ctx].supplyLine(ScreenTextDisp.convPlainToMarkdown(S));
                                ScreenTextDisp_[ctx].supplyLine("");
                                ScreenTextDisp_[ctx].displayScrollDn(-1);

                                waitStart();
                                String SS = "?";
                                if (ChatEngineGpt_.isActive)
                                {
                                    SS = ChatEngineGpt_.chatTalk(ScreenTextDisp_[ctx].textMsg, S, false);
                                    tokensI = ChatEngineGpt_.tokensI;
                                    tokensO = ChatEngineGpt_.tokensO;
                                }
                                if (ChatEngineGemini_.isActive)
                                {
                                    SS = ChatEngineGemini_.chatTalk(ScreenTextDisp_[ctx].textMsg, S, false);
                                    tokensI = ChatEngineGemini_.tokensI;
                                    tokensO = ChatEngineGemini_.tokensO;
                                }
                                waitStop();

                                ScreenTextDisp_[ctx].supplyPointRestore();

                                ScreenTextDisp_[ctx].messageIdxCounter = ScreenTextDisp_[ctx].textMsg.size() - 1;
                                ScreenTextDisp_[ctx].supplyLine("");
                                ScreenTextDisp_[ctx].messageIdxCounter = ScreenTextDisp_[ctx].textMsg.size();
                                ScreenTextDisp_[ctx].textMsg.add(new ScreenTextDispMessage(false, S, tokensI));
                                ScreenTextDisp_[ctx].supplyLine("___<<<" + tokensI + "<<<___");
                                ScreenTextDisp_[ctx].supplyLine("");
                                ScreenTextDisp_[ctx].supplyLine(ScreenTextDisp.convPlainToMarkdown(S));

                                ScreenTextDisp_[ctx].supplyLine("");
                                ScreenTextDisp_[ctx].messageIdxCounter = ScreenTextDisp_[ctx].textMsg.size();
                                ScreenTextDisp_[ctx].textMsg.add(new ScreenTextDispMessage(true, SS, tokensO));
                                ScreenTextDisp_[ctx].supplyLine("___>>>" + tokensO + ">>>___");
                                ScreenTextDisp_[ctx].supplyLine("");
                                ScreenTextDisp_[ctx].supplyLine(SS);
                                ScreenTextDisp_[ctx].displayAll();
                            }
                            break;
                        case 1: // Settings
                            {
                                S = S.trim().toLowerCase();
                                sendSettingsCommand(S, ChatEngineGpt_, ChatEngineGemini_, true);
                                ScreenTextDisp_[workContextCount].displayAll();
                            }
                            break;

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
