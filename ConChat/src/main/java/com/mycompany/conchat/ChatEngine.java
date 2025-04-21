/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conchat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author xxx
 */
public class ChatEngine
{
    protected String apiKey = "";
    protected String engineName = "";
    protected String engineHint = "";
    protected int tokensI;
    protected int tokensO;
    protected String tokensE = "";
    protected ConfigFile CF;
    protected ConfigFile CFC;
    
    public boolean isActive = false;
    
    public int objType = 0;

    private static Random Random_;
    
    /**
     * List of available engines/models
     */
    protected ArrayList<String> engineList;
    
    /**
     * Add token number to token counters
     * @param I Input tokens
     * @param O Output tokens
     */
    void tokenCount(int I, int O, boolean talkTestMode)
    {
        if (talkTestMode)
        {
            return;
        }
        CFC.ParamSet(engineName + "-i", CFC.ParamGetI(engineName + "-i") + I);
        CFC.ParamSet(engineName + "-o", CFC.ParamGetI(engineName + "-o") + O);
        CFC.FileSave(CommonTools.applDir + CommonTools.counterFileName);
    }
    
    public final void CloneObj(ChatEngine __)
    {
        apiKey = __.apiKey;
        engineName = __.engineName;
        engineHint = __.engineHint;
        tokensI = __.tokensI;
        tokensO = __.tokensO;
        tokensE = __.tokensE;
        CF = __.CF;
        CFC = __.CFC;
        isActive = __.isActive;
        engineList.clear();
        for (int i = 0; i < __.engineList.size(); i++)
        {
            engineList.add(__.engineList.get(i));
        }
    }
    
    public ChatEngine(ConfigFile CF_, ConfigFile CFC_)
    {
        CFC = CFC_;
        CF = CF_;
        engineList = new ArrayList<>();
        engineName = CF_.ParamGetS("Model");
        engineList.clear();
        objType = 0;
    }

    public ChatEngine(ChatEngine __)
    {
        CFC = __.CFC;
        CF = __.CF;
        engineList = new ArrayList<>();
        engineName = CF.ParamGetS("Model");
        engineList.clear();
        objType = 0;
        CloneObj(__);
    }

    private static ArrayList<String> contextBeginIdxModelList;
    private static String contextBeginIdxModelListStr = "~~null~~";
    
    /**
     * Fint the first message to send as context message list within token number limit
     * @param ctx Message list
     * @param ctxModel
     * @param CF_ Configuration file
     * @param partial
     * @return Index of first message to send
     */
    public static int contextBeginIdx(ArrayList<ScreenTextDispMessage> ctx, String ctxModel, ConfigFile CF_, boolean partial)
    {
        if ((contextBeginIdxModelList == null) || (!contextBeginIdxModelListStr.equals(ctxModel)))
        {
            if (contextBeginIdxModelList == null)
            {
                contextBeginIdxModelList = new ArrayList<>();
            }
            else
            {
                contextBeginIdxModelList.clear();
            }
            String[] ctxModel_ = ctxModel.split(CommonTools.splitterInfoS);
            for (int I = 0; I < ctxModel_.length; I++)
            {
                contextBeginIdxModelList.add(ctxModel_[I]);
            }
        }
        contextBeginIdxModelListStr = ctxModel;
        int idx_ = partial ? (contextBeginIdxModelList.size()) : 0;
        for (int I = 0; I < contextBeginIdxModelList.size(); I++)
        {
            int idx = 0;
            int tokenLimit = CF_.ParamGetI("HistoryLimit");
            if (tokenLimit > 0)
            {
                idx = ctx.size();
                while ((tokenLimit >= 0) && (idx > 0))
                {
                    idx--;
                    if (ctxMatch(ctx.get(idx), contextBeginIdxModelList.get(I)))
                    {
                        tokenLimit -= ctx.get(idx).unitLength(CF_);
                    }
                }
                if (tokenLimit < 0)
                {
                    idx++;
                }
            }
            if (partial)
            {
                idx_ = Math.min(idx_, idx);
            }
            else
            {
                idx_ = Math.max(idx_, idx);
            }
        }
        return idx_;
    }
    
    /**
     * Set the engine by name
     * @param engineName_ Engine name
     * @return True if engine is set successfully
     */
    public boolean setEngine(String engineName_)
    {
        for (int i = 0; i < engineList.size(); i++)
        {
            if (engineList.get(i).equalsIgnoreCase(engineName_))
            {
                engineName = engineList.get(i);
                isActive = true;
                return true;
            }
        }
        isActive = false;
        return false;
    }
    
