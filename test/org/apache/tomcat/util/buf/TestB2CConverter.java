/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomcat.util.buf;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.spi.CharsetProvider;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class TestB2CConverter {

    private static final byte[] UTF16_MESSAGE =
            new byte[] {-2, -1, 0, 65, 0, 66, 0, 67};

    private static final byte[] UTF8_INVALID = new byte[] {-8, -69, -73, -77};

    private static final byte[] UTF8_PARTIAL = new byte[] {-50};

    @Test
    public void testSingleMessage() throws Exception {
        testMessages(1);
    }

    @Test
    public void testTwoMessage() throws Exception {
        testMessages(2);
    }

    @Test
    public void testManyMessage() throws Exception {
        testMessages(10);
    }

    private void testMessages(int msgCount) throws Exception {
        B2CConverter conv = new B2CConverter(StandardCharsets.UTF_16);

        ByteChunk bc = new ByteChunk();
        CharChunk cc = new CharChunk(32);


        for (int i = 0; i < msgCount; i++) {
            bc.append(UTF16_MESSAGE, 0, UTF16_MESSAGE.length);
            conv.convert(bc, cc, true);
            Assert.assertEquals("ABC", cc.toString());
            bc.recycle();
            cc.recycle();
            conv.recycle();
        }

        System.out.println(cc);
    }

    @Test
    public void testLeftoverSize() {
        float maxLeftover = 0;
        String charsetName = "UNSET";
        for (Charset charset : Charset.availableCharsets().values()) {
            float leftover;
            if (charset.name().toLowerCase(Locale.ENGLISH).startsWith("x-")) {
                // Non-standard charset that browsers won't be using
                // Likely something used internally by the JRE
                continue;
            }
            try {
                leftover = charset.newEncoder().maxBytesPerChar();
            } catch (UnsupportedOperationException uoe) {
                // Skip it
                continue;
            }
            if (leftover > maxLeftover) {
                maxLeftover = leftover;
                charsetName = charset.name();
            }
        }
        Assert.assertTrue("Limit needs to be at least " + maxLeftover +
                " (used in charset '" + charsetName + "')",
                maxLeftover <= B2CConverter.LEFTOVER_SIZE);
    }

    @Test(expected=MalformedInputException.class)
    public void testBug54602a() throws Exception {
        // Check invalid input is rejected straight away
        B2CConverter conv = new B2CConverter(StandardCharsets.UTF_8);
        ByteChunk bc = new ByteChunk();
        CharChunk cc = new CharChunk();

        bc.append(UTF8_INVALID, 0, UTF8_INVALID.length);
        cc.allocate(bc.getLength(), -1);

        conv.convert(bc, cc, false);
    }

    @Test(expected=MalformedInputException.class)
    public void testBug54602b() throws Exception {
        // Check partial input is rejected
        B2CConverter conv = new B2CConverter(StandardCharsets.UTF_8);
        ByteChunk bc = new ByteChunk();
        CharChunk cc = new CharChunk();

        bc.append(UTF8_PARTIAL, 0, UTF8_PARTIAL.length);
        cc.allocate(bc.getLength(), -1);

        conv.convert(bc, cc, true);
    }

    @Test
    public void testBug54602c() throws Exception {
        // Check partial input is rejected once it is known to be all available
        B2CConverter conv = new B2CConverter(StandardCharsets.UTF_8);
        ByteChunk bc = new ByteChunk();
        CharChunk cc = new CharChunk();

        bc.append(UTF8_PARTIAL, 0, UTF8_PARTIAL.length);
        cc.allocate(bc.getLength(), -1);

        conv.convert(bc, cc, false);

        Exception e = null;
        try {
            conv.convert(bc, cc, true);
        } catch (MalformedInputException mie) {
            e = mie;
        }
        Assert.assertNotNull(e);
    }

    @Test
    public void testCommonEncodingsDontTriggerAvailableCharsetsCall() throws Exception {
        resetCharsetFields();
        B2CConverter.getCharset("utf-8");
        B2CConverter.getCharset("UTF-8");
        B2CConverter.getCharset("ISO-8859-1");
        Field availableCharsets = B2CConverter.class.getDeclaredField("availableCharsets");
        availableCharsets.setAccessible(true);
        Assert.assertNull(availableCharsets.get(null));
    }

    @Test
    public void testRepeatedUnsupportedEncodingExceptionsPopluatesLookup() throws Exception {
        resetCharsetFields();
        for (int i = 0; i < B2CConverter.UNSUPPORTED_ENCODING_THRESHOLD + 1; i++) {
            try {
                B2CConverter.getCharset("missing-" + i);
            } catch (UnsupportedEncodingException ex) {
            }
        }
        Field availableCharsets = B2CConverter.class.getDeclaredField("availableCharsets");
        availableCharsets.setAccessible(true);
        Assert.assertNotNull(availableCharsets.get(null));
    }

    @Test
    public void testConcurrentAccess() throws Exception {
        // See https://bz.apache.org/bugzilla/show_bug.cgi?id=51400
        String enc = "test-concurrent-caching";
        resetCharsetFields();

        // Make a call to populate the cache
        try {
            B2CConverter.getCharset(enc);
        } catch (UnsupportedEncodingException ex) {
        }

        // Get the standard provider so we can synchronize on it
        Field standardProviderField = Charset.class.getDeclaredField("standardProvider");
        standardProviderField.setAccessible(true);
        CharsetProvider standardProvider = (CharsetProvider) standardProviderField.get(null);

        // Create a thread that will trigger the provider on a cache miss
        Object[] result = new Object[1];
        Thread thread = new Thread() {
            public void run() {
                try {
                    result[0] = B2CConverter.getCharset(enc);
                } catch (UnsupportedEncodingException ex) {
                    result[0] = ex;
                }
            };
        };

        // Lock the provider so that FastCharsetProvider.FastCharsetProvider will block
        synchronized (standardProvider) {
            // Trigger the thread, it should run because it finds the cached values
            // and doesn't call the FastCharsetProvider
            thread.start();
            thread.join(1000);
        }

        // Check that our thread actually ran
        Assert.assertTrue(result[0] instanceof UnsupportedEncodingException);
    }

    @SuppressWarnings("rawtypes")
    private void resetCharsetFields() throws NoSuchFieldException, IllegalAccessException {
        Field availableCharsets = B2CConverter.class.getDeclaredField("availableCharsets");
        availableCharsets.setAccessible(true);
        availableCharsets.set(null, null);
        Field lookupFailureCount = B2CConverter.class.getDeclaredField("lookupFailureCount");
        lookupFailureCount.setAccessible(true);
        ((AtomicInteger) lookupFailureCount.get(null)).set(0);;
        Field cache = B2CConverter.class.getDeclaredField("cache");
        cache.setAccessible(true);
        ((Map) cache.get(null)).clear();
    }

}
