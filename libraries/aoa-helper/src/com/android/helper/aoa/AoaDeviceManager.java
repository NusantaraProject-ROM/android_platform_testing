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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * AOAv2 device manager, from which devices and connections are retrieved.
 *
 * @see <a href="https://source.android.com/devices/accessories/aoa2">Android Open Accessory
 *     Protocol 2.0</a>
 */
public class AoaDeviceManager implements AutoCloseable {

    private static final Duration CONNECTION_POLL_INTERVAL = Duration.ofSeconds(1L);

    private final IUsbNative mUsb;
    private Pointer mContext;

    public AoaDeviceManager() {
        this((IUsbNative) Native.loadLibrary("usb-1.0", IUsbNative.class));
    }

    @VisibleForTesting
    AoaDeviceManager(@Nonnull IUsbNative usb) {
        mUsb = usb;
        // initialize context
        PointerByReference context = new PointerByReference();
        checkResult(mUsb.libusb_init(context));
        mContext = context.getValue();
    }

    /** Verifies a USB response, throwing an exception if it corresponds to an error. */
    int checkResult(int value) {
        if (value < 0) {
            throw new AoaDeviceException(mUsb.libusb_strerror(value));
        }
        return value;
    }

    /**
     * Find an AOAv2-compatible device connected via USB using its serial number.
     *
     * @param serialNumber device serial number
     * @return AOAv2-compatible device
     */
    @Nonnull
    public AoaDevice getDevice(@Nonnull String serialNumber) {
        return waitForDevice(serialNumber, Duration.ZERO);
    }

    /**
     * Wait for an AOAv2-compatible device with the specified serial number to be connected via USB.
     *
     * @param serialNumber device serial number
     * @param timeout maximum time to wait for
     * @return AOAv2-compatible device
     */
    @Nonnull
    public AoaDevice waitForDevice(@Nonnull String serialNumber, @Nonnull Duration timeout) {
        AoaDeviceConnection connection = waitForConnection(serialNumber, timeout);
        if (connection == null) {
            throw new AoaDeviceException("Device %s not found", serialNumber);
        }
        return new AoaDevice(this, connection);
    }

    /**
     * Wait for an AOAv2-compatible device using its serial number, and open a connection to it.
     *
     * @param serialNumber device serial number
     * @param timeout maximum time to wait for
     * @return AOAv2-compatible device connection or {@code null} if not found
     */
    @Nullable
    AoaDeviceConnection waitForConnection(@Nonnull String serialNumber, @Nonnull Duration timeout) {
        Instant start = Instant.now();
        AoaDeviceConnection connection = getConnection(serialNumber);

        while (connection == null
                && timeout.compareTo(Duration.between(start, Instant.now())) > 0) {
            Uninterruptibles.sleepUninterruptibly(
                    CONNECTION_POLL_INTERVAL.toNanos(), TimeUnit.NANOSECONDS);
            connection = getConnection(serialNumber);
        }

        return connection;
    }

    /**
     * Open a connection to an AOAv2-compatible device using its serial number.
     *
     * @param serialNumber device serial number
     * @return AOAv2-compatible device connection or {@code null} if not found
     */
    @Nullable
    AoaDeviceConnection getConnection(@Nonnull String serialNumber) {
        // retrieve all connected USB devices
        PointerByReference list = new PointerByReference();
        int count = checkResult(mUsb.libusb_get_device_list(checkNotNull(mContext), list));

        try {
            for (Pointer devicePointer : list.getValue().getPointerArray(0, count)) {
                // check if device has the right serial number and is compatible
                AoaDeviceConnection connection = connect(devicePointer);
                if (connection.isValid()
                        && serialNumber.equals(connection.getSerialNumber())
                        && connection.isAoaCompatible()) {
                    return connection;
                } else {
                    connection.close();
                }
            }

            // device not found
            return null;

        } finally {
            mUsb.libusb_free_device_list(list.getValue(), true);
        }
    }

    @VisibleForTesting
    AoaDeviceConnection connect(Pointer devicePointer) {
        return new AoaDeviceConnection(mUsb, devicePointer);
    }

    /** De-initialize the USB context. */
    @Override
    public void close() {
        if (mContext != null) {
            mUsb.libusb_exit(mContext);
            mContext = null;
        }
    }
}