    public void setHint(String engineHint_)
    {
        engineHint = engineHint_.trim();
    }
    
    private void saveMapToFile(Map<String, List<String>> prop, StringBuilder logSB)
    {
        for (Map.Entry<String, List<String>> propItem : prop.entrySet())
        {
            for (int i = 0; i < propItem.getValue().size(); i++)
            {
                logSB.append(propItem.getKey() + "=" + propItem.getValue().get(i) + "\n");
            }
        }
    }
    
    public String webRequest(String urlAddr, String headerParameters, String requestBody)
    {
        try
        {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
            requestBuilder = requestBuilder.uri(URI.create(urlAddr));
            requestBuilder = requestBuilder.header("Content-Type", "application/json");
            
            
            if (!headerParameters.isEmpty())
            {
                char headerSplitter = headerParameters.charAt(headerParameters.length() - 1);
                int headerStep = 0;
                while (headerStep < headerParameters.length())
                {
                    int header1 = headerParameters.indexOf(headerSplitter, headerStep);
                    int header2 = headerParameters.indexOf(headerSplitter, header1 + 1);
                    if ((header1 > headerStep) && (header2 > header1))
                    {
                        String headerKey = headerParameters.substring(headerStep, header1);
                        String headerVal = headerParameters.substring(header1 + 1, header2);
                        requestBuilder = requestBuilder.header(headerKey, headerVal);
                        headerStep = header2 + 1;
                    }
                    else
                    {
                        headerStep = headerParameters.length();
                    }
                }
            }

            
            if (requestBody.isEmpty())
            {
                requestBuilder = requestBuilder.GET();
            }
            else
            {
                requestBuilder = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestBody));
            }
            if (CF.ParamGetI("WaitTimeout") > 0)
            {
                requestBuilder.timeout(Duration.ofSeconds(CF.ParamGetI("WaitTimeout")));
            }
            HttpRequest request = requestBuilder.build();

            if (CF.ParamGetB("Log"))
            {
                String ts = CommonTools.timeStamp();
                String fn = CommonTools.applDir + CommonTools.logFileName;
                StringBuilder logSB = new StringBuilder();
                logSB.append(ts + " - request begin\n");
                logSB.append(urlAddr + "\n");
                saveMapToFile(request.headers().map(), logSB);
                if (!requestBody.isEmpty())
                {
                    logSB.append(requestBody + "\n");
                }
                logSB.append(ts + " - request end\n\n");
                CommonTools.fileSaveText(fn, logSB.toString());
            }

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (CF.ParamGetB("Log"))
            {
                String ts = CommonTools.timeStamp();
                String fn = CommonTools.applDir + CommonTools.logFileName;
                StringBuilder logSB = new StringBuilder();
                logSB.append(ts + " - response begin\n");
                logSB.append(response.statusCode() + "\n");
                saveMapToFile(response.headers().map(), logSB);
                logSB.append(response.body() + "\n");
                logSB.append(ts + " - response end\n\n");
                CommonTools.fileSaveText(fn, logSB.toString());
            }
            
