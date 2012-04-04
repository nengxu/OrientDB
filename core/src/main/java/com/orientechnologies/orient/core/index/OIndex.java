/*
 * Copyright 1999-2010 Luca Garulli (l.garulli--at--orientechnologies.com)
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
package com.orientechnologies.orient.core.index;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import com.orientechnologies.common.listener.OProgressListener;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Basic interface to handle index.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * 
 */
public interface OIndex<T> {

	/**
	 * Creates the index.
	 * 
	 * 
	 * @param iName
	 * 
	 * @param iDatabase
	 *          Current Database instance
	 * @param iClusterIndexName
	 *          Cluster name where to place the TreeMap
	 * @param iClusterIdsToIndex
	 * @param iProgressListener
	 */
	public OIndex<T> create(String iName, final OIndexDefinition iIndexDefinition, final ODatabaseRecord iDatabase,
			final String iClusterIndexName, final int[] iClusterIdsToIndex, final OProgressListener iProgressListener);

	/**
	 * Unloads the index freeing the resource in memory.
	 */
	public void unload();

	/**
	 * Types of the keys that index can accept, if index contains composite key, list of types of elements from which this index
	 * consist will be returned, otherwise single element (key type obviously) will be returned.
	 */
	public OType[] getKeyTypes();

	/**
	 * Returns an iterator to walk across all the index items.
	 * 
	 * @return
	 */
	public Iterator<Entry<Object, T>> iterator();

	/**
	 * Gets the set of records associated with the passed key.
	 * 
	 * @param iKey
	 *          The key to search
	 * @return The Record set if found, otherwise an empty Set
	 */
	public T get(Object iKey);

	/**
	 * Tells if a key is contained in the index.
	 * 
	 * @param iKey
	 *          The key to search
	 * @return True if the key is contained, otherwise false
	 */
	public boolean contains(Object iKey);

	/**
	 * Inserts a new entry in the index. The behaviour depends by the index implementation.
	 * 
	 * @param iKey
	 *          Entry's key
	 * @param iValue
	 *          Entry's value as OIdentifiable instance
	 * @return The index instance itself to allow in chain calls
	 */
	public OIndex<T> put(Object iKey, OIdentifiable iValue);

	/**
	 * Removes an entry by its key.
	 * 
	 * @param iKey
	 *          The entry's key to remove
	 * @return True if the entry has been found and removed, otherwise false
	 */
	public boolean remove(Object iKey);

	/**
	 * Removes an entry by its key and value.
	 * 
	 * @param iKey
	 *          The entry's key to remove
	 * @param iValue
	 *          Entry's value as OIdentifiable instance
	 * @return True if the entry has been found and removed, otherwise false
	 */
	public boolean remove(Object iKey, OIdentifiable iRID);

	/**
	 * Removes a value in all the index entries.
	 * 
	 * @param iRID
	 *          Record id to search
	 * @return Times the record was found, 0 if not found at all
	 */
	public int remove(OIdentifiable iRID);

	/**
	 * Clears the index removing all the entries in one shot.
	 * 
	 * @return The index instance itself to allow in chain calls
	 */
	public OIndex<T> clear();

	/**
	 * Returns an Iterable instance of all the keys contained in the index.
	 * 
	 * @return A Iterable<Object> that lazy load the entries once fetched
	 */
	public Iterable<Object> keys();

	/**
	 * Returns a set of records with key between the range passed as parameter. Range bounds are included.
	 * 
	 * In case of {@link com.orientechnologies.common.collection.OCompositeKey}s partial keys can be used as values boundaries.
	 * 
	 * @param iRangeFrom
	 *          Starting range
	 * @param iRangeTo
	 *          Ending range
	 * 
	 * @return a set of records with key between the range passed as parameter. Range bounds are included.
	 * @see com.orientechnologies.common.collection.OCompositeKey#compareTo(com.orientechnologies.common.collection.OCompositeKey)
	 * @see #getValuesBetween(Object, boolean, Object, boolean)
	 */
	public Collection<OIdentifiable> getValuesBetween(Object iRangeFrom, Object iRangeTo);

	/**
	 * Returns a set of records with key between the range passed as parameter.
	 * 
	 * In case of {@link com.orientechnologies.common.collection.OCompositeKey}s partial keys can be used as values boundaries.
	 * 
	 * @param iRangeFrom
	 *          Starting range
	 * @param iFromInclusive
	 *          Indicates whether start range boundary is included in result.
	 * @param iRangeTo
	 *          Ending range
	 * @param iToInclusive
	 *          Indicates whether end range boundary is included in result.
	 * 
	 * @return Returns a set of records with key between the range passed as parameter.
	 * 
	 * @see com.orientechnologies.common.collection.OCompositeKey#compareTo(com.orientechnologies.common.collection.OCompositeKey)
	 * 
	 */
	public Collection<OIdentifiable> getValuesBetween(Object iRangeFrom, boolean iFromInclusive, Object iRangeTo, boolean iToInclusive);

	public Collection<OIdentifiable> getValuesBetween(Object iRangeFrom, boolean iFromInclusive, Object iRangeTo,
			boolean iToInclusive, int maxValuesToFetch);

	/**
	 * Returns a set of records with keys greater than passed parameter.
	 * 
	 * @param fromKey
	 *          Starting key.
	 * @param isInclusive
	 *          Indicates whether record with passed key will be included.
	 * 
	 * @return set of records with keys greater than passed parameter.
	 */
	public abstract Collection<OIdentifiable> getValuesMajor(Object fromKey, boolean isInclusive);

