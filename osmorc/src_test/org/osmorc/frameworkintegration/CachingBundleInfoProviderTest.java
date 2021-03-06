/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.frameworkintegration;

import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.osgi.jps.build.CachingBundleInfoProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osmorc.TestUtil;

import java.io.File;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class CachingBundleInfoProviderTest {
  private File myTempDir;
  private String myDirBundle;
  private String myInvalidDirBundle;
  private String myJarBundle;

  @Before
  public void setUp() throws Exception {
    myTempDir = FileUtil.createTempDirectory("osgi.", ".test");
    TestUtil.extractProject("CachingBundleInfoProviderTest", myTempDir.getPath());
    myDirBundle = new File(myTempDir, "t0/dirbundle").getPath();
    myInvalidDirBundle = new File(myTempDir, "t0/invaliddirbundle").getPath();
    myJarBundle = new File(myTempDir, "t0/jarbundle.jar").getPath();
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.delete(myTempDir);
  }

  @Test
  public void testIsBundle() {
    assertThat(CachingBundleInfoProvider.isBundle(myDirBundle), equalTo(true));
    assertThat(CachingBundleInfoProvider.isBundle(myJarBundle), equalTo(true));
    assertThat(CachingBundleInfoProvider.isBundle(myInvalidDirBundle), equalTo(false));
  }

  @Test
  public void testGetBundleSymbolicName() {
    assertThat(CachingBundleInfoProvider.getBundleSymbolicName(myDirBundle), equalTo("dirbundle"));
    assertThat(CachingBundleInfoProvider.getBundleSymbolicName(myJarBundle), equalTo("jarbundle"));
    assertThat(CachingBundleInfoProvider.getBundleSymbolicName(myInvalidDirBundle), equalTo(null));
  }

  @Test
  public void testGetBundleVersions() {
    assertThat(CachingBundleInfoProvider.getBundleVersion(myDirBundle), equalTo("1.0.0"));
    assertThat(CachingBundleInfoProvider.getBundleVersion(myJarBundle), equalTo("1.0.0"));
    assertThat(CachingBundleInfoProvider.getBundleVersion(myInvalidDirBundle), equalTo(null));
  }

  @Test
  public void testIsFragmentBundle() {
    assertThat(CachingBundleInfoProvider.isFragmentBundle(myDirBundle), equalTo(true));
    assertThat(CachingBundleInfoProvider.isFragmentBundle(myJarBundle), equalTo(false));
    assertThat(CachingBundleInfoProvider.isFragmentBundle(myInvalidDirBundle), equalTo(false));
  }
}
