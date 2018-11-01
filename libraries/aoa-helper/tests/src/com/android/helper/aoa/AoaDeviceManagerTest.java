/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.helper.aoa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link AoaDeviceManager} */
@RunWith(JUnit4.class)
public class AoaDeviceManagerTest {

    private static final String SERIAL_NUMBER = "serial-number";

    private AoaDeviceManager mManager;

    private IUsbNative mUsb;
    private AoaDeviceConnection mConnection;

    @Before
    public void setUp() {
        // create dummy pointer
        Pointer pointer = new Memory(1);

        mUsb = mock(IUsbNative.class);
        // populate context when initialized
        when(mUsb.libusb_init(any()))
                .then(
                        invocation -> {
                            PointerByReference context =
                                    (PointerByReference) invocation.getArguments()[0];
                            context.setValue(pointer);
                            return null;
                        });
        // find device pointer when listing devices
        when(mUsb.libusb_get_device_list(any(), any()))
                .then(
                        invocation -> {
                            PointerByReference list =
                                    (PointerByReference) invocation.getArguments()[1];
                            list.setValue(pointer);
                            return 1;
                        });

        mConnection = mock(AoaDeviceConnection.class);

        mManager = spy(new AoaDeviceManager(mUsb));
        // always return the mocked connection
        doReturn(mConnection).when(mManager).connect(any());
    }

    @Test
    public void testContext() {
        // initialized on creation
        verify(mUsb, times(1)).libusb_init(any());

        // exited on close
        mManager.close();
        verify(mUsb, times(1)).libusb_exit(any());
    }

    @Test
    public void testCheckResult() {
        // non-negative numbers are always valid
        assertEquals(0, mManager.checkResult(0));
        assertEquals(1, mManager.checkResult(1));
        assertEquals(Integer.MAX_VALUE, mManager.checkResult(Integer.MAX_VALUE));
    }

    @Test(expected = AoaDeviceException.class)
    public void testCheckResult_invalid() {
        // negative numbers indicate errors
        mManager.checkResult(-1);
    }

    @Test
    public void testGetConnection() {
        when(mConnection.isValid()).thenReturn(true);
        when(mConnection.getSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(mConnection.isAoaCompatible()).thenReturn(true);

        // valid connection was found and opened
        assertEquals(mConnection, mManager.getConnection(SERIAL_NUMBER));

        // device list was closed, but not connection
        verify(mConnection, never()).close();
        verify(mUsb, times(1)).libusb_free_device_list(any(), eq(true));
    }

    @Test
    public void testGetConnection_invalid() {
        when(mConnection.isValid()).thenReturn(false);
        when(mConnection.getSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(mConnection.isAoaCompatible()).thenReturn(true);

        // valid connection not found
        assertNull(mManager.getConnection(SERIAL_NUMBER));

        // connection and device list were closed
        verify(mConnection, times(1)).close();
        verify(mUsb, times(1)).libusb_free_device_list(any(), eq(true));
    }

    @Test
    public void testGetConnection_missing() {
        when(mConnection.isValid()).thenReturn(true);
        when(mConnection.getSerialNumber()).thenReturn("unknown");
        when(mConnection.isAoaCompatible()).thenReturn(true);

        // valid connection not found
        assertNull(mManager.getConnection(SERIAL_NUMBER));

        // connection and device list were closed
        verify(mConnection, times(1)).close();
        verify(mUsb, times(1)).libusb_free_device_list(any(), eq(true));
    }

    @Test
    public void testGetConnection_incompatible() {
        when(mConnection.isValid()).thenReturn(true);
        when(mConnection.getSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(mConnection.isAoaCompatible()).thenReturn(false);

        // valid connection not found
        assertNull(mManager.getConnection(SERIAL_NUMBER));

        // connection and device list were closed
        verify(mConnection, times(1)).close();
        verify(mUsb, times(1)).libusb_free_device_list(any(), eq(true));
    }
}
