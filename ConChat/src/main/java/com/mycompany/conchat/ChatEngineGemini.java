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
    public ChatEngineGemini(ConfigFile CF_, ConfigFile CFC_)
    {
        super(CF_, CFC_);
        apiKey = CF.ParamGetS("KeyGemini");
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
                        engineList.add(answerList.getJSONObject(i).getString("name"));
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
    public String chatTalk(ArrayList<ScreenTextDispMessage> ctx, String msg)
    {
        tokensI = 0;
        tokensO = 0;
        int ctxTokens = 0;
        JSONObject requestBody = new JSONObject();
        JSONArray messages = new JSONArray();

        JSONArray messages_;
        for (int i = contextBeginIdx(ctx); i < ctx.size(); i++)
        {
            if ((ctx.get(i).tokens > 0) && (!ctx.get(i).ommit))
            {
                ctxTokens += ctx.get(i).tokens;
                messages_ = new JSONArray();
                messages_.put(new JSONObject().put("text", ctx.get(i).message));
                messages.put(new JSONObject().put("role", ctx.get(i).isAnswer ? "model" : "user").put("parts", messages_));
            }
        }
        messages_ = new JSONArray();
        messages_.put(new JSONObject().put("text", msg));
        messages.put(new JSONObject().put("role", "user").put("parts", messages_));

        requestBody.put("contents", messages);
        
        if (workDeterminic)
        {
            JSONObject config = new JSONObject();
            config.put("temperature", ((double)CF.ParamGetI("Temperature")) / 100.0);
            config.put("topP", 1.0);
            config.put("topK", 1);
            config.put("presencePenalty", 0.0);
            config.put("frequencyPenalty", 0.0);
            if (CF.ParamGetI("AnswerTokens") > 0)
            {
                config.put("maxOutputTokens", CF.ParamGetI("AnswerTokens"));
            }
            requestBody.put("generationConfig", config);
        }
        // gemini-1.5-pro-latest
        // gemini-1.5-flash
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
                tokensI = jsonResponse.getJSONObject("usageMetadata").getInt("promptTokenCount") - ctxTokens;
                tokensO = jsonResponse.getJSONObject("usageMetadata").getInt("candidatesTokenCount");
                tokenCount(ctxTokens, tokensI, tokensO);
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
