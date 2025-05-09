/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conchat;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 *
 * @author xxx
 */
class TalkObject
{
    public Runnable talkThread;
    Future<?> talkThread_;
    public boolean talkError = false;
    
    public ArrayList<ScreenTextDispMessage> ctx;
    public String ctxModel;
    public StringUTF question;
    public StringUTF answer;

    public String engineName;
    public int tokensI = 0;
    public int tokensO = 0;
    public String tokensE = "";

    private ChatEngine e1;
    private ChatEngine e2;
    private ChatEngine e3;
    private ChatEngine ex;
    
    private ChatEngine eWork;
    
    TalkObject(String engineName_, ChatEngine e1_, ChatEngine e2_, ChatEngine e3_, ChatEngine ex_)
    {
        engineName = engineName_;
        e1 = e1_;
        e2 = e2_;
        e3 = e3_;
        ex = ex_;
        eWork = new ChatEngine(ex_);
    }
    
    void talkPrepare(ArrayList<ScreenTextDispMessage> ctx_, String ctxModel_, StringUTF question_)
    {
        talkError = false;
        ctx = ctx_;
        ctxModel = ctxModel_;
        question = question_.clone();
        answer = new StringUTF("?");
        if (e1.isActive)
        {
            if (eWork.objType == e1.objType)
            {
                eWork.CloneObj(e1);
            }
            else
            {
                eWork = new ChatEngineGpt(e1);
            }
        }
        if (e2.isActive)
        {
            if (eWork.objType == e2.objType)
            {
                eWork.CloneObj(e2);
            }
            else
            {
                eWork = new ChatEngineGemini(e2);
            }
        }
        if (e3.isActive)
        {
            if (eWork.objType == e3.objType)
            {
                eWork.CloneObj(e3);
            }
            else
            {
                eWork = new ChatEngineClaude(e3);
            }
        }
        if (ex.isActive)
        {
            if (eWork.objType == ex.objType)
            {
                eWork.CloneObj(ex);
            }
            else
            {
                eWork = new ChatEngine(ex);
            }
        }
    }
    
    void talk()
    {
        answer = new StringUTF(eWork.chatTalk(ctx, ctxModel, question.get(), false));
        tokensI = eWork.tokensI;
        tokensO = eWork.tokensO;
        tokensE = eWork.tokensE;
    }
    
    void talkThreadStart(ExecutorService ExecutorService_)
    {
        talkThread = () -> talk();
        talkThread_ = ExecutorService_.submit(talkThread);
    }
    
    void talkThreadWait()
    {
        talkError = false;
        try
        {
            talkThread_.get();
        }
        catch (Exception e)
        {
            talkError = true;
        }
    }
}
