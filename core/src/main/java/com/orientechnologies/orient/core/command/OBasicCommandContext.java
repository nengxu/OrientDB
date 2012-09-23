/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.core.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic implementation of OCommandContext interface that stores variables in a map.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * 
 */
public class OBasicCommandContext implements OCommandContext {
  private boolean               recordMetrics = false;
  private List<OCommandContext> inherited;
  private Map<String, Object>   variables;

  public Object getVariable(final String iName) {
    Object result = null;

    if (inherited != null && !inherited.isEmpty())
      for (OCommandContext in : inherited) {
        result = in.getVariable(iName);
        if (result != null)
          break;
      }

    if (variables != null && variables.containsKey(iName))
      result = variables.get(iName);

    return result;
  }

  public void setVariable(final String iName, final Object iValue) {
    init();
    variables.put(iName, iValue);
  }

  public long updateMetric(final String iName, final long iValue) {
    if (!recordMetrics)
      return -1;

    init();
    Long value = (Long) variables.get(iName);
    if (value == null)
      value = iValue;
    else
      value = new Long(value.longValue() + iValue);
    variables.put(iName, value);
    return value.longValue();
  }

  /**
   * Returns a read-only map with all the variables.
   */
  public Map<String, Object> getVariables() {
    final HashMap<String, Object> map = new HashMap<String, Object>();
    if (inherited != null && !inherited.isEmpty())
      for (OCommandContext in : inherited)
        map.putAll(in.getVariables());

    if (variables != null)
      map.putAll(variables);
    return map;
  }

  /**
   * Set the inherited context avoiding to copy all the values every time.
   * 
   * @return
   */
  public OCommandContext merge(final OCommandContext iContext) {
    if (iContext == null)
      return this;

    if (inherited != null) {
      if (!inherited.contains(iContext))
        inherited.add(iContext);
    } else {
      inherited = new ArrayList<OCommandContext>();
      inherited.add(iContext);
    }

    return this;
  }

  @Override
  public String toString() {
    return getVariables().toString();
  }

  private void init() {
    if (variables == null)
      variables = new HashMap<String, Object>();
  }

  public boolean isRecordingMetrics() {
    return recordMetrics;
  }

  public void setRecordingMetrics(boolean recordMetrics) {
    this.recordMetrics = recordMetrics;
  }
}
