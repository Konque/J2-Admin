/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jetspeed.security.mfa.portlets;

import java.io.Serializable;

import org.apache.jetspeed.security.User;

/**
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public class UserBean implements Serializable 
{
    private static final long serialVersionUID = 1L;
    
    // phase one
    private String username;
    private String password;
    private String captcha;    
    private User user;
    
    // phase two
    private String question;
    private String answer;
    private int questionFailureCount = 0;
    
    private boolean publicTerminal = false;
    private boolean invalidUser = false;
    private boolean hasCookie = false;
    
    // phase three
    private String passPhrase = null;
    
    // miscellaneous
    private boolean misconfigured = false;
    
    public UserBean()
    {
        reset();
    }
    
    public void reset()
    {
        username = "";
        captcha = "";
        user = null;
        question = "";
        answer = "";
        publicTerminal = false;
        invalidUser = false;
        passPhrase = "";
        hasCookie = false;
        questionFailureCount = 0;
    }
    
    public int incrementQuestionFailureCount()
    {
        return ++questionFailureCount;
    }
    
    public String getCaptcha()
    {
        return captcha;
    }
    
    public void setCaptcha(String captcha)
    {
        this.captcha = captcha;
    }
    
    public User getUser()
    {
        return user;
    }
    
    public void setUser(User user)
    {
        this.user = user;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public void setUsername(String username)
    {
        this.username = username;
    }

    
    public String getAnswer()
    {
        return answer;
    }

    
    public void setAnswer(String answer)
    {
        this.answer = answer;
    }

    
    public String getQuestion()
    {
        return question;
    }

    
    public void setQuestion(String question)
    {
        this.question = question;
    }

    
    public boolean isPublicTerminal()
    {
        return publicTerminal;
    }

    
    public void setPublicTerminal(boolean publicTerminal)
    {
        this.publicTerminal = publicTerminal;
    }

    
    public boolean isInvalidUser()
    {
        return invalidUser;
    }

    
    public void setInvalidUser(boolean invalidUser)
    {
        this.invalidUser = invalidUser;
    }

    
    // TODO: Re-read user attributes as few times as possible.
    public String getPassPhrase()
    {
        return passPhrase;
    }

    
    public void setPassPhrase(String passPhrase)
    {
        this.passPhrase = passPhrase;
    }

    
    public boolean isHasCookie()
    {
        return hasCookie;
    }

    
    public void setHasCookie(boolean hasCookie)
    {
        this.hasCookie = hasCookie;
    }

    
	public boolean isMisconfigured()
	{
		return misconfigured;
	}

	
	public void setMisconfigured(boolean misconfigured)
	{
		this.misconfigured = misconfigured;
	}

    
    public String getPassword()
    {
        return password;
    }

    
    public void setPassword(String password)
    {
        this.password = password;
    }

    
    public int getQuestionFailureCount()
    {
        return questionFailureCount;
    }

    
    public void setQuestionFailureCount(int questionFailureCount)
    {
        this.questionFailureCount = questionFailureCount;
    }
    
}