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
package org.apache.jetspeed.portlets.statistics;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.statistics.AggregateStatistics;
import org.apache.jetspeed.statistics.InvalidCriteriaException;
import org.apache.jetspeed.statistics.PortalStatistics;
import org.apache.jetspeed.statistics.StatisticsQueryCriteria;
import org.apache.portals.bridges.velocity.GenericVelocityPortlet;
import org.apache.velocity.context.Context;

/**
 * Statistics Portlet
 * 
 * @author <a href="mailto:chris@bluesunrise.com">Chris Schaefer </a>
 * @version $Id: $
 */
public class StatisticsPortlet extends GenericVelocityPortlet
{

    private PortalStatistics statistics;

    private static final String SESSION_CRITERIA = "criteria";

    private static final String SESSION_RESULTS = "results";

    private static final String SESSION_TOTALSESSIONS = "totalsessions";

    /* CLF logger */
    static final Logger logger = LoggerFactory.getLogger(StatisticsPortlet.class);

    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        PortletContext context = getPortletContext();
        statistics = (PortalStatistics) context
                .getAttribute(CommonPortletServices.CPS_PORTAL_STATISTICS);
        if (statistics == null)
                throw new PortletException(
                        "Could not get instance of portal statistics component");
    }

    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException
    {
        Context velocityContext = getContext(request);
        PortletSession session = request.getPortletSession();

        StatisticsQueryCriteria sqc = (StatisticsQueryCriteria) session
                .getAttribute(SESSION_CRITERIA);
        AggregateStatistics stats = (AggregateStatistics) session
                .getAttribute(SESSION_RESULTS);
        if (stats == null)
        {
            if (sqc == null)
            {
                // if we get here, we're on the first startup.
                sqc = statistics.createStatisticsQueryCriteria();
                sqc.setQueryType(PortalStatistics.QUERY_TYPE_PORTLET);
                sqc.setTimePeriod("1");
                sqc.setListsize("5");
                sqc.setSorttype("count");
                sqc.setSortorder("desc");
                session.setAttribute(SESSION_CRITERIA, sqc);

                try
                {
                    statistics.forceFlush();
                    stats = statistics.queryStatistics(sqc);
                } catch (InvalidCriteriaException e)
                {
                    logger.warn("unable to complete a statistics query ", e);
                }
                session.setAttribute(SESSION_RESULTS, stats);

            }
        }
        velocityContext.put(SESSION_TOTALSESSIONS, ""
                + statistics.getNumberOfLoggedInUsers());
        velocityContext.put(SESSION_RESULTS, stats);
        velocityContext.put(SESSION_CRITERIA, sqc);
        super.doView(request, response);
    }

    public void processAction(ActionRequest request,
            ActionResponse actionResponse) throws PortletException, IOException
    {
        PortletSession session = request.getPortletSession();
        StatisticsQueryCriteria criteria = statistics.createStatisticsQueryCriteria();
        
        String user = request.getParameter("user");
        criteria.setUser(user);
        String timeperiod = request.getParameter("timeperiod");
        if (timeperiod == null)
        {
            timeperiod = "all";
        }
        
        String listsizeStr = request.getParameter("listsize");
        if(listsizeStr == null) 
        {
            listsizeStr = "5";
        } else 
        {
           try
            {
                Integer.parseInt(listsizeStr);
            } catch (NumberFormatException e)
            {
                // if we can't parse it.. just make it 5
                listsizeStr = "5";
            }
        }
        criteria.setListsize(listsizeStr);
        criteria.setSorttype("count");
        criteria.setSortorder("desc");

        criteria.setTimePeriod(timeperiod);
        String queryType = request.getParameter("queryType");

        criteria.setQueryType(queryType);
        AggregateStatistics stats = statistics.getDefaultEmptyAggregateStatistics();
        try
        {
            statistics.forceFlush();
            stats = statistics.queryStatistics(criteria);
        } catch (InvalidCriteriaException e)
        {
            logger.warn("unable to complete a statistics query ", e);
        }
        // save this to session for later display/edit
        session.setAttribute(SESSION_CRITERIA, criteria);
        session.setAttribute(SESSION_RESULTS, stats);

    }

}
