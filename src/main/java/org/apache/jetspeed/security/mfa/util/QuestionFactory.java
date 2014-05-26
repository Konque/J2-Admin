/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jetspeed.security.mfa.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.jetspeed.security.mfa.SecurityQuestionBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestionFactory 
{
    
    static final Logger logger = LoggerFactory.getLogger(QuestionFactory.class);
    
	static private Random rand;
	
	private List<String> questions;
	
	public QuestionFactory( String source )
	{
		rand = new Random();
		
        // Read random questions.
        if ( source.charAt( source.length() - 1 ) == '?' )
        	source = source.substring(0, source.length() - 1);
        
	    try {
	        String[] questionTokens = null;
	        questionTokens = source.split("\\?");
	        questions = new ArrayList<String>();
	        for (int i=0; i < questionTokens.length; i++)
	        	questions.add( questionTokens[i].trim() + "?" );
	        
	        // System.out.println("Can now present invalid users with any of " + questions.size() + " random questions.");
        }
        catch (Throwable e)
        {
        	logger.error( "Unable to parse random questions: {}", e.toString());
        }
	}
	
	public String getRandomQuestion()
	{
		return (String)questions.get( rand.nextInt( questions.size() ) );
	}
	
	public List<String> getAllQuestions()
	{
		return questions;
	}
	
	public List<String> getAllQuestionsInRandomOrder()
	{
		List<String> result = new ArrayList<String>( questions.size() );
		
		for (int i=0; i<questions.size(); i++)
			result.add( questions.get(i) );
		
		for (int i=0; i<result.size(); i++)
		{
			int j = rand.nextInt( result.size() );
			String temp = result.get(i);
			result.set(i, result.get(j) );
			result.set(j, temp);
		}
		
		return result;
	}
	
	public SecurityQuestionBean getSecurityQuestionBean()
	{
		SecurityQuestionBean result = new SecurityQuestionBean();
		List<String> source = getAllQuestionsInRandomOrder();
		
		result.setQuestion1( source.get(1) );
		result.setQuestion2( source.get(2) );
		result.setQuestion3( source.get(3) );
		
		return result;
	}
}
