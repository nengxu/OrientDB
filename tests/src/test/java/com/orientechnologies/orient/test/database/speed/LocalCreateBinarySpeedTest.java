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
package com.orientechnologies.orient.test.database.speed;

import java.util.Random;

import org.testng.annotations.Test;

import com.orientechnologies.common.profiler.OProfiler;
import com.orientechnologies.orient.core.db.record.ODatabaseFlat;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
import com.orientechnologies.orient.core.record.impl.ORecordBytes;
import com.orientechnologies.orient.core.tx.OTransaction.TXTYPE;
import com.orientechnologies.orient.test.database.base.OrientMonoThreadTest;

@Test(enabled = false)
public class LocalCreateBinarySpeedTest extends OrientMonoThreadTest {
  private ODatabaseFlat    database;
  private ORecordBytes     record;
  private final static int RECORD_SIZE = 512;
  private byte[]           recordContent;

  public static void main(String[] iArgs) throws InstantiationException, IllegalAccessException {
    LocalCreateBinarySpeedTest test = new LocalCreateBinarySpeedTest();
    test.data.go(test);
  }

  public LocalCreateBinarySpeedTest() throws InstantiationException, IllegalAccessException {
    super(1000000);
  }

  @Override
  public void init() {
    OProfiler.getInstance().startRecording();

    database = new ODatabaseFlat(System.getProperty("url")).open("admin", "admin");
    record = new ORecordBytes();

    database.declareIntent(new OIntentMassiveInsert());
    database.begin(TXTYPE.NOTX);
    Random rnd = new Random();
    recordContent = new byte[RECORD_SIZE];
    for (int i = 0; i < RECORD_SIZE; ++i)
      recordContent[i] = (byte) rnd.nextInt(256);
  }

  @Override
  public void cycle() {
    record.reset(recordContent).save("binary");

    if (data.getCyclesDone() == data.getCycles() - 1)
      database.commit();
  }

  @Override
  public void deinit() {
    if (database != null)
      database.close();
    super.deinit();
  }
}