            if (response.statusCode() == 200)
            {
                return response.body();
            }
            else
            {
                return "ERROR\n" + response.statusCode() + "\n" + CommonTools.jsonFormat(response.body());
            }

        }
        catch (Exception e)
        {
            if (CF.ParamGetB("Log"))
            {
                String ts = CommonTools.timeStamp();
                String fn = CommonTools.applDir + CommonTools.logFileName;
                StringBuilder logSB = new StringBuilder();
                logSB.append(ts + " - error begin\n");
                logSB.append(e.getMessage() + "\n");
                logSB.append(ts + " - error end\n\n");
                CommonTools.fileSaveText(fn, logSB.toString());
            }

            return "ERROR\n" + e.getMessage();
        }
    }

    public void setEngineItem(String engineName)
    {
        if (engineName == null)
        {
            engineList.clear();
        }
        else
        {
            engineList.add(CommonTools.modelNameBlankCharRemove(engineName));
        }
    }
    
    public boolean testEngine(String testEngineName)
    {
        String testMsg = CF.ParamGetS("TestModel").trim();
        if (testMsg.isBlank())
        {
            return true;
        }
        else
        {
            String engineName_ = engineName;
            engineName = testEngineName;
            ArrayList<ScreenTextDispMessage> ctx = new ArrayList<>();
            String testStr = chatTalk(ctx, "", "test", true);
            if (testStr.startsWith("```ERROR"))
            {
                engineName = engineName_;
                return false;
            }
            else
            {
                engineName = engineName_;
                return true;
            }
        }
    }
    
    public static boolean isValidEngine(String engineNameItem)
    {
        if (engineNameItem.contains("-error-")) return false;
        return true;
    }
    
    public ArrayList<String> getEngines(boolean download)
    {
        return engineList;
    }

    public static int ctxMatchBulk(ScreenTextDispMessage ctxItem, String ctxModel)
    {
        if (ctxModel.contains(CommonTools.splitterInfoS))
        {
            String[] ctxModel_ = ctxModel.split(CommonTools.splitterInfoS);
            int match0 = 0;
            int match1 = 0;
            for (int i = 0; i < ctxModel_.length; i++)
            {
                if (ctxMatch(ctxItem, ctxModel_[i]))
                {
                    match1++;
                }
                else
                {
                    match0++;
                }
            }
            if ((match1 > 0) && (match0 == 0)) return 2;
            if ((match1 == 0) && (match0 > 0)) return 0;
            return 1;
        }
        else
        {
            if (ctxMatch(ctxItem, ctxModel))
            {
                return 2;
            }
            else
            {
                return 0;
            }
        }
    }
    
    protected static boolean ctxMatch(ScreenTextDispMessage ctxItem, String ctxModel)
    {
        if ((ctxItem.tokens > 0) && (!ctxItem.ommit))
        {
            if (ctxModel.isEmpty())
            {
                return true;
            }
            String modelList_ = "," + ctxItem.model + ",";
            if (modelList_.contains("," + ctxModel + ","))
            {
                return true;
            }
        }
        return false;
    }
    
    public String chatTalk(ArrayList<ScreenTextDispMessage> ctx, String ctxModel, String msg, boolean testMode)
    {
        if (Random_ == null)
        {
            Random_ = new Random();
        }
        
        tokensE = engineName;
        if (engineList.contains(engineName))
        {
            try
            {
                Thread.sleep(5000);
            }
            catch (InterruptedException ex)
            {
            }
            
            String answer = (engineHint.isEmpty() ? "" : "{" + engineHint + "}") + "" + Random_.nextLong() + "";
            int ctxTokens = 0;
            if (CF.ParamGetB("Log"))
            {
                String ts = CommonTools.timeStamp();
                String fn = CommonTools.applDir + CommonTools.logFileName;
                StringBuilder logSB = new StringBuilder();
                logSB.append(ts + " - dummy begin\n");
                logSB.append("Model: " + engineName + "\n");
                logSB.append("Hint: " + engineHint + "\n");
                logSB.append("Match: " + ctxModel + "\n");
                for (int i = contextBeginIdx(ctx, ctxModel, CF, false); i < ctx.size(); i++)
                {
                    if (ctxMatch(ctx.get(i), ctxModel))
                    {
                        ctxTokens += ctx.get(i).tokens;
                        String ctxItem = (ctx.get(i).isAnswer ? ">>>: " : "<<<: ");
                        String answerPart = CommonTools.stringReplaceMultiply(ctx.get(i).message, "\n", " ");
                        if (answerPart.length() > 50)
                        {
                            answerPart = answerPart.substring(answerPart.length() - 50);
                        }
                        ctxItem = ctxItem + answerPart;
                        logSB.append("Context " + ctxItem + "\n");
                    }
                }
                logSB.append("Question<<<: " + msg + "\n");
                logSB.append("Answer  >>>: " + answer + "\n");
                logSB.append(ts + " - dummy end\n\n");
                CommonTools.fileSaveText(fn, logSB.toString());
            }
            tokensI = ctxTokens + ((msg.length() + 5) / 5);
            tokensO = ((answer.length() + 5) / 5);
            tokenCount(tokensI, tokensO, testMode);
            tokensI -= ctxTokens;
            if (tokensI < 1) tokensI = 1;
            return answer;
        }
        else
        {
            tokensI = 0;
            tokensO = 0;
            return "? " + engineName + " ?";
        }
    }
    
    public static String contextFile(String fileName)
    {
        if (!fileName.startsWith("context")) return "";
        if (!fileName.endsWith(".txt")) return "";
        for (int i = 0; i < 10; i++)
        {
            if (fileName.equals("context" + i + ".txt")) return "";
        }
        return fileName.substring(7, fileName.length() - 4);
    }
}
