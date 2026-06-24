package com.simplemaker.toolleveler.config.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.function.Function;

public final class CodecHelper {

    public static final Codec<Short>  NON_NEGATIVE_SHORT = shortRange((short) 0, Short.MAX_VALUE);
    public static final Codec<Short>  POSITIVE_SHORT     = shortRange((short) 1, Short.MAX_VALUE);
    public static final Codec<Long>   NON_NEGATIVE_LONG  = longRange(0L, Long.MAX_VALUE);
    public static final Codec<Long>   POSITIVE_LONG      = longRange(1L, Long.MAX_VALUE);

    /** 0.0 – 100.0 multiplier / percentage. */
    public static final Codec<Double> PERCENTAGE =
            Codec.DOUBLE.flatXmap(
                    checkRange(0.0D, 100.0D),
                    checkRange(0.0D, 100.0D)
            );

    public static Codec<Long> longRange(final long min, final long max) {
        Function<Long, DataResult<Long>> c = checkRange(min, max);
        return Codec.LONG.flatXmap(c, c);
    }

    public static Codec<Short> shortRange(final short min, final short max) {
        Function<Short, DataResult<Short>> c = checkRange(min, max);
        return Codec.SHORT.flatXmap(c, c);
    }

    private static <N extends Number & Comparable<N>> Function<N, DataResult<N>> checkRange(
            final N min, final N max) {
        return value -> {
            if (value.compareTo(min) >= 0 && value.compareTo(max) <= 0) {
                return DataResult.success(value);
            }
            return DataResult.error(
                    () -> "Value " + value + " outside of range [" + min + ":" + max + "]",
                    value
            );
        };
    }
}