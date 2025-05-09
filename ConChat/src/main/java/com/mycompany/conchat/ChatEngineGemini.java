/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conchat;

import java.util.ArrayList;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author xxx
 */
public class ChatEngineGemini extends ChatEngine
{
    public ChatEngineGemini(ChatEngine __)
    {
        super(__.CF, __.CFC);
        apiKey = CF.ParamGetS("KeyGemini");
        objType = 2;
        CloneObj(__);
    }

    public ChatEngineGemini(ConfigFile CF_, ConfigFile CFC_)
    {
        super(CF_, CFC_);
        apiKey = CF.ParamGetS("KeyGemini");
        objType = 2;
    }

    @Override
    public ArrayList<String> getEngines(boolean download)
    {
        if (download)
        {
            engineList.clear();
            if (!apiKey.isBlank())
            {
                String response = webRequest("https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey, "", "");
                if (!response.startsWith("ERROR"))
                {
                    JSONArray answerList = (new JSONObject(response)).getJSONArray("models");
                    for (int i = 0; i < answerList.length(); i++)
                    {
                        engineList.add(CommonTools.modelNameBlankCharRemove(answerList.getJSONObject(i).getString("name")));
                        if (engineList.get(i).startsWith("models/")) engineList.set(i, engineList.get(i).substring(7));
                    }
                    Collections.sort(engineList);
                }
                else
                {
                    String[] response_ = response.split("\n");
                    for (int i = 0; i < response_.length; i++)
                    {
                        engineList.add("gemini-error-" + CommonTools.intToStr(i, 3) + "    " + response_[i]);
                    }
                }
            }
        }
        return engineList;
    }
    
    @Override
    public String chatTalk(ArrayList<ScreenTextDispMessage> ctx, String ctxModel, String msg, boolean testMode)
    {
        tokensI = 0;
        tokensO = 0;
        tokensE = engineName;
        int ctxTokens = 0;
        JSONObject requestBody = new JSONObject();
        JSONArray messages = new JSONArray();

        JSONArray messages_;
        for (int i = contextBeginIdx(ctx, ctxModel, CF, false); i < ctx.size(); i++)
        {
            if (ctxMatch(ctx.get(i), ctxModel))
            {
                ctxTokens += ctx.get(i).tokens;
                messages_ = new JSONArray();
                messages_.put(new JSONObject().put("text", ctx.get(i).message.get()));
                messages.put(new JSONObject().put("role", ctx.get(i).isAnswer ? "model" : "user").put("parts", messages_));
            }
        }
        messages_ = new JSONArray();
        messages_.put(new JSONObject().put("text", msg));
        messages.put(new JSONObject().put("role", "user").put("parts", messages_));

        requestBody.put("contents", messages);

        if ((!testMode) && (!engineHint.isEmpty()))
        {
            messages_ = new JSONArray();
            messages_.put(new JSONObject().put("text", engineHint));
            requestBody.put("systemInstruction", new JSONObject().put("parts", messages_));
        }        
        
        JSONObject config = null;
        if ((!testMode) && CommonTools.isWithinRange(CF.ParamGetI("Temperature"), 0, 200))
        {
            if (config == null) config = new JSONObject();
            config.put("temperature", ((double)CF.ParamGetI("Temperature")) / 100.0);
        }
        if ((!testMode) && CommonTools.isWithinRange(CF.ParamGetI("TopP"), 0, 100))
        {
            if (config == null) config = new JSONObject();
            config.put("topP", ((double)CF.ParamGetI("TopP")) / 100.0);
        }
        if ((!testMode) && (CF.ParamGetI("TopK") >= 1))
        {
            if (config == null) config = new JSONObject();
            config.put("topK", CF.ParamGetI("TopK"));
        }
        if ((!testMode) && CommonTools.isWithinRange(CF.ParamGetI("PresencePenalty"), 0, 100))
        {
            if (config == null) config = new JSONObject();
            config.put("presencePenalty", ((double)CF.ParamGetI("PresencePenalty")) / 100.0);
        }
        if ((!testMode) && CommonTools.isWithinRange(CF.ParamGetI("FrequencyPenalty"), 0, 100))
        {
            if (config == null) config = new JSONObject();
            config.put("frequencyPenalty", ((double)CF.ParamGetI("FrequencyPenalty")) / 100.0);
        }
        if ((!testMode) && (CF.ParamGetI("AnswerLimit") > 0))
        {
            if (config == null) config = new JSONObject();
            config.put("maxOutputTokens", CF.ParamGetI("AnswerLimit"));
        }
        if (config != null)
        {
            requestBody.put("generationConfig", config);
        }

        String response = webRequest("https://generativelanguage.googleapis.com/v1beta/models/" + engineName + ":generateContent?key=" + apiKey, "", requestBody.toString());
        if (!response.startsWith("ERROR"))
        {
            try
            {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray answerObj1 = jsonResponse.getJSONArray("candidates");
                StringBuilder answer_ = new StringBuilder();
                for (int i = 0; i < answerObj1.length(); i++)
                {
                    JSONArray answerObj2 = answerObj1.getJSONObject(i).getJSONObject("content").getJSONArray("parts");
                    for (int ii = 0; ii < answerObj2.length(); ii++)
                    {
                        answer_.append(answerObj2.getJSONObject(ii).getString("text"));
                    }
                }
                String answer = answer_.toString().trim();
                tokensI = jsonResponse.getJSONObject("usageMetadata").getInt("promptTokenCount");
                tokensO = jsonResponse.getJSONObject("usageMetadata").getInt("candidatesTokenCount");
                tokenCount(tokensI, tokensO, testMode);
                tokensI -= ctxTokens;
                if (tokensI < 1) tokensI = 1;
                return answer;
            }
            catch (Exception e)
            {
                tokensI = 0;
                tokensO = 0;
                return ScreenTextDisp.convMessageToMarkdown(e.getMessage() + "\n" + CommonTools.jsonFormat(response));
            }
        }
        else
        {
            return ScreenTextDisp.convMessageToMarkdown(response);
        }
    }
}
