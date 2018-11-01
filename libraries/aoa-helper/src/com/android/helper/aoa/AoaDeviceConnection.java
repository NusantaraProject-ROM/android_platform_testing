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

import com.google.common.primitives.Shorts;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** USB connection to an AOAv2-compatible device. */
class AoaDeviceConnection implements AutoCloseable {

    // USB constants
    private static final byte USB_DIR_IN = -128;
    private static final byte USB_DIR_OUT = 0;
    private static final byte USB_TYPE_VENDOR = 64;
    private static final byte GET_PROTOCOL = 51;

    // USB request types
    static final byte INPUT = USB_DIR_IN | USB_TYPE_VENDOR;
    static final byte OUTPUT = USB_DIR_OUT | USB_TYPE_VENDOR;

    private final IUsbNative mUsb;
    private final byte[] mDescriptor = new byte[18];
    private Pointer mHandle;

    AoaDeviceConnection(@Nonnull IUsbNative usb, @Nonnull Pointer devicePointer) {
        mUsb = usb;

        // retrieve device descriptor
        mUsb.libusb_get_device_descriptor(devicePointer, mDescriptor);

        // obtain device handle
        PointerByReference handle = new PointerByReference();
        mUsb.libusb_open(devicePointer, handle);
        mHandle = handle.getValue();
    }

    /**
     * Performs a synchronous control transaction with unlimited timeout.
     *
     * @return number of bytes transferred, or an error code
     */
    int controlTransfer(byte requestType, byte request, int value, int index, byte[] data) {
        return mUsb.libusb_control_transfer(
                checkNotNull(mHandle),
                requestType,
                request,
                (short) value,
                (short) index,
                data,
                (short) data.length,
                0);
    }

    /** @return true if device handle is non-null, but does not check if resetting is necessary */
    boolean isValid() {
        return mHandle != null;
    }

    /** @return device's serial number or {@code null} if serial could not be determined */
    @Nullable
    String getSerialNumber() {
        if (!isValid() || mDescriptor[16] <= 0) {
            // no device handle or string index is invalid
            return null;
        }

        byte[] data = new byte[64];
        int length = mUsb.libusb_get_string_descriptor_ascii(mHandle, mDescriptor[16], data, 64);
        return length > 0 ? new String(data, 0, length) : null;
    }

    /** @return device's vendor ID */
    int getVendorId() {
        return Shorts.fromBytes(mDescriptor[9], mDescriptor[8]);
    }

    /** @return device's product ID */
    int getProductId() {
        return Shorts.fromBytes(mDescriptor[11], mDescriptor[10]);
    }

    /** @return true if device supports the AOAv2 protocol */
    boolean isAoaCompatible() {
        if (!isValid()) {
            return false;
        }

        int protocol = controlTransfer(INPUT, GET_PROTOCOL, 0, 0, new byte[2]);
        return protocol >= 2;
    }

    /** Close the connection if necessary. */
    @Override
    public void close() {
        if (isValid()) {
            mUsb.libusb_close(mHandle);
            mHandle = null;
        }
    }
}
