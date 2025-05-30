/*
 * Copyright (c) 2002-2025, the original author(s).
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package org.jline.reader.impl;

import java.util.Objects;

import org.jline.reader.MaskingCallback;

/**
 * Simple {@link MaskingCallback} that will replace all the characters in the line with the given mask.
 * If the given mask is equal to {@link LineReaderImpl#NULL_MASK} then the line will be replaced with an empty String.
 */
public final class SimpleMaskingCallback implements MaskingCallback {
    private final Character mask;

    public SimpleMaskingCallback(Character mask) {
        this.mask = Objects.requireNonNull(mask, "mask must be a non null character");
    }

    @Override
    public String display(String line) {
        if (mask.equals(LineReaderImpl.NULL_MASK)) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder(line.length());
            for (int i = line.length(); i-- > 0; ) {
                sb.append((char) mask);
            }
            return sb.toString();
        }
    }

    @Override
    public String history(String line) {
        return null;
    }
}
