/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package io.github.ilmich.tempesta.util;

import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public class MXBeanUtil {

    private static final Logger logger = Logger.getLogger(MXBeanUtil.class.getName());

    private MXBeanUtil() {
    }

    public static void registerMXBean(Object self, String type, String name) {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            String mbeanName = "io.github.ilmich.tempesta:type=" + type + ",name=" + name;
            mbs.registerMBean(self, new ObjectName(mbeanName));
        } catch (Exception e) {
            logger.severe("Unable to register "+self.getClass().getCanonicalName()+" MXBean: " + e.getMessage());
        }
    }

}
