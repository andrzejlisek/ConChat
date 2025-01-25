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
public class ChatEngineGpt extends ChatEngine
{
    String chatSystemRole = "";

    public ChatEngineGpt(ConfigFile CF_, ConfigFile CFC_)
    {
        super(CF_, CFC_);
        apiKey = CF.ParamGetS("KeyGpt");
    }
    
    @Override
    public ArrayList<String> getEngines(boolean download)
    {
        if (download)
        {
            engineList.clear();
            if (!apiKey.isBlank())
            {
                String response = webRequest("https://api.openai.com/v1/models", apiKey, "");
                if (!response.startsWith("ERROR"))
                {
                    JSONObject jsonResponse = new JSONObject(response);
                    int L = jsonResponse.getJSONArray("data").length();
                    for (int i = 0; i < L; i++)
                    {
                        engineList.add(jsonResponse.getJSONArray("data").getJSONObject(i).getString("id"));
                    }
                    Collections.sort(engineList);
                }
                else
                {
                    String[] response_ = response.split("\n");
                    for (int i = 0; i < response_.length; i++)
                    {
                        engineList.add("gpt-error-" + CommonTools.intToStr(i, 3) + "    " + response_[i]);
                    }
                }
            }
        }
        return engineList;
    }
    
    @Override
    public String chatTalk(ArrayList<ScreenTextDispMessage> ctx, String msg, boolean testMode)
    {
        tokensI = 0;
        tokensO = 0;
        tokensE = engineName;
        int ctxTokens = 0;
        String apiEndpoint = "https://api.openai.com/v1/chat/completions";

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", engineName);
        if ((!testMode) && CommonTools.isWithinRange(CF.ParamGetI("Temperature"), 0, 200))
        {
            requestBody.put("temperature", ((double)CF.ParamGetI("Temperature")) / 100.0);       // 1.0   0.0 - 2.0
        }
        if ((!testMode) && CommonTools.isWithinRange(CF.ParamGetI("TopP"), 0, 100))
        {
            requestBody.put("top_p", ((double)CF.ParamGetI("TopP")) / 100.0);             // 1.0   0.0 - 1.0
        }
        if ((!testMode) && CommonTools.isWithinRange(CF.ParamGetI("FrequencyPenalty"), 0, 100))
        {
            requestBody.put("frequency_penalty", ((double)CF.ParamGetI("FrequencyPenalty")) / 100.0); // 0.0   0.0 - 2.0
        }
        if ((!testMode) && CommonTools.isWithinRange(CF.ParamGetI("PresencePenalty"), 0, 100))
        {
            requestBody.put("presence_penalty", ((double)CF.ParamGetI("PresencePenalty")) / 100.0);  // 0.0   0.0 - 2.0
        }

        JSONArray messages = new JSONArray();
        if (!chatSystemRole.isEmpty())
        {
            messages.put(new JSONObject().put("role", "system").put("content", chatSystemRole));
        }
        for (int i = contextBeginIdx(ctx, CF); i < ctx.size(); i++)
        {
            if ((ctx.get(i).tokens > 0) && (!ctx.get(i).ommit))
            {
                ctxTokens += ctx.get(i).tokens;
                messages.put(new JSONObject().put("role", ctx.get(i).isAnswer ? "assistant" : "user").put("content", ctx.get(i).message));
            }
        }
        messages.put(new JSONObject().put("role", "user").put("content", msg));
        requestBody.put("messages", messages);
        if ((!testMode) && (CF.ParamGetI("AnswerTokens") > 0))
        {
            requestBody.put("max_tokens", CF.ParamGetI("AnswerTokens"));
        }

        String response = webRequest(apiEndpoint, apiKey, requestBody.toString());

        if (!response.startsWith("ERROR"))
        {
            try
            {
                JSONObject jsonResponse = new JSONObject(response);
                String answer = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim();
                tokensI = jsonResponse.getJSONObject("usage").getInt("prompt_tokens") - ctxTokens;
                if (tokensI < 1) tokensI = 1;
                tokensO = jsonResponse.getJSONObject("usage").getInt("completion_tokens");
                tokenCount(ctxTokens, tokensI, tokensO, testMode);
                return answer;
            }
            catch (Exception ee)
            {
                tokensI = 0;
                tokensO = 0;
                return ScreenTextDisp.convMessageToMarkdown(ee.getMessage() + "\n" + CommonTools.jsonFormat(response));
            }
        }
        else
        {
            return ScreenTextDisp.convMessageToMarkdown(response);
        }
    }
}
