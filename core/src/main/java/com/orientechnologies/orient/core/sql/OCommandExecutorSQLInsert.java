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
package com.orientechnologies.orient.core.sql;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.orientechnologies.orient.core.command.OCommandDistributedConditionalReplicateRequest;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.command.OCommandRequestText;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.security.ODatabaseSecurityResources;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.serializer.OStringSerializerHelper;

/**
 * SQL INSERT command.
 * 
 * @author Luca Garulli
 * @author Johann Sorel (Geomatys)
 */
public class OCommandExecutorSQLInsert extends OCommandExecutorSQLSetAware implements
    OCommandDistributedConditionalReplicateRequest {
  public static final String        KEYWORD_INSERT = "INSERT";
  private static final String       KEYWORD_VALUES = "VALUES";
  private String                    className      = null;
  private String                    clusterName    = null;
  private String                    indexName      = null;
  private List<Map<String, Object>> newRecords;

  @SuppressWarnings("unchecked")
  public OCommandExecutorSQLInsert parse(final OCommandRequest iRequest) {
    final ODatabaseRecord database = getDatabase();
    database.checkSecurity(ODatabaseSecurityResources.COMMAND, ORole.PERMISSION_READ);

    init(((OCommandRequestText) iRequest).getText());

    className = null;
    newRecords = null;

    parserRequiredKeyword("INSERT");
    parserRequiredKeyword("INTO");

    String subjectName = parserRequiredWord(true, "Invalid subject name. Expected cluster, class or index");
    if (subjectName.startsWith(OCommandExecutorSQLAbstract.CLUSTER_PREFIX))
      // CLUSTER
      clusterName = subjectName.substring(OCommandExecutorSQLAbstract.CLUSTER_PREFIX.length());

    else if (subjectName.startsWith(OCommandExecutorSQLAbstract.INDEX_PREFIX))
      // INDEX
      indexName = subjectName.substring(OCommandExecutorSQLAbstract.INDEX_PREFIX.length());

    else {
      // CLASS
      if (subjectName.startsWith(OCommandExecutorSQLAbstract.CLASS_PREFIX))
        subjectName = subjectName.substring(OCommandExecutorSQLAbstract.CLASS_PREFIX.length());

      final OClass cls = database.getMetadata().getSchema().getClass(subjectName);
      if (cls == null)
        throwParsingException("Class " + subjectName + " not found in database");

      className = cls.getName();
    }

    parserSkipWhiteSpaces();
    if (parserIsEnded())
      throwSyntaxErrorException("Set of fields is missed. Example: (name, surname) or SET name = 'Bill'");

    newRecords = new ArrayList<Map<String, Object>>();
    if (parserGetCurrentChar() == '(') {
      parseBracesFields();
    } else {
      final LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
      newRecords.add(fields);

      // ADVANCE THE SET KEYWORD
      parseRequiredWord(false);

      parseSetFields(fields);
    }

    return this;
  }

  protected void parseBracesFields() {
    final int beginFields = parserGetCurrentPosition();

    final int endFields = text.indexOf(')', beginFields + 1);
    if (endFields == -1)
      throwSyntaxErrorException("Missed closed brace");

    final ArrayList<String> fieldNames = new ArrayList<String>();
    parserSetCurrentPosition(OStringSerializerHelper.getParameters(text, beginFields, endFields, fieldNames));
    if (fieldNames.size() == 0)
      throwSyntaxErrorException("Set of fields is empty. Example: (name, surname)");

    // REMOVE QUOTATION MARKS IF ANY
    for (int i = 0; i < fieldNames.size(); ++i)
      fieldNames.set(i, OStringSerializerHelper.removeQuotationMarks(fieldNames.get(i)));

    parserRequiredKeyword(KEYWORD_VALUES);
    parserSkipWhiteSpaces();
    if (parserIsEnded() || text.charAt(parserGetCurrentPosition()) != '(') {
      throwParsingException("Set of values is missed. Example: ('Bill', 'Stuart', 300)");
    }

    final int textEnd = text.lastIndexOf(')');

    int blockStart = parserGetCurrentPosition();
    int blockEnd = parserGetCurrentPosition();

    while (blockStart != textEnd) {
      // skip comma between records
      blockStart = text.indexOf('(', blockStart - 1);

      blockEnd = OStringSerializerHelper.findEndBlock(text, '(', ')', blockStart);
      if (blockEnd == -1)
        throw new OCommandSQLParsingException("Missed closed brace. Use " + getSyntax(), text, blockStart);

      final List<String> values = OStringSerializerHelper.smartSplit(text, new char[] { ',' }, blockStart + 1, blockEnd - 1, true);

      if (values.isEmpty()) {
        throw new OCommandSQLParsingException("Set of values is empty. Example: ('Bill', 'Stuart', 300). Use " + getSyntax(), text,
            blockStart);
      }

      if (values.size() != fieldNames.size()) {
        throw new OCommandSQLParsingException("Fields not match with values", text, blockStart);
      }

      // TRANSFORM FIELD VALUES
      final Map<String, Object> fields = new LinkedHashMap<String, Object>();
      for (int i = 0; i < values.size(); ++i) {
        fields.put(fieldNames.get(i), OSQLHelper.parseValue(this, OStringSerializerHelper.decode(values.get(i).trim()), context));
      }
      newRecords.add(fields);
      blockStart = blockEnd;
    }

  }

  /**
   * Execute the INSERT and return the ODocument object created.
   */
  public Object execute(final Map<Object, Object> iArgs) {
    if (newRecords == null)
      throw new OCommandExecutionException("Cannot execute the command because it has not been parsed yet");

    if (indexName != null) {
      final OIndex<?> index = getDatabase().getMetadata().getIndexManager().getIndex(indexName);
      if (index == null)
        throw new OCommandExecutionException("Target index '" + indexName + "' not found");

      // BIND VALUES
      Map<String, Object> result = null;
      for (Map<String, Object> candidate : newRecords) {
        index.put(candidate.get(KEYWORD_KEY), (OIdentifiable) candidate.get(KEYWORD_RID));
        result = candidate;
      }

      // RETURN LAST ENTRY
      return new ODocument(result);
    } else {

      // CREATE NEW DOCUMENTS
      final List<ODocument> docs = new ArrayList<ODocument>();
      for (Map<String, Object> candidate : newRecords) {
        final ODocument doc = className != null ? new ODocument(className) : new ODocument();
        OSQLHelper.bindParameters(doc, candidate, new OCommandParameters(iArgs));

        if (clusterName != null) {
          doc.save(clusterName);
        } else {
          doc.save();
        }
        docs.add(doc);
      }

      if (docs.size() == 1) {
        return docs.get(0);
      } else {
        return docs;
      }
    }
  }

  public boolean isReplicated() {
    return indexName != null;
  }

  @Override
  public String getSyntax() {
    return "INSERT INTO <Class>|cluster:<cluster>|index:<index> [(<field>[,]*) VALUES (<expression>[,]*)[,]*]|[SET <field> = <expression>[,]*]";
  }
}