	public abstract Collection<OIdentifiable> getValuesMajor(Object fromKey, boolean isInclusive, int maxValuesToFetch);

	/**
	 * Returns a set of records with keys less than passed parameter.
	 * 
	 * @param toKey
	 *          Ending key.
	 * @param isInclusive
	 *          Indicates whether record with passed key will be included.
	 * 
	 * @return set of records with keys less than passed parameter.
	 */
	public abstract Collection<OIdentifiable> getValuesMinor(Object toKey, boolean isInclusive);

	public abstract Collection<OIdentifiable> getValuesMinor(Object toKey, boolean isInclusive, int maxValuesToFetch);

	/**
	 * Returns a set of documents that contains fields ("key", "rid") where "key" - index key, "rid" - record id of records with keys
	 * greater than passed parameter.
	 * 
	 * @param fromKey
	 *          Starting key.
	 * @param isInclusive
	 *          Indicates whether record with passed key will be included.
	 * 
	 * @return set of records with key greater than passed parameter.
	 */
	public abstract Collection<ODocument> getEntriesMajor(Object fromKey, boolean isInclusive);

	public abstract Collection<ODocument> getEntriesMajor(Object fromKey, boolean isInclusive, int maxEntriesToFetch);

	/**
	 * Returns a set of documents that contains fields ("key", "rid") where "key" - index key, "rid" - record id of records with keys
	 * less than passed parameter.
	 * 
	 * @param toKey
	 *          Ending key.
	 * @param isInclusive
	 *          Indicates whether record with passed key will be included.
	 * 
	 * @return set of records with key greater than passed parameter.
	 */
	public abstract Collection<ODocument> getEntriesMinor(Object toKey, boolean isInclusive);

	public abstract Collection<ODocument> getEntriesMinor(Object toKey, boolean isInclusive, int maxEntriesToFetch);

	/**
	 * Returns a set of documents with key between the range passed as parameter.
	 * 
	 * @param iRangeFrom
	 *          Starting range
	 * @param iRangeTo
	 *          Ending range
	 * @param iInclusive
	 *          Include from/to bounds
	 * @see #getEntriesBetween(Object, Object)
	 * @return
	 */
	public abstract Collection<ODocument> getEntriesBetween(final Object iRangeFrom, final Object iRangeTo, final boolean iInclusive);

	public abstract Collection<ODocument> getEntriesBetween(final Object iRangeFrom, final Object iRangeTo, final boolean iInclusive,
			final int maxEntriesToFetch);

	public Collection<ODocument> getEntriesBetween(Object iRangeFrom, Object iRangeTo);

	/**
	 * @return number of entries in the index.
	 */
	public long getSize();

	/**
	 * For unique indexes it will throw exception if passed in key is contained in index.
	 * 
	 * @param iRecord
	 * @param iKey
	 */
	public void checkEntry(final OIdentifiable iRecord, final Object iKey);

	/**
	 * Stores all the in-memory changes to disk.
	 * 
	 * @return The index instance itself to allow in chain calls
	 */
	public OIndex<T> lazySave();

	/**
	 * Delete the index.
	 * 
	 * @return The index instance itself to allow in chain calls
	 */
	public OIndex<T> delete();

	/**
	 * Returns the index name.
	 * 
	 * @return The name of the index
	 */
	public String getName();

	/**
	 * Returns the type of the index as string.
	 */
	public String getType();

	/**
	 * Tells if the index is automatic. Automatic means it's maintained automatically by OrientDB. This is the case of indexes created
	 * against schema properties. Automatic indexes can always been rebuilt.
	 * 
	 * @return True if the index is automatic, otherwise false
	 */
	public boolean isAutomatic();

	/**
	 * Rebuilds an automatic index.
	 * 
	 * @return The number of entries rebuilt
	 */
	public long rebuild();

	/**
	 * Populate the index with all the existent records.
	 */
	public long rebuild(final OProgressListener iProgressListener);

	/**
	 * Returns the index configuration.
	 * 
	 * @return An ODocument object containing all the index properties
	 */
	public ODocument getConfiguration();

	/**
	 * Returns the Record Identity of the index if persistent.
	 * 
	 * @return Valid ORID if it's persistent, otherwise ORID(-1:-1)
	 */
	public ORID getIdentity();

	/**
	 * Commits changes as atomic. It's called during the transaction's commit.
	 * 
	 * @param iDocument
	 *          Collection of entries to commit
	 */
	public void commit(ODocument iDocument);

	/**
	 * Returns the internal index used.
	 * 
	 */
	public OIndexInternal<T> getInternal();

	/**
	 * Returns set of records with keys in specific set
	 * 
	 * @param iKeys
	 *          Set of keys
	 * @return
	 */
	public Collection<OIdentifiable> getValues(Collection<?> iKeys);

	public Collection<OIdentifiable> getValues(Collection<?> iKeys, int maxValuesToFetch);

	/**
	 * Returns a set of documents with keys in specific set
	 * 
	 * @param iKeys
	 *          Set of keys
	 * @return
	 */
	public Collection<ODocument> getEntries(Collection<?> iKeys);

	public Collection<ODocument> getEntries(Collection<?> iKeys, int maxEntriesToFetch);

	public OIndexDefinition getDefinition();
}
