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

// Generated by jextract

package org.apache.tomcat.util.openssl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.CLinker.*;
class constants$5 {

    static final FunctionDescriptor EVP_PKEY_base_id$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER
    );
    static final MethodHandle EVP_PKEY_base_id$MH = RuntimeHelper.downcallHandle(
        openssl_h.LIBRARIES, "EVP_PKEY_base_id",
        "(Ljdk/incubator/foreign/MemoryAddress;)I",
        constants$5.EVP_PKEY_base_id$FUNC, false
    );
    static final FunctionDescriptor EVP_PKEY_bits$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER
    );
    static final MethodHandle EVP_PKEY_bits$MH = RuntimeHelper.downcallHandle(
        openssl_h.LIBRARIES, "EVP_PKEY_bits",
        "(Ljdk/incubator/foreign/MemoryAddress;)I",
        constants$5.EVP_PKEY_bits$FUNC, false
    );
    static final FunctionDescriptor EC_GROUP_free$FUNC = FunctionDescriptor.ofVoid(
        C_POINTER
    );
    static final MethodHandle EC_GROUP_free$MH = RuntimeHelper.downcallHandle(
        openssl_h.LIBRARIES, "EC_GROUP_free",
        "(Ljdk/incubator/foreign/MemoryAddress;)V",
        constants$5.EC_GROUP_free$FUNC, false
    );
    static final FunctionDescriptor EC_GROUP_get_curve_name$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER
    );
    static final MethodHandle EC_GROUP_get_curve_name$MH = RuntimeHelper.downcallHandle(
        openssl_h.LIBRARIES, "EC_GROUP_get_curve_name",
        "(Ljdk/incubator/foreign/MemoryAddress;)I",
        constants$5.EC_GROUP_get_curve_name$FUNC, false
    );
    static final FunctionDescriptor EC_KEY_new_by_curve_name$FUNC = FunctionDescriptor.of(C_POINTER,
        C_INT
    );
    static final MethodHandle EC_KEY_new_by_curve_name$MH = RuntimeHelper.downcallHandle(
        openssl_h.LIBRARIES, "EC_KEY_new_by_curve_name",
        "(I)Ljdk/incubator/foreign/MemoryAddress;",
        constants$5.EC_KEY_new_by_curve_name$FUNC, false
    );
    static final FunctionDescriptor EC_KEY_free$FUNC = FunctionDescriptor.ofVoid(
        C_POINTER
    );
    static final MethodHandle EC_KEY_free$MH = RuntimeHelper.downcallHandle(
        openssl_h.LIBRARIES, "EC_KEY_free",
        "(Ljdk/incubator/foreign/MemoryAddress;)V",
        constants$5.EC_KEY_free$FUNC, false
    );
}


