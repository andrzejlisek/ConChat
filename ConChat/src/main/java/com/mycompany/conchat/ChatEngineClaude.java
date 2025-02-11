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
public class ChatEngineClaude extends ChatEngine
{
    String chatSystemRole = "";

    public ChatEngineClaude(ConfigFile CF_, ConfigFile CFC_)
    {
        super(CF_, CFC_);
        apiKey = CF.ParamGetS("KeyClaude");
    }

    @Override
    public ArrayList<String> getEngines(boolean download)
    {
        if (download)
        {
            engineList.clear();
            if (!apiKey.isBlank())
            {
                String response = webRequest("https://api.anthropic.com/v1/models", "anthropic-version|2023-06-01|X-API-Key|" + apiKey + "|", "");
                if (!response.startsWith("ERROR"))
                {
                    JSONArray answerList = (new JSONObject(response)).getJSONArray("data");
                    for (int i = 0; i < answerList.length(); i++)
                    {
                        engineList.add(answerList.getJSONObject(i).getString("id"));
                    }
                    Collections.sort(engineList);
                }
                else
                {
                    String[] response_ = response.split("\n");
                    for (int i = 0; i < response_.length; i++)
                    {
                        engineList.add("claude-error-" + CommonTools.intToStr(i, 3) + "    " + response_[i]);
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
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", engineName);
        if ((!testMode) && CommonTools.isWithinRange(CF.ParamGetI("Temperature"), 0, 200))
        {
            requestBody.put("temperature", ((double)CF.ParamGetI("Temperature")) / 100.0);
        }
        if ((!testMode) && CommonTools.isWithinRange(CF.ParamGetI("TopP"), 0, 100))
        {
            requestBody.put("top_p", ((double)CF.ParamGetI("TopP")) / 100.0);
        }
        if ((!testMode) && (CF.ParamGetI("TopK") >= 1))
        {
            requestBody.put("top_k", CF.ParamGetI("TopK"));
        }
        if ((!testMode) && (CF.ParamGetI("AnswerLimit") > 0))
        {
            requestBody.put("max_tokens", CF.ParamGetI("AnswerLimit"));
        }
        else
        {
            // HARDCODING The max_tokens is required parameter
            if (engineName.contains("claude-3-5-"))
            {
                requestBody.put("max_tokens", 8192);
            }
            else
            {
                if (engineName.contains("claude-3-"))
                {
                    requestBody.put("max_tokens", 4096);
                }
            }
        }
        if (!chatSystemRole.isEmpty())
        {
            requestBody.put("system", chatSystemRole);
        }

        JSONArray messages = new JSONArray();
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

        String response = webRequest("https://api.anthropic.com/v1/messages", "anthropic-version|2023-06-01|X-API-Key|" + apiKey + "|", requestBody.toString());
        if (!response.startsWith("ERROR"))
        {
            try
            {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray answerObj = jsonResponse.getJSONArray("content");
                StringBuilder answer_ = new StringBuilder();
                for (int i = 0; i < answerObj.length(); i++)
                {
                    answer_.append(answerObj.getJSONObject(i).getString("text"));
                }
                String answer = answer_.toString().trim();
                tokensI = jsonResponse.getJSONObject("usage").getInt("input_tokens");
                tokensO = jsonResponse.getJSONObject("usage").getInt("output_tokens");
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
