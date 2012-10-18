/*
    JSPWiki - a JSP-based WikiWiki clone.

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.   
 */
package org.apache.wiki;

import org.apache.commons.fileupload.ProgressListener;
import org.apache.wiki.spring.BeanHolder;
import org.apache.wiki.ui.progress.ProgressItem;
import org.apache.wiki.ui.progress.ProgressManager;

/**
 * Provides tracking for upload progress.
 * 
 */
public class CommonProgressListener extends ProgressItem implements
        ProgressListener {
    public long m_currentBytes;
    public long m_totalBytes;
    private ProgressManager mana;
    private String progressId;

    public void update(long recvdBytes, long totalBytes, int item) {
        m_currentBytes = recvdBytes;
        m_totalBytes = totalBytes;
    }

    public int getProgress() {
        return (int) (((float) m_currentBytes / m_totalBytes) * 100 + 0.5);
    }

    public void startProgress(String progressId) {
        mana = BeanHolder.getProgressManager();
        this.progressId = progressId;
        mana.startProgress(this, progressId);
    }

    public void stopProgress() {
        mana.stopProgress(progressId);
    }
}
