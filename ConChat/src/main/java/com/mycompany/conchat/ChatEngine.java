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

/**
 *
 * @author xxx
 */
public class ChatEngine
{
    protected String apiKey = "";
    protected String engineName = "";
    protected int tokensI;
    protected int tokensO;
    protected String tokensE = "";
    protected ConfigFile CF;
    protected ConfigFile CFC;
    
    public boolean isActive = false;
    
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
    
    public ChatEngine(ConfigFile CF_, ConfigFile CFC_)
    {
        CFC = CFC_;
        CF = CF_;
        engineList = new ArrayList<>();
        engineName = CF_.ParamGetS("Model");
    }
    
    /**
     * Fint the first message to send as context message list within token number limit
     * @param ctx Message list
     * @param CF_ Configuration file
     * @return Index of first message to send
     */
    public static int contextBeginIdx(ArrayList<ScreenTextDispMessage> ctx, ConfigFile CF_)
    {
        int idx = 0;
        int tokenLimit = CF_.ParamGetI("HistoryLimit");
        if (tokenLimit > 0)
        {
            idx = ctx.size();
            while ((tokenLimit >= 0) && (idx > 0))
            {
                idx--;
                if (!ctx.get(idx).ommit)
                {
                    tokenLimit -= ctx.get(idx).unitLength(CF_);
                }
            }
            if (tokenLimit < 0)
            {
                idx++;
            }
        }
        return idx;
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
    
    private void saveMapToFile(Map<String, List<String>> prop)
    {
        for (Map.Entry<String, List<String>> propItem : prop.entrySet())
        {
            for (int i = 0; i < propItem.getValue().size(); i++)
            {
                CommonTools.fileSaveText(CommonTools.applDir + CommonTools.logFileName, propItem.getKey() + "=" + propItem.getValue().get(i) + "\n");
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
                CommonTools.fileSaveText(fn, ts + " - request begin\n");
                CommonTools.fileSaveText(fn, urlAddr + "\n");
                saveMapToFile(request.headers().map());
                if (!requestBody.isEmpty())
                {
                    CommonTools.fileSaveText(fn, requestBody + "\n");
                }
                CommonTools.fileSaveText(fn, ts + " - request end\n\n");
            }

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (CF.ParamGetB("Log"))
            {
                String ts = CommonTools.timeStamp();
                String fn = CommonTools.applDir + CommonTools.logFileName;
                CommonTools.fileSaveText(fn, ts + " - response begin\n");
                CommonTools.fileSaveText(fn, response.statusCode() + "\n");
                saveMapToFile(response.headers().map());
                CommonTools.fileSaveText(fn, response.body() + "\n");
                CommonTools.fileSaveText(fn, ts + " - response end\n\n");
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
                CommonTools.fileSaveText(fn, ts + " - error begin\n");
                CommonTools.fileSaveText(fn, e.getMessage() + "\n");
                CommonTools.fileSaveText(fn, ts + " - error end\n\n");
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
            engineList.add(engineName);
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
            String testStr = chatTalk(ctx, "test", true);
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
    
    public String chatTalk(ArrayList<ScreenTextDispMessage> ctx, String msg, boolean testMode)
    {
        if (engineName.isEmpty())
        {
            return "Chat model not selected";
        }
        else
        {
            return "Invalid chat model `" + engineName + "`";
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
