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
package org.apache.jetspeed.portlets.spaces;

import java.util.List;

public class SpaceChangeContext
{
	private SpaceBean space;
	private List<SpaceBean> spaces;

	public SpaceChangeContext(SpaceBean space, List<SpaceBean> spaces)
	{
		this.space = space;
		this.spaces = spaces;
	}

	public SpaceBean getSpace() {
		return space;
	}

	public void setSpace(SpaceBean space) {
		this.space = space;
	}

	public List<SpaceBean> getSpaces() {
		return spaces;
	}

	public void setSpaces(List<SpaceBean> spaces) {
		this.spaces = spaces;
	}
}
